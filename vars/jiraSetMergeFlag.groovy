@Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.7.1')
import java.util.regex.Matcher
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method
import com.santaba.agent.groovyapi.http.*;
def String call() {
 def resvalues = readProperties file: "globalvalues.groovy"
 jiraUrl = resvalues['jiraUrl']
 println "jiraSetMergeFlag enter"
 println "jiraUrl is ${jiraUrl}"
 listofIssues = []
 checkedListofIssues = []
 checkedListofDefects = []
 if (env.pullRequestId == null) {
	    println "getting PR data"
		targetBranch  = getBranch().trim()
		getPR()
		title = readFile(file: 'TitlePR.txt')
        String[] line = title.split(" ");
		line.each{ l ->
		  println "line is ${l}"
		  switch( l ){
			case ~/[\s|]?([A-Z]+-[0-9]+)[\s:|]?/:
			  println "JIRA: ${Matcher.lastMatcher[0][1]}"
			  listofIssues.add(Matcher.lastMatcher[0][1])
			  break
			default:
			  println "no JIRA $l"
		  }
		}
		
		listofIssues.each{d ->
		  println "DEFECT is ${d}"
		  IsAJirraIssue(d,checkedListofIssues)
		}

		checkedListofIssues.each{d ->
		  println "REAL JIRA ISSUE ---->>> ${d} Checking is it a DEFECT"
		  def values = d.split('-')
		  if (values[0] == "SWDEFECT")
		  {
             checkedListofDefects.add(d)
		  }
		}
		checkedListofDefects.each{d ->
		  println "REAL DEFECT ---->>> ${d} -----"
		 jiraIssueSetMergeFlag(d)
		}
		 
	     
	

    }
	
}
def IsAJirraIssue(String issue,List<String> checkedListofIssues)
{

	try{

			myurl =  "${jiraUrl}/rest/api/2/issue/${issue}"
			def response = httpRequest authentication: 'Jira', ignoreSslErrors: true, url: myurl
			println('Status: '+response.status)
			checkedListofIssues.add(issue)
			
	} catch(Exception  ex) {
	//Catch block 
		println(ex.getMessage())
	}





}
def jiraIssueSetMergeFlag(String issue)
{

	try{

			myurl =  "${jiraUrl}/rest/api/2/issue/${issue}/transitions"
			bodytxt = "{ \"fields\" : { \"customfield_15606\" : [{ \"value\": \"Yes\"}]} , \"transition\": {\"id\": \"381\"}}"
			def response = httpRequest authentication: 'Jira', ignoreSslErrors: true, url: myurl, httpMode: "POST", contentType: "APPLICATION_JSON", requestBody: bodytxt
			println('Status: '+response.status)
			int intNum = response.status.toInteger()
			if (intNum > 399)
			{
				error ("failed to set merge flag on ${issue}")
			}
			
			
	} catch(Exception  ex) {
	//Catch block 
		println(ex.getMessage())
	}





}


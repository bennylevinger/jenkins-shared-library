@Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.7.1')
import java.util.regex.Matcher
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method
import com.santaba.agent.groovyapi.http.*;
def String call() {
 writeFile file: "globalvalues.groovy", text: libraryResource("globalvalues.groovy")
 def resvalues = readProperties file: "globalvalues.groovy"
 jiraUrl = resvalues['jiraUrl']
 listofIssues = []
 checkedListofIssues = []
 checkedListofDefects = []
 if (env.pullRequestId != null) {
		title = pullRequestTitle
		
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
		  fixedVersion = GetDetectedVersion(d)
          def b = IsFromTheRightVersion(fixedVersion)
		   sh """ 
			  rm  -f DefectListFile | true
			"""
		  if (b == false)
		    {
				echo "DEFECT fixedVersion is wrong"
				echo "The DEFECT ${d} fixedVersion ${fixedVersion} is wrong"
				error "The DEFECT ${d} fixedVersion ${fixedVersion} is wrong"
			}else
			{
				println "We are from the right version"
				  sh """ 
				  echo ${d} >> DefectListFile
				  """
			}
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
def String GetDetectedVersion(String issue)
{

	try{

			myurl =  "${jiraUrl}/rest/api/2/issue/${issue}/?fields=fixVersions"
			def response = httpRequest authentication: 'Jira', ignoreSslErrors: true, url: myurl
			println('Status: '+response.status)
			println('full response: ' + response.content)
			def jsonObj = readJSON text: response.content
			println('fixVersions is: ' + jsonObj.fields.fixVersions[0].name)
			retval = jsonObj.fields.fixVersions[0].name
			
	} catch(Exception  ex) {
	//Catch block 
		println(ex.getMessage())
	}

     return  retval



}
def Boolean IsFromTheRightVersion(String ver)
{
   def Boolean bret = false
    println( "checking fixed version " + ver)
	println( "targetBranch is " + targetBranch)

	switch (ver) {

          case "OCB":
		     println( "case  is OCB")
		     if (targetBranch == "develop")
			 {
				bret = true
			 }
		  break

		  case "S6 OCB":
		  println( "case  is S6 OCB")
            if (targetBranch == "develop")
			 {
				bret = true
			 }
		  break

	}


return bret


}
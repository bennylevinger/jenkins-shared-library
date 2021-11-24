def call() {

 //try {
	 echo "try env.pullRequestId is ${env.pullRequestId}"
	  if (env.pullRequestId != null) {
			myurl =  "http://v-git.inr.rd.hpicorp.net:7990/rest/api/1.0/projects/${env.destinationRepositoryOwner}/repos/${env.sourceRepositoryName}/pull-requests/${env.pullRequestId}"
			def response = httpRequest authentication: '51482bc4-7bcb-49c9-8852-b778c3fd8fb4', ignoreSslErrors: true, url: myurl
			println('Status: '+response.status)
			println('Response: '+response.content)
			def jsonObj = readJSON text: response.content
			println('id: '+jsonObj.id)
			println('eMail: '+jsonObj.author.user.emailAddress)
			commitMail = jsonObj.author.user.emailAddress
			eMail = 'indigodevops@hp.com'
			mybranch = sourceBranch
			subject = 'Process of ' + pullRequestTitle + ' build started'
	  } else {
	  
	       fullurl = env.GIT_URL
           def extrepo = fullurl.tokenize('/')[-1] 
           def proj = fullurl.tokenize('/')[-2] 
		   def repo = extrepo.tokenize('.')[-2] 
		   
	       myurl =  "http://v-git.inr.rd.hpicorp.net:7990/rest/api/1.0/projects/${proj}/repos/${repo}/commits?until=develop&limit=0&start=1"
		   def response = httpRequest authentication: '51482bc4-7bcb-49c9-8852-b778c3fd8fb4', ignoreSslErrors: true, url: myurl
		   println('Status: '+response.status)
		   println('Response: '+response.content)
		   def jsonObj = readJSON text: response.content
		   commitMail = jsonObj.values[0].author.emailAddress
		   println("commitMail: " + commitMail)
		   println("We are on regular branch")
		  //commitMail = getMailFromCommit()
		  eMail = 'indigodevops@hp.com'
		  mybranch = getGitBranch()
		  commitComment = getCOMMENTFromCommit()
		  subject = 'Process of ' + mybranch + ' build after merge ' +  commitComment + ' by ' + commitMail + ' started'
	  }

		 sh """
		        rm -f summary.html
			    
                 echo "<BR> Jenkins Log: ${BUILD_URL} <BR>" >> summary.html
				

				               
              
                exit 0
                
            """
			
		 mailList =	"${commitMail},${eMail}"
         emailext   body: readFile('summary.html'), mimeType: 'text/html', subject: subject + " from branch " + mybranch, to: mailList 		
 
  } 	
def getMailFromCommit() {
    def commitMail = ""
    sh 'git log -1 --pretty=format:%ae > outFile'
	commitMail = readFile 'outFile'
	println('commitMail:' + commitMail)
	return commitMail
}
def getGitBranch() {
    def gitBranch = ""
    sh 'git rev-parse --abbrev-ref HEAD > outFile'
	gitBranch = readFile 'outFile'
	println('gitBranch:' + gitBranch)
	return gitBranch
}
def getCOMMENTFromCommit() {
    def commitComment = ""
    sh 'git log -1 --pretty=%B > outFile'
	commitComment = readFile 'outFile'
	println('commitComment:' + commitComment)
	return commitComment
}
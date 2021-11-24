def call() {

 //try {
	 echo "try env.pullRequestId is ${env.pullRequestId}"
	  if (env.pullRequestId != null) {
			def response = httpRequest authentication: '51482bc4-7bcb-49c9-8852-b778c3fd8fb4', ignoreSslErrors: true, url: "http://v-git.inr.rd.hpicorp.net:7990/rest/api/1.0/projects/sqa/repos/docker-jenkins/pull-requests/${env.pullRequestId}"
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
		  println("We are on regular branch")
		  commitMail = getMailFromCommit()
		  eMail = 'indigodevops@hp.com'
		  mybranch = getGitBranch()
		  commitComment = getCOMMENTFromCommit()
		  subject = 'Process of ' + mybranch + ' build after merge ' +  commitComment + ' started'
	  }

		 sh """
		        rm -f summary.html
			    
                 echo "<BR> Jenkins Log: ${BUILD_URL}console <BR>" >> summary.html
				

				               
              
                exit 0
                
            """
			
		 mailList =	"${commitMail},${eMail}"
         emailext attachLog: true,   body: readFile('summary.html'), mimeType: 'text/html', subject: subject + " from branch " + mybranch, to: mailList 		
 
  } 	
def getMailFromCommit() {
    def commitMail = ""
    sh 'git log -1 --pretty=format:%ae > outFile'
	commitMail = readFile 'outFile'
	return commitMail
}
def getGitBranch() {
    def gitBranch = ""
    sh 'git rev-parse --abbrev-ref HEAD > outFile'
	gitBranch = readFile 'outFile'
	return gitBranch
}
def getCOMMENTFromCommit() {
    def commitComment = ""
    sh 'git log -1 --pretty=%B > outFile'
	commitComment = readFile 'outFile'
	return commitComment
}
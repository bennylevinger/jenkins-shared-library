def String call() {
    fullurl = env.GIT_URL
    def extrepo = fullurl.tokenize('/')[-1]
    def proj = fullurl.tokenize('/')[-2]
    def repo = extrepo.tokenize('.')[-2]
 if (env.pullRequestId != null) {
          myurl =  "http://v-git.inr.rd.hpicorp.net:7990/rest/api/1.0/projects/${proj}/repos/${repo}/pull-requests/${pullRequestId}"
          def responsePR = httpRequest authentication: '51482bc4-7bcb-49c9-8852-b778c3fd8fb4', ignoreSslErrors: true, url: myurl
          println('Status: '+responsePR.status)
          println('Response: '+responsePR.content)
          def jsonObjPR = readJSON text: responsePR.content
          
		  MyVersion = jsonObjPR.version
        
		  println("Pull Request Version: " + MyVersion)
		  println("Source Commit Hash: " + sourceCommitHash)
          return MyVersion
      } else {
          println("pullRequestId seems to be null")
      }


}
def String call(String PRver) {
    fullurl = env.GIT_URL
    def extrepo = fullurl.tokenize('/')[-1]
    def proj = fullurl.tokenize('/')[-2]
    def repo = extrepo.tokenize('.')[-2]
 if (pullRequestId != null) {
          myurl =  "http://v-git.inr.rd.hpicorp.net:7990/rest/api/1.0/projects/${proj}/repos/${repo}/pull-requests/${pullRequestId}"
          def responsePR = httpRequest authentication: '51482bc4-7bcb-49c9-8852-b778c3fd8fb4', ignoreSslErrors: true, url: myurl
          println('Status: '+responsePR.status)
          println('Response: '+responsePR.content)
          def jsonObjPR = readJSON text: responsePR.content
         
		  String MyVersion = jsonObjPR.version
		  println('MyVersion: '+MyVersion)
		  println('PRver: '+PRver)
          if (PRver == MyVersion ){
		  
		  myurl =  "http://v-git.inr.rd.hpicorp.net:7990/rest/api/1.0/projects/${proj}/repos/${repo}/pull-requests/${pullRequestId}/merge?version=" + MyVersion
          def responseMergePR = httpRequest authentication: '51482bc4-7bcb-49c9-8852-b778c3fd8fb4', ignoreSslErrors: true, url: myurl, httpMode: "POST", contentType: "APPLICATION_JSON"
          println('Status: '+responseMergePR.status)
          println('Response: '+responseMergePR.content)
          def jsonObjMergePR = readJSON text: responseMergePR.content
     
		  
		  }		  
		  else
		  {
		    println("You are attempting to merge pull request that was modified")
			error("You are attempting to merge pull request that was modified") 
		  }
		  
		  
		  
		  println("Version: " + MyVersion)
          
      } else {
          println("pullRequestId seems to be null")
      }


}
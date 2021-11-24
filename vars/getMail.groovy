def String call() {
  if (env.pullRequestId != null) {
            myurl =  "http://v-git.inr.rd.hpicorp.net:7990/rest/api/1.0/projects/${env.destinationRepositoryOwner}/repos/${env.sourceRepositoryName}/pull-requests/${env.pullRequestId}"
          def response = httpRequest authentication: '51482bc4-7bcb-49c9-8852-b778c3fd8fb4', ignoreSslErrors: true, url: myurl
          println('Status: '+response.status)
          println('Response: '+response.content)
          def jsonObj = readJSON text: response.content
          println('id: '+jsonObj.id)
          println('eMail: '+jsonObj.author.user.emailAddress)
          commitMail = jsonObj.author.user.emailAddress
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
        
       // println("We are on regular branch")
       
      }

      return commitMail
}
	
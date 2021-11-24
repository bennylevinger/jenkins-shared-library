def String call() {
    fullurl = env.GIT_URL
    def extrepo = fullurl.tokenize('/')[-1]
    def proj = fullurl.tokenize('/')[-2]
    def repo = extrepo.tokenize('.')[-2]
        
        myurl =  "http://v-git.inr.rd.hpicorp.net:7990/rest/api/1.0/projects/${proj}/repos/${repo}/commits?until=develop&limit=0&start=0"
        def response = httpRequest authentication: '51482bc4-7bcb-49c9-8852-b778c3fd8fb4', ignoreSslErrors: true, url: myurl

        def jsonObj = readJSON text: response.content
        commitMessage = jsonObj.values[0].message
        println("commitMessage: " + commitMessage)

        pullRequestId = sh(script: "echo \"${commitMessage}\" | grep 'Merge pull request' | awk \'{ gsub(\"#\",\"\",\$4); print\$4 }\'", returnStdout: true).trim() //grep the message for the Merge line and get the PR number from the forth column

        println("pullRequestId is ${pullRequestId}")

      if (pullRequestId != null) {
          myurl =  "http://v-git.inr.rd.hpicorp.net:7990/rest/api/1.0/projects/${proj}/repos/${repo}/pull-requests/${pullRequestId}"
          def responsePR = httpRequest authentication: '51482bc4-7bcb-49c9-8852-b778c3fd8fb4', ignoreSslErrors: true, url: myurl
          println('Status: '+responsePR.status)
          println('Response: '+responsePR.content)
          def jsonObjPR = readJSON text: responsePR.content
          descriptionPR = jsonObjPR.description
		  TitlePR = jsonObjPR.title
          println("description: " + descriptionPR)
		  println("Title: " + TitlePR)
          if (descriptionPR == null)
          {
              descriptionPR = "No description"
          }
          if (TitlePR == null)
          {
              TitlePR = "No title"
          }
          writeFile(file: 'descriptionPR.txt', text: descriptionPR) // dunp description into file
          writeFile(file: 'TitlePR.txt', text: TitlePR) // dump title into file
          sh "cat descriptionPR.txt"
          sh "cat TitlePR.txt"
      } else {
          println("pullRequestId seems to be null")
      }

}
	
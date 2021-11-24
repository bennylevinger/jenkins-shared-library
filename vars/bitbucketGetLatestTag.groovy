//http://v-git:7990/rest/api/1.0/projects/S6/repos/chartpress/tags?filterText=1.0&orderBy=modification&limit=1
def String call(tagfilter) {
     myurl =  "http://v-gitv-git.inr.rd.hpicorp.net:7990/rest/api/1.0/projects/S6/repos/chartpress/tags?filterText=${tagfilter}&orderBy=modification&limit=1"
          def response = httpRequest authentication: '51482bc4-7bcb-49c9-8852-b778c3fd8fb4', ignoreSslErrors: true, url: myurl
          println('Status: '+response.status)
          println('Response: '+response.content)
          def jsonObj = readJSON text: response.content
          println('displayId: '+jsonObj.displayId)
          
          mytag = jsonObj.values[0].displayId

          return mytag
}
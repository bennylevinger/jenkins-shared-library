#!/usr/bin/env groovy

def call(String tag,String registry,String workspace, String push , String Dockerfile) {
 if (env.OS == null)
 {
    echo "OS is ${env.OS}"
    SUDOVAR = 'printf "ubuntu\\n" | sudo -S'
 } else
 {
     echo "OS is ${env.OS}"
	 if (env.OS.toLowerCase().contains('windows')) {
		SUDOVAR = ""
	  } else {
		SUDOVAR = 'printf "ubuntu\\n" | sudo -S'
	  } 
 }
    echo "building and pushing version: ${tag}"
  sh """
    cd ${workspace}
    echo building image ${tag}
    ${SUDOVAR} docker build --build-arg REGISTRY_URL=v-nugetsrv3.inr.rd.hpicorp.net -t ${registry}/${tag} -f ${Dockerfile} .
  """
  if ("${push}" == 'true'){
    echo "${push} comes to shove"
    sh "${SUDOVAR} docker push ${registry}/${tag}"
  }

}
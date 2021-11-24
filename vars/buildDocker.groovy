#!/usr/bin/env groovy

def call(String tag,String folder, String push , String Dockerfile) {
  writeFile file: "globalvalues.groovy", text: libraryResource("globalvalues.groovy")
  def values = readProperties file: "globalvalues.groovy"
  dockerRegistry = values['dockerRegistry']
    echo "building and pushing version: ${tag}"
  sh """
    cd ${folder}
    echo building image ${tag}
    docker build -t ${dockerRegistry}/${tag} .
  """
  if ("${push}" == 'true'){
    echo "${push} comes to shove"
    sh " docker push ${dockerRegistry}/${tag}"
  }

}
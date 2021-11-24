#!/usr/bin/env groovy

def call(String component, String CurrentVersion, String registry) {
  echo "building and pushing version: ${CurrentVersion}"
  bat """

docker build --build-arg REGISTRY_URL=v-nugetsrv3.inr.rd.hpicorp.net -t ${registry}/${component}:${CurrentVersion} .

docker push ${registry}/${component}:${CurrentVersion} 

cd ..

  """
}
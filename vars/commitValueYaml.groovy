#!/usr/bin/env groovy

def call(String GIT_URL) {
   
    echo "commit value "
	echo "GIT_URL ${GIT_URL}"
	   GIT_URLWithoutHttpPrefix = sh(script: "echo ${GIT_URL} | sed \'s|http://||g\'", returnStdout: true).trim()
                         withCredentials([usernamePassword(credentialsId: '51482bc4-7bcb-49c9-8852-b778c3fd8fb4',
                                usernameVariable: 'username',
                                passwordVariable: 'password')]){
                            sh("""
                            git config --global user.email "indigodevops@hp.com"
                            git config --global user.name "admincm"
                            git add values.yaml
                          
                            git commit -m "newver ${env.ImageVersion}"
                            git push http://$username:$password@$GIT_URLWithoutHttpPrefix #Push the new text
                             """)}
}

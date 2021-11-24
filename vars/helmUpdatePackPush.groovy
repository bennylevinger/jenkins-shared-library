#!/usr/bin/env groovy
@Grab('org.yaml:snakeyaml:1.17')
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.DumperOptions
import static org.yaml.snakeyaml.DumperOptions.FlowStyle.BLOCK

def call(String chartName, String ImageName,  String ImageVersion) {
  writeFile file: "globalvalues.groovy", text: libraryResource("globalvalues.groovy")
  def values = readProperties file: "globalvalues.groovy"
  helmRepository = values['helmRepository']
  echo "helmRepository  is  ${helmRepository}"

    newVer = sh(script: "grep ^version ${chartName}/Chart.yaml | awk \'{print\$2}\' | awk -F. \'{\$NF = \$NF + 1;} 1\' | sed \'s/ /./g\'", returnStdout: true).trim()
    sh """
    pwd
    echo "Bunmping the old version number in ${chartName}/Chart.yaml with ${newVer}"
    sed -i "s|^version: .*\$|version: "$newVer"|" "${chartName}"/Chart.yaml #Replaces old ver with new
    """
  // modify and save yaml https://stackoverflow.com/questions/34668930/groovy-load-yaml-file-modify-and-write-it-in-a-file
  def chartYaml = readYaml file: "${chartName}/Chart.yaml" // read  Chart yaml
  def valuesYaml = readYaml file: "${chartName}/values.yaml" // read  values yaml
  ver = chartYaml.getAt('version')
  pressName = chartYaml.getAt('name')
  echo "Version is : ${ver}"
  echo "Name is ${pressName}"
  //  Chart.yaml iterate dependencies look for the name of the chart that lounched this current call 
  def dependencies = chartYaml.dependencies //all dependencies
  dependencies.each { e -> // commands for each iteration
    echo "running commands for: component: ${e.getAt('name')} with version  type: ${e.getAt('version')} - Before"
    depName = e.getAt('name')
    if (depName == ImageName )
    {
      echo "Found dependency:  ${ImageName} with version : ${e.getAt('version')}"
      e.version=ImageVersion 
      echo "Change dependency:  ${ImageName} to version : ${e.getAt('version')}"
	    
    }
    
  } //end of dependencies
  filename = "${chartName}/Chart.yaml"
  writeFile file: filename, text:yamlToString(chartYaml)

  sh """
  if [ -f "${chartName}"/values.yaml ];
       then
         echo "updating ${chartName}/values.yaml"
         sed -i "s|installationName: .*\$|installationName: "${newVer}"|" "${chartName}"/values.yaml #Replaces old installationName with newVer
        else
           echo "File values.yaml does not exist skiping."	   
  fi	
  echo "Helm - update dependencies, pack and push . . ."
  ###helm repo add PressCharts http://v-nugetsrv3.inr.rd.hpicorp.net:8624/helm/PressCharts
  helm dependency update ${chartName}
  helm package "${chartName}" #For packaging, name is the name of the directory
  curl "${helmRepository}" --user Admin:Admin --upload-file "${chartName}"-"${newVer}".tgz
  """

// Git bash with user and pass (was git cloned with http). If git was cloned with ssh, use sshagent([ (see: https://stackoverflow.com/questions/38769976/is-it-possible-to-git-merge-push-using-jenkins-pipeline )
  GIT_URLWithoutHttpPrefix = sh(script: "echo ${GIT_URL} | sed \'s|http://||g\'", returnStdout: true).trim()
  withCredentials([usernamePassword(credentialsId: '51482bc4-7bcb-49c9-8852-b778c3fd8fb4',
          usernameVariable: 'username',
          passwordVariable: 'password')]){
    sh("""
    git config --global user.email "indigodevops@hp.com"
    git config --global user.name "admincm"
    git add ${chartName}/Chart.yaml
	if [ -f "${chartName}"/values.yaml ];
       then
		git add ${chartName}/values.yaml
	fi	
    git commit -m "newver ${newVer} set by helmUpdatePackPush lib"
    git push http://$username:$password@$GIT_URLWithoutHttpPrefix #Push the new text
    git tag -f "${newVer}" ### || git push --delete origin "${newVer}" && git tag -f "${newVer}"
    git push http://$username:$password@$GIT_URLWithoutHttpPrefix "${newVer}" #Push the new tag
    """)
  }

  // helm update helm pack helm push
  // need commit tag and push by newver 

  //writeYaml file: filename, data: chartYaml

}
@NonCPS
String yamlToString(Object data){
    def opts = new DumperOptions()
    opts.setDefaultFlowStyle(BLOCK)
    return new Yaml(opts).dump(data)
}
#!/usr/bin/env groovy

def call(String yamlFileName) {
  writeFile file: "globalvalues.groovy", text: libraryResource("globalvalues.groovy")
  def values = readProperties file: "globalvalues.groovy"
  nugetRegistry = values['nugetRegistry']
  echo "nugetRegistry  is  ${nugetRegistry}"
  def yaml = readYaml file: "$yamlFileName" // read local values yaml
  ver = "${yaml.getAt('majorVersion')}.${yaml.getAt('minorVersion')}"
  verfull = "${yaml.getAt('majorVersion')}.${yaml.getAt('minorVersion')}.${BUILD_NUMBER}"
  if (env.pullRequestId != null) { // Is this a pull request use dev values
     return 0
	} 
 
  writeFile file: "tmp.txt", text: nugetRegistry
  writeFile file: "ver.txt", text: verfull

       
  notifyOnSuccess("press-ui :${ver}.${BUILD_NUMBER} nuget was published successfuly" )

}
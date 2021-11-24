#!/usr/bin/env groovy

def call(String yamlFileName) {
  writeFile file: "globalvalues.groovy", text: libraryResource("globalvalues.groovy")
  def values = readProperties file: "globalvalues.groovy"
  nugetRegistry = values['nugetRegistry']
  echo "nugetRegistry  is  ${nugetRegistry}"
  def yaml = readYaml file: "$yamlFileName" // read local values yaml
  ver = "${yaml.getAt('majorVersion')}.${yaml.getAt('minorVersion')}"
  if (env.pullRequestId != null) { // Is this a pull request use dev values
     return 0
	} 
  writeFile file: "tmp.txt", text: nugetRegistry
  writeFile file: "ver.txt", text: ver

  echo "Creating nuget"
  bat '''
               set /p REG=<tmp.txt
			   set /p ver=<ver.txt
			  del  /Q tmp.txt
			  del  /Q ver.txt
                xcopy /S /Y \\\\iihome\\compsversions\$\\ReleaseArea\\PRODUCTION\\PressUIHost\\%ver%.%BUILD_NUMBER%\\*.*  .\\pressuihost\\
			  \\\\iihome\\compsversions\$\\Utilities\\S6\\nuget.exe  pack -Version %ver%.%BUILD_NUMBER%	pressuihost.nuspec
			  \\\\iihome\\compsversions\$\\Utilities\\S6\\nuget.exe  push pressuihost.%ver%.%BUILD_NUMBER%.nupkg -apiKey Indigo2018 -Source %REG% -timeout 50000
			  if NOT "%errorlevel%" == "0"	exit 1	
                '''        
  notifyOnSuccess("pressuihost :${ver}.${BUILD_NUMBER} nuget was published successfuly" )

}
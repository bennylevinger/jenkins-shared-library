def call(String PROJ_PATH) {
    echo "Project location  is ${PROJ_PATH}"
    def version = "1.0"
    writeFile file: "globalvalues.groovy", text: libraryResource("globalvalues.groovy")
    def values = readProperties file: "${PROJ_PATH}\\globalvalues.groovy"
	def yaml = readYaml file: "values.yaml"
	
    //def value1 = ''
    LocalenvVals = values
	Registry = LocalenvVals['Registry']
	if ( yaml.getAt('isDocker') == true) {
        prefix = yaml.getAt('dockerImagePrefix')
		major = yaml.getAt('majorVersion')
		minor = yaml.getAt('minorVersion')
		imageTag = "${prefix}:${major}.${minor}.${BUILD_NUMBER}"
		imageTag = imageTag.replaceAll("\"","");
		echo "imageTag is ${imageTag}"
    }
	
}
return this

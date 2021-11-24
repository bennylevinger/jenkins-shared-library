#!/usr/bin/env groovy

def call(String chartFolder, String dockerImageName,  String dockerImageVersion , boolean push) {
  writeFile file: "globalvalues.groovy", text: libraryResource("globalvalues.groovy")
  def values = readProperties file: "globalvalues.groovy"
  chartcomponent = dockerImageName.replaceAll("/","-")
  helmRepository = values['helmRepository']
  echo "helmRepository  is  ${helmRepository}"
  
  
    newVer = dockerImageVersion
    sh """
    pwd
    echo "${newVer}"
    sed -i "s|^version: .*\$|version: "$newVer"|" "${chartFolder}"/Chart.yaml #Replaces old ver with new
    """
    sh """
	if [ -f "${chartFolder}"/values.yaml ];
       then
           sed -i "s|tag: .*\$|tag: "${newVer}"|" "${chartFolder}"/values.yaml
	      echo "updating values.yaml."
       else
           echo "File values.yaml does not exist, skiping."	   
	  fi	   
      """

if (push == true) {
  getPR() //creates descriptionPR.txt and TitlePR.txt
    sh """
echo "Creating /templates/NOTES.txt . . ."
    cat <<EOF > "${chartFolder}"/templates/NOTES.txt
    You have just installed {{.Chart.Name}}. version "${newVer}" .
    The latest PR details are:
    EOF
    """
    sh """
    cat TitlePR.txt >> "${chartFolder}"/templates/NOTES.txt
    echo "" >> "${chartFolder}"/templates/NOTES.txt
    cat descriptionPR.txt >> "${chartFolder}"/templates/NOTES.txt
    """
}
    sh """
    
     echo "update dependencies, pack and push . . ."
	 echo "dockerImageName is ${dockerImageName}"
     helm dependency update ${chartFolder}
	 helm package "${chartFolder}" #For packaging, name is the name of the directory
     if [ "${push}" = 'true' ]; then 
		    curl "${helmRepository}" --user Admin:Admin --upload-file "${chartcomponent}"-"${newVer}".tgz
		 fi
    
    """

}

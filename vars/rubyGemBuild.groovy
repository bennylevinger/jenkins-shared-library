#!/usr/bin/env groovy

def call(String folder , String gemspecFile,String ver,boolean push) {
  newver="\'${ver}\'"
  sh """
         cd ${folder}
		pwd
		ls
         echo "updating version in ${gemspecFile}.gemspec to ${newver}"

         echo sed -i "s|s.version         = .*\$|s.version         = "\"${newver}\""|" ${gemspecFile}.gemspec

         sed -i "s|s.version         = .*\$|s.version         = "\"${newver}\""|" ${gemspecFile}.gemspec
		gem build  ${gemspecFile}
    if [ "${push}" = 'true' ]; then 
		        gem push ${gemspecFile}-${ver}.gem --host http://15.17.201.53:8624/rubygems/Rgems
    fi
   """
   


	
}

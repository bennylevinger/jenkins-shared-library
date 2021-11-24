#!/usr/bin/env groovy

import org.apache.commons.lang.StringUtils

def call(String filter_string, int occurrence) {
    def logs = currentBuild.rawBuild.getLog(10000).join('\n')
    int count = StringUtils.countMatches(logs, filter_string);
    if (count > occurrence -1) {
        currentBuild.result='UNSTABLE'
    }
}


pipeline {
    agent {
		label "v-comp-s4-bld15"
	}
	options { timestamps() }
	parameters {
        string(defaultValue: '', description: 'Branch to build', name: 'BRANCH_NAME')
        string(defaultValue: 'new_nuget_ver', description: 'git branch of buildmgr', name: 'branch')
		string(defaultValue: '1.2.3.4', description: 'iDO Version', name: 'MYVERSION')
		
        string(defaultValue: "\\\\iihome\\compsversions\$\\Utilities\\S6\\nuget.exe", description: 'nuget.exe', name: 'nuget')
		string(defaultValue: "C:\\Program Files (x86)\\Microsoft Visual Studio\\2017\\Enterprise\\MSBuild\\15.0\\Bin\\MSBuild.exe", description: 'MSBuild Path', name: 'msbuild')
		string(defaultValue: "C:\\Program Files\\dotnet\\dotnet.exe", description: 'Dotnet Path', name: 'dotnet')
		string(defaultValue: "\\\\IIHOME\\CompsVersions\$\\FeatureBranches", description: 'Feature branch properties file location', name: 'FEATURE_BRANCHES_LOCATION')
		string(defaultValue: "\\\\IIHOME\\CompsVersions\$\\ReleaseArea\\PRODUCTION", description: 'Path to Production folder on iihome', name: 'IIHOME_PROD')
		string(defaultValue: 'Prod', description: 'build optimization environment', name: 'Environment')
		string(defaultValue: '', description: 'WHere to merge the branch', name: 'TO_BRANCH')
		
    }
	stages { 
		
		stage ('Git Clone') {            
            steps {
			    
				echo "Cloning buildmgr repository. Branch Name: ${params.branch}" 
				checkout([$class: 'GitSCM', branches: [[name: "*/${params.branch}"]], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'LocalBranch', localBranch: "${params.branch}"], [$class: 'RelativeTargetDirectory', relativeTargetDir: 'buildmgr'], [$class: 'WipeWorkspace']], submoduleCfg: [], userRemoteConfigs: [[credentialsId: '86954733-ba6c-4df3-bc64-c73872a30c9e', url: 'ssh://git@v-git.indigo.co.il:7999/s6/buildmgr.git']]])
				
			}
		}
			stage ('Clean Env') {            
            steps {
                echo "Clean Old  Build" 
				bat '''
				 dotnet nuget locals --clear all
				if exist temp RmDir /S /Q temp
				if exist temp  RmDir /S /Q temp

				if exist temp RmDir /S /Q temp
				
				if exist Temp RmDir /S /Q Temp
               
				'''
				}
			}
		stage ('Copy And Extract iVision') {            
            steps {
			bat '''
			mkdir temp
			xcopy %IIHOME_PROD%\\ivision\\S-IV\\%MYVERSION%\\*.*  temp\\  /Y
			cd temp
			"C:\\Program Files\\7-Zip\\7z.exe" x *.zip -o*
			 if exist iVisionRelease\\Bin\\iVision\\MLServer  RMDIR  /S /Q  iVisionRelease\\Bin\\iVision\\MLServer
				'''
			}
			
			
		}
			stage ('Pack iVisionDev') {            
            steps {
			bat '''
		        xcopy buildmgr\\iVision\\*.* temp\\iVisionDev
		        cd temp\\iVisionDev
		        %nuget% pack iVisionDev.nuspec -Version "%MYVERSION%" 
			
				'''
			}
			
			
		}
			stage ('Pack iVisionRelease') {            
            steps {
			bat '''
                DEL  /S /Q /F temp\\iVisionRelease\\*.pdb
			    xcopy buildmgr\\iVision\\*.* temp\\iVisionRelease
		        cd temp\\iVisionRelease
		        %nuget% pack iVisionRelease.nuspec -Version "%MYVERSION%" 
			
				'''
			}
			
			
		}
        stage ('Pack HP.Indigo.iVisionClient.Managed.Runtime') {            
            steps {
			bat '''
                if NOT exist temp\\iVisionClientS6 exit 0
                cd temp\\iVisionClientS6
                mkdir lib
                mkdir lib\\netcoreapp3.1
                MOVE *.* lib\\netcoreapp3.1\\
			    xcopy ..\\..\\buildmgr\\iVision\\HP.Indigo.iVisionClient.Managed.Runtime.nuspec .
		          
		        %nuget% pack HP.Indigo.iVisionClient.Managed.Runtime.nuspec -Version "%MYVERSION%" 
			
				'''
			}
			
			
		}
		stage ('Publish iVisionDev') {            
            steps {
			bat '''
			cd temp\\iVisionDev\\
		   REM      \\\\iihome\\compsversions\$\\Utilities\\S6\\nuget.exe push iVisionDev.%MYVERSION%.nupkg -apiKey Indigo2018 -Source http://15.17.200.185:8624/nuget/S6Nugets/ -timeout 50000 
		       
				'''
			}
			
			
		}
			stage ('Publish iVisionRelease') {            
            steps {
			bat '''
		       
				cd temp\\iVisionRelease\\
		REM 		\\\\iihome\\compsversions\$\\Utilities\\S6\\nuget.exe push iVisionRelease.%MYVERSION%.nupkg -apiKey Indigo2018 -Source http://15.17.200.185:8624/nuget/S6Nugets/ -timeout 50000 
				'''
			}
			
			
		}
			
	   stage ('Publish HP.Indigo.iVisionClient.Managed.Runtime') {            
            steps {
			bat '''
             if NOT exist temp\\iVisionClientS6 exit 0
			cd temp\\iVisionClientS6\\
		   REM      \\\\iihome\\compsversions\$\\Utilities\\S6\\nuget.exe push HP.Indigo.iVisionClient.Managed.Runtime.%MYVERSION%.nupkg -apiKey Indigo2018 -Source http://15.17.200.185:8624/nuget/S6Nugets/ -timeout 50000 
		       
				'''
			}
			
			
		}
	
		
	}
	post {
	 always {
     echo 'One way or another, I have finished'
            
              }
        success {
            echo 'I succeeeded!'
        		//	emailext attachLog: true, body: "${currentBuild.currentResult}: Job ${env.JOB_NAME} build ${env.BUILD_NUMBER}\n More info at: ${env.BUILD_URL}" ,subject:  "ivisio Nugets were updated to ${env.MYVERSION}" ,to: '$EMAIL,indigodevops@hp.com,alon.fital1@hp.com,benny.levinger1@hp.com'
               emailext attachLog: true, body: "${currentBuild.currentResult}: Job ${env.JOB_NAME} build ${env.BUILD_NUMBER}\n More info at: ${env.BUILD_URL}" ,subject:  "iVision Nugets (DEV/Release/HP.Indigo.iVisionClient.Managed.Runtime) ${env.MYVERSION} were pushed to nuget server" ,to: 'benny.levinger1@hp.com,mor.shaked@hp.com , inbal.vaserman@hp.com'
          }
        unstable {
            echo 'I am unstable :/'
               }
        failure {
            echo 'I failed :('
			    //    			emailext attachLog: true, body: "${currentBuild.currentResult}: Job ${env.JOB_NAME} build ${env.BUILD_NUMBER}\n More info at: ${env.BUILD_URL}" ,subject:  "SDK Nugets FAILED to updated version" ,to: 'benny.levinger1@hp.com,mor.shaked@hp.com , inbal.vaserman@hp.com'

                      }
        changed {
             echo 'Things were different before...'
            }
    
       
	}
}
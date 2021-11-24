#!/usr/bin/env groovy

def call(String CHANGE_TARGET,String BUILD_NUMBER,String workspace, String CHANGE_FORK) {
  echo "Mergin Pull request: From ${BUILD_NUMBER} To ${CHANGE_TARGET}"
  
  
  bat '''
                if "%CHANGE_TARGET%" == "" echo "NO Target Branch no merge"  & goto skip
					SET
					echo "ID :  %CHANGE_ID%"
					echo "BRANCH : %CHANGE_BRANCH%"
					echo "TARGET : %CHANGE_TARGET%"
					for /f "tokens=4 delims=/ " %%G IN ("%CHANGE_URL%") DO echo "PROJECT is  %%G" & SET CHANGE_PROJECT=%%G
					for /f "tokens=6 delims=/ " %%G IN ("%CHANGE_URL%") DO echo "REPO is  %%G" & SET CHANGE_REPO=%%G
					echo %CHANGE_PROJECT%
					echo %CHANGE_REPO%
					cd %workspace%
					echo on
					set
					SET NOT_BO=Yes
					
										
					SET Automerge=Yes
					SET CREATE_SA=No
					SET COMPILATION_ONLY=No
					SET Run_On_Hardware=No
					SET REBASE=No
					SET TO_BRANCH=%CHANGE_TARGET%
					SET NO_JIRA_ID=Yes
					SET USE_RLEASE_BRANCH_FROM_ENV=Yes
					echo Automerge=%Automerge%
					
					
					\\\\IIHOME\\CompsVersions\$\\FeatureBranches\\Build\\S6\\FBStarter.exe %CHANGE_BRANCH% S6 1.0.%BUILD_NUMBER% CheckNoMerge No %CHANGE_REPO%
					
					if %errorlevel% == -1 echo FAIL>merg.txt & goto errorfound
					
					if %errorlevel% == -2 echo FAIL>merg.txt & exit 1
					
					if %errorlevel% == -5 echo FAIL>merg.txt & exit 1
					
					if %errorlevel% == -6 echo FAIL>merg.txt & exit 1
					

					echo SUCCESS>merg.txt                       
					
					echo off
					
					:skip
					exit 0
					
					:errorfound
					
					exit 1
					
                '''        
  
}
#!/usr/bin/env groovy

def call() {
    GetPullRequestVersion()
    echo "We will do this always"
    for(String line : currentBuild.rawBuild.getLog(10000)){
										
									   if ( line.contains("Failed to merge pull request"))
											{
											     echo line
												 error("Build failed because of this and that..")    
											}
							           }    
		                      
}

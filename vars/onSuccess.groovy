#!/usr/bin/env groovy

def call() {
    if (env.pullRequestId == null) {
        jiraSetMergeFlag()

    }else
	{
	        
	        def PullReqVer = sh(script: "cat MyPullReqVer.groovy", returnStdout: true).trim()
	        
			println("Pull Request Version from file is : " + PullReqVer)
			MergePullRequest(PullReqVer)
	}
    echo "build was  finished successfully. :)"
}

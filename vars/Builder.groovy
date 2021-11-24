def call(String yamlFileName) {
  //saving local file from the shared lib resources dir
  writeFile file: "globalvalues.groovy", text: libraryResource("globalvalues.groovy")
  def values = readProperties file: "globalvalues.groovy"

  if (env.pullRequestId != null) { // Is this a pull request use dev values
    String MyPullReqVer = GetPullRequestVersion()
	writeFile file: "MyPullReqVer.groovy", text: MyPullReqVer
	//def PullReqVerVars = readProperties file: "MyPullReqVer.groovy"
	PullReqVer = sh(script: "cat MyPullReqVer.groovy", returnStdout: true).trim()
	println("Pull Request Version from file is : " + PullReqVer)
    npmRegistry = values['npmDevRegistry']
    dockerRegistry = values['dockerRegistry']
    publishDocker = false // we only build docker without publishing
	  createGitTag = false
	  sendMail = false
    echo "we are running Pullrequest"
	  copy = false
	  verifyPullRequestTitle()
  } else {
    npmRegistry = values['npmRegistry']
    dockerRegistry = values['dockerRegistry']
    publishDocker = true // we  build docker and publish it
	  createGitTag = true // Create Tag on build with versions
	  sendMail = true
	  copy = true
    echo "we are running on a release branch"
  }
  echo "npm registry is  ${npmRegistry}"
  echo "Docker registry is  ${dockerRegistry}"
  sh 'printenv'
  http_proxy = values['http_proxy']
  echo "npm proxy is  ${http_proxy}"
  def yaml = readYaml file: "$yamlFileName" // read local values yaml
  def componentYaml = yaml.project_components //vars for just the project
  reg = yaml.getAt('registry')
  ver = "${yaml.getAt('majorVersion')}.${yaml.getAt('minorVersion')}"
  fullVersion = "${ver}.${BUILD_NUMBER}"
  imageName = yaml.getAt('dockerImagePrefix')
  notifyOnStart()
  updateChartPress = false
  componentYaml.each { e -> // commands for each iteration
    echo "running commands for: component: ${e.getAt('component')} with build type: ${publishDocker}"
    comp = e.getAt('component') //This var will set the folder to cd into for each component. Usually it is the component name.
    update = e.getAt('update')
    switch ("${e.getAt('buildType')}") {
      case "npm":
      echo "BUILDTYPE IS NPM"
	  if (update != null)
	  {
	      updatePackage()
	  }
        buildNPM("${e.getAt('doPublish')}")
        break

      case "npmmake":
      echo "BUILDTYPE IS npmmake"

        npmmake("${comp}","${npmRegistry}")
        break
      case "localization":
            echo "BUILDTYPE IS localization"

            localization()
            if (env.pullRequestId != null)
            {
                echo "only build nuget - no push"
                sh """
                dotnet restore PressUILocalizationContent.csproj
                dotnet pack PressUILocalizationContent.csproj  --no-build --no-restore --output . -p:PackageVersion=${fullVersion}
                """
            }else
            {
                echo "only build nuget and push"
                sh """ 
                   dotnet restore PressUILocalizationContent.csproj
                   dotnet pack PressUILocalizationContent.csproj  --no-build --output . -p:PackageVersion=${fullVersion}
                   dotnet nuget push press-ui-localization.${fullVersion}.nupkg   -k Indigo2018 -s http://15.17.203.202:8624/nuget/S6Nugets/
               """
            }
            break
      case "copyBeforeDocker":
      echo "BUILDTYPE IS COPY BEFORE DOCKER"
		          trg = e.getAt('toTarget')
              sh """ 
              printf "ubuntu\\n" | sudo -S umount /tmp/copybeforedocker/ -lf || true
              df -h 
              printf "ubuntu\\n" | sudo -S mkdir -p /tmp/copybeforedocker
              printf "ubuntu\\n" | sudo -S mount -t cifs  -o username=cibuild,password=hpindigo2010,domain=inr "${e.getAt('copyFromSource')}" /tmp/copybeforedocker
              df -h
              printf "ubuntu\\n" | sudo -S cp -a /tmp/copybeforedocker/ . 
              ls
              pwd
              """


        break





      case "docker":
      echo "BUILDTYPE IS DOCKER"
        tag = "${yaml.getAt('dockerImagePrefix')}:${fullVersion}" // e.g: v-nugetsrv3.inr.rd.hpicorp.net:8624/kedem/library/indigo-content-base:1.0.1
        try {
          dockerFile = e.getAt('dockerFile') // get dockerFile path if nothing there use default Dockerfile
          echo "Got dockerfile from Yaml dockerFile is ${dockerFile}"
        } catch(Exception ex)
        {
                println("Exception: ${ex}")
          echo "Use defaul dockerfile :- ${dockerFile}"
          dockerFile = "Dockerfile"
            }
        if (dockerFile == null)
        {
          dockerFile = "Dockerfile"
        }
        if (dockerFile == "")
        {
          dockerFile = "Dockerfile"
        }
        echo "dockerFile is ${dockerFile}"
            buildDocker("${tag}","${dockerRegistry}","${comp}","${publishDocker}" ,"${dockerFile}")
        if (sendMail)
            {
            notifyOnSuccess("${yaml.getAt('dockerImagePrefix')}:${fullVersion} was created successfuly" )
            }
            break
	 case "copy":
    echo "BUILDTYPE IS COPY"
	     src = e.getAt('source')
		 trg = e.getAt('target')


		 if (copy == true)
		 if (copy == true)
		 {
             echo "copying ..."
		  buidCopy(src,trg)
		 }
		  break
    case "nuget":
            echo "BUILDTYPE IS NUGET"
            if (env.pullRequestId != null)
            {
                echo "only build nuget - no push"
                sh """
                rm *.nupkg || true
                dotnet restore PressUIContent.csproj
                dotnet pack PressUIContent.csproj  --no-build --no-restore --output . -p:PackageVersion=${fullVersion}
                """
            }else
            {
            echo "only build nuget and push"
                sh """ 
                   dotnet restore PressUIContent.csproj
                   dotnet pack PressUIContent.csproj  --no-build --output . -p:PackageVersion=${fullVersion}
                   dotnet nuget push press-ui.${fullVersion}.nupkg   -k Indigo2018 -s http://15.17.203.202:8624/nuget/S6Nugets/
				   rm -f press-ui.${fullVersion}.nupkg || true
               """
            }
            break

	case "CopyWin":
    echo "BUILDTYPE IS COPYWIN"
	     src = e.getAt('source')
		 trg = e.getAt('target')
         docopy = e.getAt('docopy')
            echo "docopy ${docopy}"

            if (docopy == true)
            {
                nover = true
                copy = true
            }else
            {
                nover = false
            }
		 if (copy == true)
		 if (copy == true)
		 {
             if (nover == true) {
                 buidCopyWinNoVer(src,trg)
             }
              else {
                 buidCopyWin(src,trg)
             }


		 }
		  break
   case "chart":
        echo "BUILDTYPE IS CHART"
        componentHelmUpdatePackPush(comp, "${yaml.getAt('dockerImagePrefix')}" ,"${fullVersion}",publishDocker)
        doUpdate = e.getAt('updateChartPress')
        if (doUpdate == "false")
        {
          updateChartPress = false
        }else
        {
          updateChartPress = true
        }

	    break
    case "gem":
        echo "BUILDTYPE IS GEM"
        rubyGemBuild(comp, "${e.getAt('gemspec')}","${fullVersion}" ,publishDocker)
         if (sendMail)
         {
            notifyOnSuccess("${e.getAt('gemspec')}-${ver} gem was published successfuly" )
         }
	    break
   case "overrideversion":
        echo "BUILDTYPE IS OVERRIDEVERSION"
        echo "fullVersion before change: ${fullVersion}"
        fullVersion = e.getAt('version')
        echo "fullVersion changed to: ${fullVersion}"
	    break
   case "getnuget":
        echo "BUILDTYPE IS GETNUGET"
        nugetname = yaml.getAt('name')
        nugetver = e.getAt('version')
        nugettarget = e.getAt('target')
        echo "Call installNuget(${nugetname},${nugetver},${nugettarget}) "
        installNuget(nugetname,nugetver,nugettarget)
	    break
   case "movetocontent":
        echo "BUILDTYPE IS MOVECONTENT"
        nugetname = yaml.getAt('name')
        nugetver = e.getAt('version')
        from = e.getAt('from')
        to = e.getAt('to')
        echo "Call mvToContent(${nugetname},${nugetver},${from},${to}) "
        mvToContent(nugetname,nugetver,from,to)
	    break
   default:
        error "Please check the buildType in your yaml file. The buildType ${e.getAt('buildType')} is not listed. No build will be made :-( "
        break
    } // end of case
  } //end off component loop
  echo "End of Components LOOP"
  if ((publishDocker == true) && (updateChartPress == true))
  {
     echo "Post switch Calling getBranch()"
     branchname = getBranch()
     echo "Post switch Calling getMail()"
     email = getMail()
     echo "send parameters to build ImageVersion: ${fullVersion} ImageName: ${imageName} BRANCH_NAME: ${branchname} EMAIL: ${email} "
     build job: '000.indigo-chart-press', wait: true,propagate: true, parameters: [string(name: 'ImageVersion', value: "${fullVersion}" ),string(name: 'ImageName', value: imageName ),string(name: 'branch', value: branchname.trim() ),string(name: 'EMAIL', value: email )  ]
  }
  tag = "${fullVersion}"
  gitTag(tag)
  GetPullRequestVersion()
} // end of call

// Functions:
def buildNPM(String Publish) {
    sh """
       cd "${comp}"
       pwd
       sleep 30  
     
       npm config set registry  ${npmRegistry}
       npm set progress=false
         npm prune 
	     npm i
	  #   npm i rxjs
       #npm i typescript
       #npm i jest
	   npm rebuild node-sass
       npm run testCI
       npm run build
       #npm version patch #Per instructions: https://docs.npmjs.com/cli/v6/commands/npm-version
       bump ${fullVersion}
       if [ "${Publish}" = 'true' ]; then npm publish; fi
       """
}
def localization() {
    sh """
       cd "${comp}"
       pwd
       sleep 10  
       npm run build-translation
      
       """
}
def gitTag(String tag) {
    sh """
	    echo "applying git tag ${tag}"
         if [ "${createGitTag}" = 'true' ]; then 
		   git tag -f -a -m "Build "  ${tag}; 
		   git push -f --tags 
		fi
       """
}
def updatePackage() {


      String[] str;
      str = update.split(',');

      for( String values : str )
      runUpdateNpm(values,comp)

}
def buidCopy(String src,String trg) {
    SUDOVAR = 'printf "ubuntu\\n" | sudo -S'
    sh """
	  ${SUDOVAR} mkdir ${trg}/${fullVersion}
	  ${SUDOVAR} cp -R ${src}/. ${trg}/${fullVersion}
	    
       """
}
def buidCopyWin(String src,String trg) {

    bat """
	   mkdir ${trg}\\${fullVersion}
	   xcopy /Y /E ${src}\\* ${trg}\\${fullVersion}\\
	    
       """
}
def buidCopyWinNoVer(String src,String trg) {

    bat """
	   
	   xcopy /Y /E ${src}\\* ${trg}\\
	    
       """
}
def runUpdateNpm(String pack,String dir) {
    sh """
	  cd "${dir}"
	  npm i -P -E  ${pack}@${fullVersion}
       """
}



//def buildDocker() is in a different groovy file
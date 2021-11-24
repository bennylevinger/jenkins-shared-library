#!/usr/bin/env groovy

def call(String nugetname,String nugetver,String nugettarget) {
   
    echo "running nuget install for ${nugetname} in version ${nugetver} in folder ${nugettarget}"
	writeFile file: "nugetname.txt", text: nugetname
    writeFile file: "nugetver.txt", text: nugetver
    writeFile file: "nugettarget.txt", text: nugettarget

	bat '''
	set /p nugetname=<nugetname.txt
	del  /Q nugetname.txt
	set /p nugetver=<nugetver.txt
	del  /Q nugetver.txt
	set /p nugettarget=<nugettarget.txt
	del  /Q nugettarget.txt
	
	\\\\iihome\\compsversions$\\Utilities\\nuget.exe install %nugetname% -Version %nugetver% -Source http://15.17.203.202:8624/nuget/S6Nugets -OutputDirectory %nugettarget%
	'''
}

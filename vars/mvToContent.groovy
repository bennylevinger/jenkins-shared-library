def call(String nugetname,String nugetver,String from,String to) {
    
writeFile file: "nugetname.txt", text: nugetname
writeFile file: "nugetver.txt", text: nugetver
writeFile file: "from.txt", text: from
writeFile file: "to.txt", text: to

bat '''
	set /p nugetname=<nugetname.txt
	del  /Q nugetname.txt
	set /p nugetver=<nugetver.txt
	del  /Q nugetver.txt
	set /p from=<from.txt
	del  /Q from.txt
	set /p to=<to.txt
	del  /Q to.txt
	if not exist "%to%" mkdir "%to%"
	echo "moving  folder %nugetname%.%nugetver%\\%from%  %to%"
	move  /Y  %nugetname%.%nugetver%\\%from%  %to%
	'''
}

def String call(String comp ,String npmRegistry) {
   bat """
       cd "${comp}"
       pwd
        
       
       REM npm config set registry  "${npmRegistry}"
	   npm i
       echo "finish npm install"      
       """
	   
	   bat """
       cd "${comp}"
       pwd
	   echo "running npm brun make"
       npm run make
        echo "finished npm brun make"
       """
}
#!/usr/bin/env groovy

def String call(String comp,String ver) {
    def String SemVersion
    echo "orig ver is: ${ver}"
    echo "comp is: ${comp}xxx"
	 switch (comp) {
                        case "pc":
                            //// o,it the minor  (the first zero) X.0.X.X
                           
                            SemVersion = sh(script: "echo ${ver} | cut -d'.' -f 2 --complement", returnStdout: true).trim()
                            echo "SemVersion is ${SemVersion}"

                            break
                        case "efw":
                         //// o,it the minor  (the first zero) X.0.X.X
                          
                            SemVersion = sh(script: "echo ${ver} | cut -d'.' -f 2 --complement", returnStdout: true).trim()
                            echo "SemVersion is ${SemVersion}"

                            break

                        case "icfc":
                        //take only the first three X.X.X
                            SemVersion = sh(script: "echo ${ver} | cut -d'.' -f 4 --complement", returnStdout: true).trim()
                            echo "SemVersion is ${SemVersion}"
                            break

                        case "IdoRelease":
                        //take only the first three X.X.X
                            SemVersion = sh(script: "echo ${ver} | cut -d'.' -f 4 --complement", returnStdout: true).trim()
                            echo "SemVersion is ${SemVersion}"

                            break

                        case "ivisionrelease":
                        //take only the first three X.X.X
                           SemVersion = sh(script: "echo ${ver} | cut -d'.' -f 4 --complement", returnStdout: true).trim()
                           echo "SemVersion is ${SemVersion}"

                            break
                        
                        case "lphControllerRelease":
                            SemVersion = "${ver}"
                            break

                        case "InfraDev":
                            SemVersion = sh(script: "echo ${ver} | cut -d'.' -f 2 --complement", returnStdout: true).trim()
                            echo "SemVersion is ${SemVersion}"

                            break
                        
                        case "eTools":
                        //// omits the minor  (the first zero) X.0.X.X
                            SemVersion = sh(script: "echo ${ver} | cut -d'.' -f 2 --complement", returnStdout: true).trim()
                            echo "SemVersion is ${SemVersion}"
                            break

                        case "TFTP":
                        //// omits the minor  (the first zero) X.0.X.X
                            SemVersion = sh(script: "echo ${ver} | cut -d'.' -f 2 --complement", returnStdout: true).trim()
                            echo "SemVersion is ${SemVersion}"
                            
                            break

                        case "idrivers":
                            SemVersion = "${ver}"
                            break


                        case "PLC_KEDEM":
                            SemVersion = "${ver}"
                            break

                        default:
                            echo "nothing to do"
							SemVersion = "${ver}"
                            break
                    }
                    return SemVersion
}

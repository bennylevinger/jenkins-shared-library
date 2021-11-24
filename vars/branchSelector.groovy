def call(String GIT_BRANCH) {
    echo "gitbranch is ${GIT_BRANCH}"
    def version = "1.0"

    def values = readProperties file: "${env.WORKSPACE}\\values.groovy"
    //def value1 = ''
    envVals = values
    switch(GIT_BRANCH) {
        case "develop":
            env = "dev"
            echo "setting env to ${env}"
            value1 = values['DevValueloadedFromReasources']
            break
        case ["master", "Feature*".toString()]:
            env = "prod"
            echo "setting env to ${env}"
            value1 = values['ProdValueloadedFromReasources']
            break
        case "Feature*":
            env = "dev"
            echo "setting env to ${env}"
            value1 = values['FeatureValueloadedFromReasources']
            break
        default:
            value1 = "nothin"
            break
    }
    echo "The values for this env are ${value1} and ${env}"
}
return this
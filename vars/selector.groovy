#!/usr/bin/env groovy

def call(String componentname) {
    echo "componentname is ${componentname}"
    //def name = "${componentname}";
    switch (componentname) {
        case "compoA":
        case "compoB":
        case "compoC":
            env.tag = "tag1";
            break;
        case "compoD":
        case "compoE":
        case "compoF":
            env.Tag = "tag2";
            break;
        case null:
            System.out.println("There component name entered is not in the case list. Basa.");
            break;
        default:
            System.out.println("lo tov.");
            break;
    }
    echo "building and pushing version: ${env.Tag}"
}

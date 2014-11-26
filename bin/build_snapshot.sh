#!/bin/bash

function quit() {
    echo "ERROR: $*"
    exit 1
}

function getVersion() {
    mvn ${MVN_ARGS} org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version |grep "^[0-9]\+\\.[0-9]\+" 2>/dev/null
}

V=$(getVersion)
if [[ ${V} == *-SNAPSHOT ]]; then
    mvn clean test||exit 1
    ./bin/deploy.sh||quit "Unable to deploy"
else
    echo "${V} is not a snapshot version. Exiting"
    exit 1
fi

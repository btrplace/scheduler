#!/bin/bash

source bin/commons.sh

V=$(getVersion)
if [[ ${V} == *-SNAPSHOT ]]; then
    mvn clean test||exit 1
    ./bin/deploy.sh||quit "Unable to deploy"
    ./bin/push_javadoc.sh btrplace/apidocs-next.git
else
    echo "${V} is not a snapshot version. Exiting"
    exit 1
fi

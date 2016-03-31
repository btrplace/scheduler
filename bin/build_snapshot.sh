#!/bin/bash

source bin/commons.sh

V=$(getVersion)
if [[ ${V} == *-SNAPSHOT ]]; then
    mvn clean test||exit 1
    ./bin/deploy.sh > deploy.out 2>&1 ||err "  Unable to deploy" deploy.out
    ./bin/push_javadoc.sh btrplace/apidocs-next.git >javadoc.out 2>&1  || warn "  Unable to push the javadoc" javadoc.out
else
    echo "${V} is not a snapshot version. Exiting"
    exit 1
fi

#!/bin/bash

source bin/commons.sh

V=$(getVersion)
if [[ ${V} == *-SNAPSHOT ]]; then
    echo "** Testing **"
    mvn test
else
    echo "${V} is not a snapshot version. Exiting"
    exit 1
fi

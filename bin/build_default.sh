#!/bin/bash

source bin/commons.sh

V=$(getVersion)
if [[ ${V} == *-SNAPSHOT ]]; then
    echo "** Testing **"
    mvn test > test.out 2>&1 ||err "  Unstable build" test.out
else
    echo "${V} is not a snapshot version. Exiting"
    exit 1
fi

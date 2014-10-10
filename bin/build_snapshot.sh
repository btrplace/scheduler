#!/bin/bash

function quit() {
    echo "ERROR: $*"
    exit 1
}

mvn clean test||exit 1
./bin/deploy.sh||quit "Unable to deploy"
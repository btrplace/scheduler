#!/bin/bash
#Shell script to initiate the release
#The script creates a 'release' branch, set the version the pom.xml and the changelog then push
#The remainder of the process should be handled by the build_release.sh script.

function getVersionToRelease() {
    CURRENT_VERSION=`mvn ${MVN_ARGS} org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version | grep -v "\[INFO\]"`
    echo ${CURRENT_VERSION%%-SNAPSHOT}
}

git ls-remote --exit-code --heads origin release > /dev/null
if [ $? -eq 0 ]; then
    echo "Error: A release is already under progress"
    exit 1
fi

VERSION=$(getVersionToRelease)
git checkout -b release || exit 1

#Establish the version, maven side, misc. side
./bin/set_version.sh ${VERSION}
git commit -m "initiate release ${VERSION}" -a
git push origin release || exit 1
git checkout -
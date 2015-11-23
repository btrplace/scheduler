#!/bin/bash
#Script to notify the website about a release

function getVersion() {
	#blank execution as this command is very fragile and bug if there is sth to download
mvn ${MVN_ARGS} org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version > /dev/null
CURRENT_VERSION=`mvn ${MVN_ARGS} org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version | grep -v "\[INFO\]"`
echo ${CURRENT_VERSION%%-SNAPSHOT}
}

function guess() {
    v=$1
    if [[ $v == *-SNAPSHOT ]]; then
        echo ${v%%-SNAPSHOT}
    else
        echo "${v%.*}.$((${v##*.}+1))-SNAPSHOT"
    fi
}

function sedInPlace() {
	if [ $(uname) = "Darwin" ]; then			
		sed -i '' "$1" $2
	else
		sed -i'' "$1" $2
	fi
}

if [ $1 == "--next" ]; then
    CUR=$(getVersion)
    VERSION=$(guess ${CUR})
else
    VERSION=$1
fi
echo "New version is ${VERSION}"
#Update the poms
mvn versions:set -DnewVersion=${VERSION} -DgenerateBackupPoms=false||exit 1

sedInPlace "s%<version>.*</version>%<version>$VERSION</version>%"  README.md

d=`LANG=en_US.utf8 date +"%d %b %Y"`
REGEX="s%????*%${VERSION} - ${d}%"
sedInPlace "${REGEX}" CHANGES.md


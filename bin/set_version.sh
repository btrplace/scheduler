#!/bin/bash
#Script to notify the website about a release

function getVersion() {
    mvn -Dmaven.repo.local=/tmp/cache -q ${MVN_ARGS} org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version |grep "^[0-9]\+\\.[0-9]\+" 2>/dev/null
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
mvn -Dmaven.repo.local=/tmp/cache versions:set -DnewVersion=${VERSION} -DgenerateBackupPoms=false||exit 1

sedInPlace "s%<version>.*</version>%<version>$VERSION</version>%"  README.md

d=`LANG=en_US.utf8 date +"%d %b %Y"`
REGEX="s%????*%${VERSION} - ${d}%"
sedInPlace "${REGEX}" CHANGES.md


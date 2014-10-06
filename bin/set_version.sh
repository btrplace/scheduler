#!/bin/bash
#Script to notify the website about a release

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

if [ $1 == "--auto" ]; then
    VERSION=$(guess $2)
else
    VERSION=$1
fi
echo "New version is ${VERSION}"
#Update the poms
mvn versions:set -DnewVersion=${VERSION}
#README.md
sedInPlace "s%<version>.*</version>%<version>$VERSION</version>%"  README.md
d=`LANG=en_US.utf8 date +"%d %b %Y"`
REGEX="s%????*%${VERSION} - ${d}%"
sedInPlace "${REGEX}" CHANGES.md


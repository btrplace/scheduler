#!/bin/bash
#Script to notify the website about a release

function guess {
    v=$1
    #Contains snapshot, version will be the release

    #Otherwise, increment last number for the next SNAPSHOT version

}

function sedInPlace {
	if [ $(uname) = "Darwin" ]; then			
		sed -i '' "$1" $2
	else
		sed -i'' "$1" $2
	fi
}

if [ $# -ne 2 ]; then
    echo "Usage: $0 [site|code] version_number|--auto"
    exit 1
fi
if [ $1 == "--auto" ]; then
    VERSION=guess
else
    VERSION=$2
fi

#Update the poms
mvn versions:set -Dnewversion=${VERSION}
#README.md
sedInPlace "s%<version>.*</version>%<version>$VERSION</version>%"  README.md
d=`LANG=en_US.utf8 date +"%d %b %Y"`
REGEX="s%????*%${VERSION} - ${d}%"
sedInPlace "${REGEX}" CHANGES.md


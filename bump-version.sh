#!/bin/sh
#Script to upgrade the version number in some working files

if [ $# -ne 1 ]; then
    echo "Usage: $0 version_number"
    exit 1
fi
VERSION=$1

#The README.md
#Just an update of the version number
sed  -i '' "s%<version>.*</version>%<version>$VERSION</version>%"  README.md

#The CHANGES.md file
d=`LANG=en_US.utf8 date +"%d %b %Y"`
sed  -i '' "s%????*%$VERSION - $d%" CHANGES.md 
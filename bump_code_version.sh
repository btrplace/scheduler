#!/bin/sh
#Script to upgrade the version number in some working files

if [ $# -ne 1 ]; then
    echo "Usage: $0 version_number"
    exit 1
fi
VERSION=$1

## The README.md

# Update of the version number for maven usage
sed  -i '' "s%<version>.*</version>%<version>$VERSION</version>%"  README.md

# Update the bundle location
sed  -i '' "s%repos/releases.*%repos/releases/btrplace/solver\-bundle/$VERSION/solver\-bundle\-$VERSION\.jar%" README.md

# Apidoc
sed  -i '' "s%apidoc/releases.*%apidoc/releases/btrplace/solver/$VERSION/%" README.md

## The CHANGES.md file
d=`LANG=en_US.utf8 date +"%d %b %Y"`
sed  -i '' "s%????*%$VERSION - $d%" CHANGES.md 
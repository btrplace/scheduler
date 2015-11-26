#!/bin/bash
#Shell script to initiate the release
#The script creates a 'release' branch, set the version the pom.xml and the changelog then push
#The remainder of the process should be handled by the build_release.sh script.

source bin/commons.sh

echo "Check for a competing release"
git ls-remote --exit-code --heads origin release
if [ $? -eq 0 ]; then
    echo "Error: A release is already under progress"
    exit 1
fi
echo "Ok"
VERSION=$(getVersionToRelease)
echo "Version to release: ${VERSION}"
git checkout -b release || exit 1

#Establish the version, maven side, misc. side
./bin/set_version.sh ${VERSION}
git commit -m "initiate release ${VERSION}" -a
git push origin release || exit 1
git checkout -
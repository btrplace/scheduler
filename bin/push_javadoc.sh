#!/bin/bash

source bin/commons.sh

if [ $# -ne 1 ]; then
	echo "Usage: $0 repos"
	echo "the github token must be provided through environment variable GH_TOKEN"
	exit 1
fi

[ -z "$GH_TOKEN" ] && echo "Missing environment variable GH_TOKEN" && exit 1;

LOCAL=`mktemp -d -t btrplace.XXX`
REPOS=$1
VERSION=$(getVersionToRelease)
HEAD=$(git rev-parse HEAD)
git -C "${LOCAL}" init
git -C "${LOCAL}" remote add origin "https://${GH_TOKEN}@github.com/${REPOS}"||exit 1
git -C "${LOCAL}" fetch origin||exit 1
git -C "${LOCAL}" checkout gh-pages||git -C "${LOCAL}" checkout -b gh-pages

#Don't generate if not needed
if [ -f "${LOCAL}/.commit" ]; then
	IN=`cat ${LOCAL}/.commit`		
	if [ $IN == "${HEAD}" ]; then
		echo "Javadoc synced with HEAD"
		exit 0
	else
		echo "Stored version is ${IN} but HEAD is ${HEAD}. Need to resync"
	fi
fi
cd "${LOCAL}"
rm -rf *
cd -
#Generate and copy
mvn -q compile -DskipTests javadoc:aggregate||exit 1
cp -r target/site/apidocs/* "${LOCAL}"/
echo "${HEAD}" > "${LOCAL}/.commit"

#Publish
cd "${LOCAL}"
git add *
git add .commit
cd -

git -C "${LOCAL}" commit -m "apidoc for version ${VERSION}" -a||exit 1
git -C "${LOCAL}" push --force --quiet origin gh-pages||exit 1
rm -rf "${LOCAL}"

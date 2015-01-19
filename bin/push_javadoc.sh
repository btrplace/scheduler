#!/bin/bash
if [ $# -ne 2 ]; then
	echo "Usage: $0 remote version"
	exit 1
fi
LOCAL=`mktemp -d -t btrplace.XXX`
REMOTE=$1
VERSION=$2
set -x
git -C ${LOCAL} init
git -C ${LOCAL} remote add origin git@github.com:btrplace/${REMOTE}||exit 1
git -C ${LOCAL} pull origin gh-pages||exit 1
git -C ${LOCAL} checkout gh-pages||exit 1
cd ${LOCAL}
rm -rf *
cd -

#Generate and copy
mvn -q compile -DskipTests javadoc:aggregate||exit 1
cp -r target/site/apidocs/* ${LOCAL}/

#Publish
cd ${LOCAL}
git add *
cd -
git -C ${LOCAL} commit -m "apidoc for version ${VERSION}" -a||exit 1
git -C ${LOCAL} push origin gh-pages||exit 1
rm -rf ${LOCAL}

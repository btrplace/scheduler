#!/bin/sh
LOCAL=$1
VERSION=$2

#Clone the dedicated repository if needed
if [ ! -d ${LOCAL} ]; then	
	git clone git@github.com:btrplace/apidocs.git ${LOCAL} || exit 1
else
	git -C ${LOCAL} pull || exit 1
fi

#Generate and copy
mvn javadoc:aggregate
cp -r target/site/apidocs/* ${LOCAL}/

#Publish
git -C ${LOCAL} commit -m "apidoc for version ${VERSION}" -a || exit 1
git -C ${LOCAL} push || exit 1

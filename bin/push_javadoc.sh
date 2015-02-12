#!/bin/bash
if [ $# -ne 1 ]; then
	echo "Usage: $0 repos"
	exit 1
fi
LOCAL=`mktemp -d -t btrplace.XXX`
REPOS=$1
VERSION=$2
HEAD=$(git rev-parse HEAD)
git -C ${LOCAL} init
git -C ${LOCAL} remote add origin git@github.com:${REPOS}||exit 1
git -C ${LOCAL} pull origin gh-pages||exit 1
git -C ${LOCAL} checkout gh-pages||exit 1

#Don't generate if not needed
if [ -f ${LOCAL}/.commit ]; then
	IN=`cat ${LOCAL}/.commit`		
	if [ $IN == $HEAD ]; then 
		echo "Javadoc synced with HEAD"
		exit 0
	else
		echo "Stored version is ${IN} but HEAD is ${HEAD}. Need to resync"
	fi
fi
cd ${LOCAL}
rm -rf *
cd -
#Generate and copy
mvn -q compile -Dmaven.repo.local=/tmp/cache -DskipTests javadoc:aggregate||exit 1
cp -r target/site/apidocs/* ${LOCAL}/
echo ${HEAD} > ${LOCAL}/.commit

#Publish
cd ${LOCAL}
git add *
git add .commit
cd -

git -C ${LOCAL} commit -m "apidoc for version ${VERSION}" -a||exit 1
git -C ${LOCAL} push origin gh-pages||exit 1
rm -rf ${LOCAL}

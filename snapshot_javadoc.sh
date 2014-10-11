#!/bin/sh

if [ $# -ne 1 ]; then
	echo "Usage: $0 output_dir"
	exit 1
fi

WWW=$1

echo "Publishing javadoc into ${WWW}"
mkdir -p ${WWW}

#sub-modules apidoc
echo "Generate the javadoc for the modules"
mvn javadoc:javadoc > /dev/null
mvn javadoc:jar > /dev/null
#set -x
for m in api json choco; do
	ARTIFACT_ID="solver-${m}"
	mkdir -p ${WWW}/${ARTIFACT_ID} > /dev/null
	rm -rf ${WWW}/${ARTIFACT_ID}/apidocs-snapshot
	mv ${m}/target/apidocs ${WWW}/${ARTIFACT_ID}/apidocs-snapshot
done

#Aggregated javadoc
echo "Generate the aggregated javadoc"
mvn javadoc:aggregate > /dev/null
mvn javadoc:aggregate-jar > /dev/null
mkdir -p ${WWW}/solver
rm -rf ${WWW}/solver/apidocs-snapshot
mv target/apidocs ${WWW}/solver/apidocs-snapshot
rm -rf ${WWW}/apidocs-snapshot
ln -s ${WWW}/solver/apidocs-snapshot ${WWW}/apidocs-snapshot
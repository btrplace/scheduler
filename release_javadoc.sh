#!/bin/sh

function getVersion {
    mvn org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version | grep -v "\[INFO\]"
}

if [ $# -ne 1 ]; then
	echo "Usage: $0 output_dir"
	exit 1
fi

WWW=$1
VERSION=$(getVersion)

echo "Publishing javadoc for version $VERSION into ${WWW}"
mkdir -p ${WWW}

#sub-modules apidoc
echo "Generate the javadoc for the modules"
mvn javadoc:javadoc > /dev/null
mvn javadoc:jar > /dev/null
#set -x
for m in api json choco; do
	ARTIFACT_ID="solver-${m}"
	mkdir -p ${WWW}/${ARTIFACT_ID} > /dev/null
	rm -rf ${WWW}/${ARTIFACT_ID}/${VERSION}
	mv ${m}/target/site/apidocs ${WWW}/${ARTIFACT_ID}/${VERSION}
	mv ${m}/target/${ARTIFACT_ID}-${VERSION}-javadoc.jar ${WWW}/${ARTIFACT_ID}/
	rm -rf ${WWW}/${ARTIFACT_ID}/apidocs
	ln -s ${WWW}/${ARTIFACT_ID}/${VERSION} ${WWW}/${ARTIFACT_ID}/apidocs
done

#Aggregated javadoc
echo "Generate the aggregated javadoc"
mvn javadoc:aggregate > /dev/null
mvn javadoc:aggregate-jar > /dev/null
mkdir -p ${WWW}/solver
rm -rf ${WWW}/solver/${VERSION}
mv target/site/apidocs ${WWW}/solver/${VERSION}
mv target/solver-${VERSION}-javadoc.jar ${WWW}/solver/
rm -rf ${WWW}/apidocs
ln -s ${WWW}/solver/${VERSION} ${WWW}/apidocs
rm -rf ${WWW}/solver/apidocs
ln -s ${WWW}/solver/${VERSION} ${WWW}/solver/apidocs

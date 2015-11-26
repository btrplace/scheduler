function err() {
    echo "ERROR: $1"
    cat $2
    exit 1
}

function warn() {
    echo "WARNING: $1"
    cat $2
}

function getVersionToRelease() {
	#blank execution as this command is very fragile and bug if there is sth to download
mvn ${MVN_ARGS} org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version > /dev/null
CURRENT_VERSION=`mvn ${MVN_ARGS} org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version | grep -v "\[INFO\]"`
echo ${CURRENT_VERSION%%-SNAPSHOT}
}

function getVersion() {
	#blank execution as this command is very fragile and bug if there is sth to download
mvn ${MVN_ARGS} org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version > /dev/null
CURRENT_VERSION=`mvn ${MVN_ARGS} org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version | grep -v "\[INFO\]"`
echo ${CURRENT_VERSION}
}

function quit() {
    echo "ERROR: $*"
    exit 1
}
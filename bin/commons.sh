function err() {
    echo "ERROR: $1"
    cat $2
    exit 1
}

function warn() {
    echo "WARNING: $1"
    cat $2
}

function getVersion() {
    xmllint --xpath '/*[local-name()="project"]/*[local-name()="version"]/text()' pom.xml
}

function quit() {
    echo "ERROR: $*"
    exit 1
}

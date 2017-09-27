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
    ./bin/version.py
}

function quit() {
    echo "ERROR: $*"
    exit 1
}

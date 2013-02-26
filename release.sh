#!/bin/sh


if [ $# != 1 ]; then
    echo "Usage: $0 prepare|perform"
    exit 1
fi


function getVersionToRelease {
    CURRENT_VERSION=`mvn ${MVN_ARGS} org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version | grep -v "\[INFO\]"`
    echo ${CURRENT_VERSION%%-SNAPSHOT}
}

function getBranch {
    git symbolic-ref --short HEAD
}

case $1 in

prepare)        
    VERSION=$(getVersionToRelease)
    RELEASE_BRANCH="release/$VERSION"    
    git checkout -b ${RELEASE_BRANCH} || exit 1
    echo $VERSION > .version
    git add .version
    git push origin ${RELEASE_BRANCH} || exit 1
    echo "Branch $RELEASE_BRANCH is ready"
    ;;
perform)
    if [ $(hostname) != "btrp" ]; then
            echo "This script must be executed on btrp.inria.fr"
            exit 1
    fi
    set -x
    #CURRENT_BRANCH=$(getBranch)    
    #VERSION=${CURRENT_BRANCH##release/}    
    VERSION=$(cat .version)
    #Code update and maven release process
    ./bump_release.sh code $VERSION || exit 1
    mvn -B release:prepare || exit 1
    mvn release:perform || exit 1

    #We generate the big javadoc and put it on the webserver
    mvn javadoc:aggregate || exit 1
    APIDOC_ROOT="/usr/share/nginx/html/apidocs/release/btrplace/solver/"
    mkdir -p $APIDOC_ROOT > /dev/null
    mv target/apidoc ${APIDOC_ROOT}/${VERSION}
    
    # merge the version changes back into develop so that folks are working against the new release 
    git checkout develop || exit 1
    git merge --no-ff release/$VERSION || exit 1
 
    # housekeeping -- rewind the release branch by one commit to fix its version at $VERSION
    #   excuse the force push, it's because maven will have already pushed the next dev version
    #   to origin with this branch, and I don't want that version (or a diverging revert commit)
    #   in the release or master branches.
    git checkout release/$VERSION || exit 1
    git reset --hard HEAD~1 || exit 1
    git push --force origin release/$VERSION || exit 1
 
    # finally, if & when the code gets deployed to production
    git checkout master || exit 1
    git merge --no-ff release/$VERSION || exit 1
    git branch -d release/$VERSION || exit 1

    ./bump_release.sh site ${VERSION}
    ;;
esac
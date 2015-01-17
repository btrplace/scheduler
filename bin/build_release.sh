#!/bin/bash

function err() {
    echo "ERROR: $1"
    cat *1
    exit 1
}

function warn() {    
    echo "WARNING: $1"
    cat $1    
}

function getVersion() {
    mvn ${MVN_ARGS} org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version |grep "^[0-9]\+\\.[0-9]\+" 2>/dev/null
}

    #Extract the version
    VERSION=$(getVersion)
    TAG="btrplace-scheduler-${VERSION}"
    COMMIT=$(git rev-parse HEAD)
    echo "** Starting release of ${TAG} from ${COMMIT} on github **"
    #Quit if tag already exists
    git ls-remote --exit-code --tags origin ${TAG} 2>&1 > /dev/null
    if [ $? -ne 0 ]; then
        echo "Tag ${TAG} does not exist. Retry"        
        #Working version ?
        mvn clean test 2>&1 > test.out ||err "Unstable build" test.out 
        echo "\tTests are ok"   
        git tag ${TAG} 2>&1 > tag.out ||err "Unable to tag with ${TAG}" tag.out        
        echo "\tTagged locally"
        git push deploy --tags 2>&1 > push.out ||err "Unable to push the tag ${TAG}" push.out
        echo "\tTag pushed"        
    else
        echo "Already done"
    fi

    #Deploy the artifacts    
    echo "** Releasing to sonatype **"
    ./bin/deploy.sh 2>&1 > deploy.out ||err "Unable to release" deploy.out    
    echo "\tOK"
    
    echo "** Deploying the javadoc **"
    ./bin/push_javadoc.sh apidocs.git ${VERSION} 2>&1 > javadoc.out || warn "Unable to push the javadoc" javadoc.out
    echo "\tOK"

    #Clean      
    git push deploy --delete release 2>&1 > delete_release.out || warn "Unable to delete the remote release branch" delete_release.out
    rm -rf delete_release.out
    echo "** Remote branch release removed **"

    #Set the next development version    
    echo "** Prepare master for the next version **"
    git fetch deploy master:refs/remotes/deploy/master 2>&1 > master.out ||warn "Unable to fetch master" master.out
    git checkout -b master deploy/master 2>&1 >> master.out ||warn "No master branch" master.out
    git merge -m "merging with version ${VERSION}" --no-ff ${COMMIT} 2>&1 >> master.out ||warn "Unable to integrate to master" master.out
    echo "\tMerged"
    ./bin/set_version.sh --next ${VERSION}
    NEW_VERSION=$(getVersion)    
    git commit -m "Prepare the code for the next version ${NEW_VERSION}" -a 2>&1 >> master.out || warn "Unable to commit" master.out
    echo "\tCommitted new version ${NEW_VERSION}"
    git push deploy master 2>&1 >> master.out ||warn "Unable to push master" master.out
    echo "\tPushed"
    rm -rf *.out > /dev/null

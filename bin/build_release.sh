#!/bin/bash

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
    mvn ${MVN_ARGS} -Dmaven.repo.local=/tmp/cache org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version |grep "^[0-9]\+\\.[0-9]\+" 2>/dev/null
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
        mvn -Dmaven.repo.local=/tmp/cache clean test >tests.out 2>&1 ||err "Unstable build" tests.out 
        echo "Tests are ok"   
        git tag ${TAG} >tag.out 2>&1 ||err "Unable to tag with ${TAG}" tag.out        
        echo "Tagged locally"
        git push deploy --tags >push.out 2>&1 ||err "Unable to push the tag ${TAG}" push.out
        echo "Tag pushed"
        bin/github_release.pl        
    else
        echo "Already done"
    fi

    #Deploy the artifacts    
    echo "** Releasing to sonatype **"
    ./bin/deploy.sh >deploy.out 2>&1 ||err "Unable to release" deploy.out        
    
    echo "** Deploying the javadoc **"
    ./bin/push_javadoc.sh btrplace/apidocs.git ${VERSION} >javadoc.out 2>&1  || warn "Unable to push the javadoc" javadoc.out    

    #Clean      
    git push deploy --delete release >delete_release.out 2>&1 || warn "Unable to delete the remote release branch" delete_release.out    
    echo "** Remote branch release removed **"

    #Set the next development version    
    echo "** Prepare master for the next version **"
    git fetch deploy master:refs/remotes/deploy/master >master.out 2>&1 ||warn "Unable to fetch master" master.out
    git checkout -b master deploy/master >>master.out 2>&1 ||warn "No master branch" master.out
    git merge -m "merging with version ${VERSION}" --no-ff ${COMMIT} 2>&1 >> master.out ||warn "Unable to integrate to master" master.out
    echo "\tMerged"
    ./bin/set_version.sh --next ${VERSION} >version.out 2>&1 ||warn "Unable to set the new version" version.out
    NEW_VERSION=$(getVersion)    
    git commit -m "Prepare the code for the next version ${NEW_VERSION}" -a 2>&1 >> master.out || warn "Unable to commit" master.out
    echo "\tCommitted new version ${NEW_VERSION}"
    git push deploy master >> master.out 2>&1||warn "Unable to push master" master.out
    echo "\tPushed"
    rm -rf *.out > /dev/null

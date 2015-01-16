#!/bin/bash

function quit() {
    echo "ERROR: $*"
    exit 1
}

function warn() {
    echo "WARNING: $*"
    exit 0
}

function getVersion() {
    mvn ${MVN_ARGS} org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version |grep "^[0-9]\+\\.[0-9]\+" 2>/dev/null
}

    #Extract the version
    VERSION=$(getVersion)
    TAG="btrplace-scheduler-${VERSION}"
    COMMIT=$(git rev-parse HEAD)
    echo "** Starting the release of ${TAG} from ${COMMIT} **"
    #Quit if tag already exists
    git ls-remote --exit-code --tags origin ${TAG} 
    if [ $? -ne 0 ]; then
        echo "The tag does not exist. Maybe we continue a breaking release"        
        #Working version ?
        mvn clean test ||quit "Unstable build"
        git tag ${TAG} ||quit "Unable to tag with ${TAG}"
        git push deploy --tags ||quit "Unable to push the tag ${TAG}"

    else
        echo "Tag already exists. Maybe the releasing process is still not complete"
    fi

    #Deploy the artifacts    
    #echo "** Deploying the javadoc **"
    #./bin/push_javadoc.sh apidocs.git ${VERSION}
    echo "** Deploying artifacts to sonatype **"
    ./bin/deploy.sh||quit "Unable to release"

    #Clean    
    git push deploy --delete release

    #Set the next development version
    echo "** Prepare master for the next version **"
    git fetch deploy master:refs/remotes/deploy/master||warn "Unable to fetch master"
    git checkout -b master deploy/master||warn "No master branch"
    git merge -m "merging with version ${VERSION}" --no-ff ${COMMIT}||warn "Unable to integrate to master"
    ./bin/set_version.sh --next ${VERSION}
    git commit -m "Prepare the code for the next version" -a
    git push deploy master ||warn "Unable to push master"        
#!/bin/bash

function quit() {
    echo "ERROR: $*"
    exit 1
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
    git ls-remote --exit-code --tags origin ${TAG} && quit "tag ${TAG} already exists"

    #Working version ?
    mvn clean test ||quit "Unstable build"

    git fetch origin master:refs/remotes/origin/master||quit "Unable to fetch master"
    #Integrate with master and tag
    echo "** Integrate to master **"
    git checkout -b master origin/master||quit "No master branch"
    git merge -m "merging with version ${VERSION}" --no-ff ${COMMIT}||quit "Unable to integrate to master"

    git tag ${TAG} ||quit "Unable to tag with ${TAG}"
    git push deploy --tags ||quit "Unable to push the tag ${TAG}"
    git push deploy master ||quit "Unable to push master"

    #Deploy the artifacts
    echo "** Deploying the artifacts **"
    ./bin/push_javadoc.sh apidocs.git ${VERSION}
    ./bin/deploy.sh

    #Clean
    #git push origin --delete release

    #Set the next development version
    echo "** Prepare master for the next version **"
    ./bin/set_version.sh --next ${VERSION}
    git commit -m "Prepare the code for the next version" -a

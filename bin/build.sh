#!/bin/bash

function quit() {
    echo "ERROR: $1"
    exit 1
}
function getVersion() {
    mvn ${MVN_ARGS} org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version | grep -v "\[INFO\]"
}

if test -z "$TRAVIS_BRANCH"; then
    BRANCH=$(git rev-parse --abbrev-ref HEAD)
else
    BRANCH=${TRAVIS_BRANCH}
fi

if [ ${BRANCH} = "release" ]; then
    #Extract the version
    VERSION=$(getVersion)
    TAG="btrplace-scheduler-${VERSION}"
    COMMIT=$(git rev-parse HEAD)
    echo "** Starting the release of ${TAG} from commit **"
    echo "Commit: ${COMMIT}"
    #Quit if tag already exists
    git ls-remote --exit-code --tags origin ${TAG} ||quit "tag ${TAG} already exists"

    #Working version ?
    mvn clean test ||quit "Unstable build"

    #Integrate with master and tag
    echo "** Integrate to master **"
    git checkout master
    git merge --no-ff ${COMMIT}

    #Javadoc
    ./bin/push_javadoc apidocs.git ${VERSION}

    git tag ${TAG} ||quit "Unable to tag"
    git push --tags ||quit "Unable to push the tag"
    git push origin master ||quit "Unable to push master"

    #Set the next development version
    echo "** Prepare develop for the next version **"
    git checkout develop
    git merge --no-ff ${TAG}
    ./bin/set_version.sh --next ${VERSION}
    git commit -m "Prepare the code for the next version" -a

    #Push changes on develop, with the tag
    git push origin develop ||quit "Unable to push develop"

    #Deploy the artifacts
    echo "** Deploying the artifacts **"
    ./bin/deploy.sh ||quit "Unable to deploy"

    #Clean
    #git push origin --delete release
else
    ./bin/deploy.sh||quit "Unable to deploy"
fi

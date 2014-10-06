#!/bin/sh

function getVersionToRelease {
    CURRENT_VERSION=`mvn ${MVN_ARGS} org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version | grep -v "\[INFO\]"`
    echo ${CURRENT_VERSION%%-SNAPSHOT}
}

if [ ${TRAVIS_BRANCH} =~ ^release ]; then
    #Extract the version
    VERSION=${TRAVIS_BRANCH:8}
    TAG="btrplace-scheduler-${VERSION}"

    #Quit if tag already exists
    git ls-remote --exit-code --tags origin ${TAG} || exit 1

    #Establish the version, maven side, misc. side
    mvn versions:set -DnewVersion=${VERSION} || exit 1
    ./bump_release.sh code ${VERSION} || exit 1

    #Working version ?
    mvn clean test || exit 1

    #Tag the code
    git tag ${TAG} || exit 1

    #Integrate with master and develop
    git checkout master
    git merge --no-ff ${TAG}

    git checkout develop
    git merge --no-ff ${TAG}
    #Set the next version
    mvn versions:use-next-releases
    git commit -m "Prepare the code for the next version" -a


    #Push changes, with the tag
    git push origin master develop || exit 1
    git push --tags || exit 1

    #Deploy the artifacts
    mvn deploy || exit 1

    #Javadoc
    ./push_javadoc apidocs.git ${VERSION}

else
    mvn clean test
fi

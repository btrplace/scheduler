#!/bin/bash

function getVersion {
    mvn ${MVN_ARGS} org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version | grep -v "\[INFO\]"
}

if test -z "$TRAVIS_BRANCH"; then
    BRANCH=$(git rev-parse --abbrev-ref HEAD)
else
    BRANCH=${TRAVIS_BRANCH}
fi

if [ ${BRANCH} = "release" ]; then
    #Extract the version
    VERSION=getVersion
    TAG="btrplace-scheduler-${VERSION}"
    COMMIT=$(git rev-parse HEAD)

    #Quit if tag already exists
    git ls-remote --exit-code --tags origin ${TAG} ||exit 1

    #Working version ?
    mvn clean test ||exit 1

    #Integrate with master and tag
    echo "** Integrate to master **"
    git checkout master
    git merge --no-ff ${COMMIT}

    #Javadoc
    ./bin/push_javadoc apidocs.git ${VERSION}

    git tag ${TAG} ||exit 1
    git push --tags ||exit 1
    git push origin master ||exit 1

    #Set the next development version
    echo "** Prepare develop for the next version **"
    git checkout develop
    git merge --no-ff ${TAG}
    ./bin/set_version.sh --auto ${VERSION}
    git commit -m "Prepare the code for the next version" -a

    #Push changes on develop, with the tag
    git push origin develop ||exit 1

    #Deploy the artifacts
    echo "** Deploying the artifacts **"
    ./bin/deploy.sh ||exit 1

    #Clean
    git push origin --delete release
else
    ./bin/deploy.sh||exit 1
fi

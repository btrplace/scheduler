#!/bin/bash

function getVersionToRelease() {
    CURRENT_VERSION=`mvn ${MVN_ARGS} org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version | grep -v "\[INFO\]"`
    echo ${CURRENT_VERSION%%-SNAPSHOT}
}

if test -z "$TRAVIS_BRANCH"; then
    BRANCH=$(git rev-parse --abbrev-ref HEAD)
else
    BRANCH=${TRAVIS_BRANCH}
fi

if [ ${BRANCH} = "release" ]; then
    #Extract the version
    VERSION=getVersionToRelease
    TAG="btrplace-scheduler-${VERSION}"

    #Quit if tag already exists
    git ls-remote --exit-code --tags origin ${TAG} ||exit 1

    #Establish the version, maven side, misc. side
    ./bin/set_version.sh --auto ${VERSION}

    #Working version ?
    mvn clean test ||exit 1

    #Tag the code
    git tag ${TAG} ||exit 1

    #Integrate with master
    git checkout master
    git merge --no-ff ${TAG}

    #Set the next development version
    git checkout develop
    git merge --no-ff ${TAG}
    ./bin/set_version.sh --auto ${VERSION}
    git commit -m "Prepare the code for the next version" -a

    #Push changes, with the tag
    git push origin master develop ||exit 1
    git push --tags ||exit 1

    #Deploy the artifacts
    gpg --import etc/private.key
    mvn -s etc/sonatype.xml clean javadoc:jar source:jar gpg:sign -Dgpg.passphrase="${GPG_PASSPHRASE}" deploy ||exit 1

    #Javadoc
    ./bin/push_javadoc apidocs.git ${VERSION}

    #Clean
    git branch -d release
    git push origin --delete release
else
    gpg --import etc/private.key
    mvn -s etc/sonatype.xml clean javadoc:jar source:jar gpg:sign -Dgpg.passphrase="${GPG_PASSPHRASE}" deploy ||exit 1
fi

#!/bin/bash

source bin/commons.sh




#Tests are passing
echo "** Testing **"
mvn test >tests.out 2>&1 ||err "  Unstable build" tests.out

#No open issues in the current milestone
VERSION=`./bin/version.py --release`
echo "** Version to release: ${VERSION} **"
./bin/github.py milestone-close ${VERSION}||exit 1
echo "  Milestone ${VERSION} closed"

#Extract the version
TAG="btrplace-scheduler-${VERSION}"
COMMIT=$(git rev-parse HEAD)
echo "** Starting release of ${TAG} from ${COMMIT} on github **"
#Quit if tag already exists
git ls-remote --exit-code --tags origin ${TAG} 2>&1 > /dev/null
if [ $? -ne 0 ]; then
    echo "  Tag ${TAG} does not exist. Tagging"
    git tag ${TAG} >tag.out 2>&1 ||err "Unable to tag with ${TAG}" tag.out        
    echo "  Tagged locally"
    git push deploy --tags >push.out 2>&1 ||err "Unable to push the tag ${TAG}" push.out
    echo "  Tag pushed"    
else
    echo "  Already done"
fi

#Deploy the artifacts    
echo "** Releasing to sonatype **"
./bin/deploy.sh >deploy.out 2>&1 ||err "  Unable to release" deploy.out        

echo "** Deploying the javadoc **"
./bin/push_javadoc.sh btrplace/apidocs.git >javadoc.out 2>&1  || warn "  unable to push the javadoc" javadoc.out

echo "** Push the changelog **"
./bin/github.py release ${VERSION}

#Clean      
git push deploy --delete release >delete_release.out 2>&1 || warn "  unable to delete the remote release branch" delete_release.out    
echo "** Remote branch release removed **"

#Integrate into master
echo "** Prepare master for the next version **"
git fetch deploy master:refs/remotes/deploy/master >master.out 2>&1 ||warn "Unable to fetch master" master.out
git checkout -b master deploy/master >>master.out 2>&1 ||warn "No master branch" master.out
git merge -m "merging with version ${VERSION}" --no-ff ${COMMIT} 2>&1 >> master.out ||warn "Unable to integrate to master" master.out
echo " release merged with master"

#Prepare the new version
NEW_VERSION=`./bin/version.py --next`
DEV_VERSION="${NEW_VERSION}-SNAPSHOT"
echo " new development version: ${DEV_VERSION}"
mvn versions:set -DnewVersion=${DEV_VERSION} -DgenerateBackupPoms=false >version.out 2>&1 ||warn "Unable to set the new version" version.out
./bin/changelog.py new ${NEW_VERSION}
./bin/github.py milestone-open ${NEW_VERSION}||exit 1
git commit -m "Prepare the code for the next version ${NEW_VERSION}" -a 2>&1 >> master.out || warn "Unable to commit" master.out
echo "  committed new dev version ${DEV_VERSION}"
git push deploy master >> master.out 2>&1||warn "Unable to push master" master.out
echo "  pushed"
rm -rf *.out > /dev/null

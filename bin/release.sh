#!/bin/bash
#Shell script to initiate the release
#The script creates a 'release' branch, set the version the pom.xml and the changelog then push
#The remainder of the process should be handled by the build_release.sh script.

source bin/commons.sh

git checkout -b release ||exit 1

####
#No open issues in the current milestone
####
VERSION=$(./bin/version.py --release)
echo "** Version to release: ${VERSION} **"
./bin/github.py milestone-close "${VERSION}"||exit 1
echo "  Milestone ${VERSION} closed"

#### 
# Prepare the release.
####

# Set the version, maven side.
mvn versions:set -DnewVersion="${VERSION}" -DgenerateBackupPoms=false||exit 1
# changelog side.
./bin/changelog.py timestamp||exit 1
git commit -m "Release version ${VERSION}" -a

TAG="btrplace-scheduler-${VERSION}"
COMMIT=$(git rev-parse HEAD)
git tag "${TAG}"||exit 1
echo "** Release ${TAG} cut from ${COMMIT} **"

####
# Prepare the next version.
####
NEW_VERSION=$(./bin/version.py --next)
DEV_VERSION="${NEW_VERSION}-SNAPSHOT"
echo "** New development version: ${DEV_VERSION} **"
mvn versions:set -DnewVersion="${DEV_VERSION}" -DgenerateBackupPoms=false >version.out 2>&1 ||warn "Unable to set the new version" version.out
./bin/changelog.py new "${NEW_VERSION}"
./bin/github.py milestone-open "${NEW_VERSION}"||exit 1
echo "  Milestone ${NEW_VERSION} opened"
git commit -m "Initiate new version ${NEW_VERSION}" -a

# Back to master, we rebase to be in sync.
echo "** switching back to the master branch **"
git checkout master
git rebase release
git branch -d release

####
# Push the master branch then the tag to launch the test & deploy.
####
git push
git push origin "${TAG}"


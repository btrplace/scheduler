#!/bin/bash

if test -z "$TRAVIS_BRANCH"; then
    BRANCH=$(git rev-parse --abbrev-ref HEAD)
else
    BRANCH=${TRAVIS_BRANCH}
fi

if [ ${BRANCH} = "release" ]; then
    ./bin/build_release.sh || exit 1
else
    ./bin/build_snapshot.sh ||exit 1
fi

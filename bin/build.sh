#!/bin/bash

if test -z "$TRAVIS_BRANCH"; then
    BRANCH=$(git rev-parse --abbrev-ref HEAD)
else
    BRANCH=${TRAVIS_BRANCH}
fi

case ${BRANCH} in
master)
	./bin/build_snapshot.sh || exit 1
	;;
*)
    ./bin/build_default.sh || exit 1
esac

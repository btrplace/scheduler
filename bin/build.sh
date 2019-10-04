#!/bin/bash

if test -z "$TRAVIS_BRANCH"; then
    BRANCH=$(git rev-parse --abbrev-ref HEAD)
else
    BRANCH=${TRAVIS_BRANCH}
fi

case ${BRANCH} in
release)
	./bin/build_release.sh || exit 1	
	;;
master)
	./bin/build_snapshot.sh || exit 1
	;;
travis_dbg)
	./bin/push_javadoc.sh btrplace/apidocs|| exit 1
	;;
*)
    ./bin/build_default.sh || exit 1
esac

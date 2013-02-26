#!/bin/sh
#Script to notify the website about a release




if [ $# -ne 2 ]; then
    echo "Usage: $0 [site|code] version_number"
    exit 1
fi
VERSION=$2

case $1	in

site)
	d=`LANG=en_US.utf8 date +"%d %b %Y"`

	WWW_HOOK="http://localhost/~fhermeni/btrplace/admin/bump_release.php"
	JSON="{\"version\":\"$VERSION\",\
	\"date\":\"$d\",\
	\"title\":\"solver\",\
	\"apidoc\":\"http://btrp.inria.fr:8080/apidocs/releases/btrplace/solver/$VERSION/\",\
	\"changelog\":\"https://github.com/fhermeni/btrplace-solver/tree/btrplace-solver-$VERSION\",\
	\"link\":\"http://btrp.inria.fr:8080/repos/releases/btrplace/solver-bundle/$VERSION/solver-bundle-$VERSION.jar\"\
	}"
	curl -X POST --data "data=$JSON" $WWW_HOOK
	;;
code)
	## The README.md
	# Update of the version number for maven usage
	sed  -i '' "s%<version>.*</version>%<version>$VERSION</version>%"  README.md

	# Update the bundle location
	sed  -i '' "s%repos/releases.*jar%repos/releases/btrplace/solver\-bundle/$VERSION/solver\-bundle\-$VERSION\.jar%" README.md

	# apidoc
	sed  -i '' "s%apidoc/releases.*%apidoc/releases/btrplace/solver/$VERSION/%" README.md

	## The CHANGES.md file
	d=`LANG=en_US.utf8 date +"%d %b %Y"`
	sed  -i '' "s%????*%$VERSION - $d%" CHANGES.md 
	;;
	*)
		echo "Target must be either 'site' or 'code'"
		exit 1
esac


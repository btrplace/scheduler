#!/bin/sh
#Script to notify the website about a release


if [ $# -ne 2 ]; then
    echo "Usage: $0 [site|code] version_number"
    echo "'site': notify a release on the website"
    echo "'code': upgrade the version number in the code"
    exit 1
fi
VERSION=$2
REPO_URL="http://btrp.inria.fr:8080/repos"
APIDOC_URL="http://btrp.inria.fr:8080/apidocs"

case $1	in

site)
	d=`LANG=en_US.utf8 date +"%d %b %Y"`
	WWW_HOOK="http://btrp.inria.fr:8080/www/admin/bump_release.php"

	JSON="{\"version\":\"$VERSION\",\
	\"date\":\"$d\",\
	\"title\":\"solver\",\
	\"apidoc\":\"$APIDOC_URL/releases/btrplace/solver/$VERSION/\",\
	\"changelog\":\"https://github.com/fhermeni/btrplace-solver/tree/btrplace-solver-$VERSION\",\
	\"link\":\"$REPO_URL/releases/btrplace/solver-bundle/$VERSION/solver-bundle-$VERSION.jar\"\
	}"
	curl -X POST --data "data=$JSON" $WWW_HOOK
	;;
code)

	#Darwin specificities about "in place" editing
	IN_PLACE="-i ''"
	if [ $(uname) = "Darwin" ]; then		
		IN_PLACE="-i''"
	fi

	## The README.md
	# Update of the version number for maven usage
	sed  ${IN_PLACE} "s%<version>.*</version>%<version>$VERSION</version>%"  README.md

	snapshot=0
	echo $VERSION | grep "\-SNAPSHOT$" > /dev/null && snapshot=1

	if [ $snapshot = 0 ]; then 
		# Update the bundle and the apidoc location			
		sed ${IN_PLACE} "s%$REPO_URL.*solver\-bundle.*%$REPO_URL/releases/btrplace/solver\-bundle/$VERSION/solver\-bundle\-$VERSION\.jar%" README.md		
		sed ${IN_PLACE} "s%$APIDOC_URL/.*%$APIDOC_URL/releases/btrplace/solver/$VERSION/%" README.md
	else 
		# Update the bundle and the apidoc location
		sed ${IN_PLACE} "s%$REPO_URL.*solver\-bundle.*%$REPO_URL/snapshot-releases/btrplace/solver\-bundle/$VERSION/%" README.md	 #There is multiple jar for the snapshots, so we refer to the directory
		sed ${IN_PLACE} "s%$APIDOC_URL/.*%$APIDOC_URL/snapshots/btrplace/solver/%" README.md
	fi	
	## The CHANGES.md file
	d=`LANG=en_US.utf8 date +"%d %b %Y"`
	sed  ${IN_PLACE} "s%????*%$VERSION - $d%" CHANGES.md 
	;;
	*)
		echo "Target must be either 'site' or 'code'"
		exit 1
esac


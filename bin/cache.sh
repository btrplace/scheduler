#!/bin/bash
#Get the local repository cache

LOCAL=$2

case $1 in

	get)
	REPOS=$3
	set -x
	mkdir -p ${LOCAL} > /dev/null
	git -C ${LOCAL} init
	git -C ${LOCAL} remote add origin git@github.com:${REPOS}.git||exit 1
	git -C ${LOCAL} pull origin master||exit 1
	;;
	push)
	cd ${LOCAL}
	git add *	
	cd -
	git -C ${LOCAL} commit -m "update the cache" -a||exit 1
	git -C ${LOCAL} push origin master||exit 1	
	;;
	*)
	echo "Unsupppored operation ($1)"
esac

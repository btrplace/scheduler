#!/bin/sh
authors=`git log --format='%aE'|sort -u`
#echo "email;files;inserted;deleted"
#for a in ${authors}; do
#	git log --shortstat --author="${a}" | grep -E "fil(e|es) changed" | awk -v a="$a" '{files+=$1; inserted+=$4; deleted+=$6} END {printf "%s;%d;%d;%d\n", a, files, inserted, deleted }'
#done


printf "%-30s %-8s %-8s %-8s\n" email files inserted deleted
for a in ${authors}; do
	git log --shortstat --author="${a}" | grep -E "fil(e|es) changed" | awk -v a="$a" '{files+=$1; inserted+=$4; deleted+=$6} END {printf "%-30s %-8d %-8d %-8d\n", a, files, inserted, deleted }'
done
#git log --shortstat --author="${a}" | grep -E "fil(e|es) changed" | awk -v a="$a" '{files+=$1; inserted+=$4; deleted+=$6} END {print a, " files changed: ", files, "lines inserted: ", inserted, "lines deleted: ", deleted }'
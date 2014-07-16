#!/bin/sh

workers="1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16"
echo "cores duration"
for w in ${workers}; do
    nb=`./verify_fuzz.sh v1.cspec noVMsOnOfflineNodes 1x1 impl -t 60 -v -p ${w}|cut -d' ' -f1|cut -d'/' -f2`
	echo ${w} ${nb}
done

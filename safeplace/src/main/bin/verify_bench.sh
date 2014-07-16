#!/bin/sh

OUTPUT=$1
mkdir -p ${OUTPUT}
shift
echo "$*" > ${OUTPUT}/guards.txt
#Verifications
for c in core states VM2VM VM2PM; do
	echo "Benching ${c}..."
    ./verify_${c}.sh $*> ${OUTPUT}/${c}.data
done

tar cfz ${OUTPUT}.tar.gz ${OUTPUT}
rm -rf ${OUTPUT}
echo "Done. Results available in ${OUTPUT}.tar.gz"
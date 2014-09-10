#!/bin/sh

#Specification length
./specStatistics.sh v1.cspec specLength.pdf

#Verifications
GUARD="-m 10000"
for c in core states VM2VM VM2PM; do
	echo "Benching ${c}..."
    ./verify_${c}.sh ${GUARD} > ${c}.data
done
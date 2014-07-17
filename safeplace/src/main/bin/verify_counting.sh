#!/bin/sh
VERIFIERS="impl impl_repair checker"
SPEC="v1.cspec"

CSTRS="runningCapacity maxOnline"
RESTRICTIONS="continuous discrete"
DOMAINS="--dom int=0..5"
echo "constraint verif restriction falseOk falseKo tests"
for CSTR in ${CSTRS}; do
    for VERIFIER in ${VERIFIERS}; do
        for R in ${RESTRICTIONS}; do
      	    f=`./verify_fuzz.sh ${SPEC} ${CSTR} 3x3 ${VERIFIER} ${DOMAINS} --${R} -v $* |tr "/" " "|cut -d' ' -f1,2,3`
            echo "${CSTR} ${VERIFIER} ${R} ${f}"
        done
    done
done

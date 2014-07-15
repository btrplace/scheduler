#!/bin/sh
VERIFIERS="impl impl_repair checker"
SPEC="v1.cspec"

CSTRS="ban fence root quarantine"
RESTRICTIONS="continuous discrete"
echo "constraint verif restriction failures tests"
for CSTR in ${CSTRS}; do
    for VERIFIER in ${VERIFIERS}; do
        for R in ${RESTRICTIONS}; do
            f=`./verify_fuzz.sh ${SPEC} ${CSTR} 3x3 ${VERIFIER} --${R} -v $* |tr "/" " "|cut -d' ' -f1,2`
            echo "${CSTR} ${VERIFIER} ${R} ${f}"
        done
    done
done

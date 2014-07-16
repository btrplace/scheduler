#!/bin/sh
VERIFIERS="impl impl_repair checker"
SPEC="v1.cspec"

CSTRS="online offline running sleeping ready killed"
echo "constraint verif restriction failures tests"
for CSTR in ${CSTRS}; do
    for VERIFIER in ${VERIFIERS}; do
        f=`./verify_fuzz.sh ${SPEC} ${CSTR} 1x1 ${VERIFIER} --discrete -v $* |tr "/" " "|cut -d' ' -f1,2`
        echo "${CSTR} ${VERIFIER} discrete ${f}"
    done
done

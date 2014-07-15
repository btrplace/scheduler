#!/bin/sh

VERIFIERS="impl impl_repair checker"
SPEC="v1.cspec"

CSTRS=`cat ${SPEC}|grep "^core constraint"|cut -d' ' -f 3|cut -d'(' -f1`
echo "constraint verif failures"
for CSTR in ${CSTRS}; do
    for VERIFIER in ${VERIFIERS}; do
        f=`./verify_fuzz.sh ${SPEC} ${CSTR} 1x1 ${VERIFIER} -v $* |tr "/" " "|cut -d' ' -f1,2`
        echo "${CSTR} ${VERIFIER} ${f}"
    done
done

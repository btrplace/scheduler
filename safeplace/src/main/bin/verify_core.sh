#!/bin/sh

VERIFIERS="impl impl_repair checker"
SPEC="v1.cspec"

CSTRS=`cat ${SPEC}|grep "^core constraint"|cut -d' ' -f 3|cut -d'(' -f1`
for CSTR in ${CSTRS}; do
    echo "--- core constraint ${CSTR} ---"
    for VERIFIER in ${VERIFIERS}; do
        echo "\tVerifying ${VERIFIER}"
        ./verify_fuzz.sh ${SPEC} ${CSTR} 1x1 ${VERIFIER} -t 30 -v
    done
done
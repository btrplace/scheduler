#!/bin/sh

VERIFIERS="impl impl_repair checker"

CSTRS="online offline running sleeping ready killed"
for CSTR in ${CSTRS}; do
    echo "--- constraint ${CSTR} ---"
    for VERIFIER in ${VERIFIERS}; do
        echo "\tVerifying ${VERIFIER}"
        ./verify_fuzz.sh --size 1x1 -v --discrete -t 5 -p 3 --verifier ${VERIFIER} $* ${CSTR}
    done
done
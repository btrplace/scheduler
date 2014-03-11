#!/bin/sh
CSTRS="noVMsOnOfflineNodes hostForVMs offline online running ready sleeping killed among ban fence runningCapacity resourceCapacity gather lonely maxOnline overbook preserve root quarantine sequentualVMTransitions spread split splitAmong"
VERIFIERS="impl impl_repair checker"
for CSTR in ${CSTRS}; do
    echo "--- constraint ${CSTR} ---"
    for VERIFIER in ${VERIFIERS}; do
        echo "\tVerifying ${VERIFIER}"
        ./verify.sh -v --restriction discrete --verifier ${VERIFIER} $* ${CSTR}
    done
done

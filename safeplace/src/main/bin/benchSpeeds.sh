#!/bin/sh

#!/bin/sh
for s in 3 4 5; do
    >&2 echo "Benching with ${s} element(s)"
    ./benchSpeed.sh --vms $s --nodes $s $*
done
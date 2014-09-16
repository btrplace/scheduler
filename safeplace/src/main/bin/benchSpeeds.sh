#!/bin/sh

#!/bin/sh
for s in 2 3 4 5 6 8 10 12 15; do
    >&2 echo "Benching with ${s} element(s)"
    ./benchSpeed.sh --vms $s --nodes $s $*
done
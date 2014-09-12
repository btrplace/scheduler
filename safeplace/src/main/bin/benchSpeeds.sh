#!/bin/sh

#!/bin/sh
for s in 3 4 5 10 20 50 100; do
    >&2 echo "Benching with ${s} element(s)"
    ./benchSpeed.sh --vms $s --nodes $s $*|sed "s,\(Bench\),${s} \1,"
done
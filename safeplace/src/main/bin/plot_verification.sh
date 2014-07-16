#!/bin/sh
./failureStats.R discrete $1/states.data $1/states.pdf
./failureStats.R continuous $1/core.data $1/core.pdf

for c in VM2VM VM2PM counting; do
    for r in continuous discrete; do
        ./failureStats.R ${r} $1/${c}.data $1/${c}-${r}.pdf
    done
done

#!/bin/sh
if [ $# -ne 1 ]; then
    echo "Usage: $0 dir"
    exit 1
fi
o=$1/img
mkdir -p ${o}
WD="safeplace/src/main/bin"

if [ -e "$1/testing-speed-trans.csv" ]; then
    echo "-- Testing speed w. transition --"
    ${WD}/testing-speed.R "$1/testing-speed-trans.csv" "${o}/testing-trans"
fi

if [ -e "$1/testing-speed-notrans.csv" ]; then
    echo "-- Testing speed simple transitions --"
    ${WD}/testing-speed.R "$1/testing-speed-notrans.csv" "${o}/testing-notrans"
fi

if [ -e "$1/errors.csv" ]; then
    echo "-- Error detection --"
    ${WD}/errors.R "$1/errors.csv" "${o}/errors.pdf"
fi

if [ -e "$1/restriction_stable.csv" ]; then
    echo "-- Discrete vs. Continuous --"
    ${WD}/restrictions.R "$1/restriction_stable.csv" "${o}/restriction"
fi

if [ -e "$1/verifier_stable.csv" ]; then
    echo "-- Spec vs. Checkers --"
    ${WD}/verifier.R "$1/verifier_stable.csv" "${o}/verifier"

fi

if [ -e "$1/mode_stable.csv" ]; then
    echo "-- Rebuild vs. Repair --"
    ${WD}/mode.R $1/mode_stable.csv ${o}/mode
fi

if [ -e "$1/fuzzer.csv" ]; then
    echo "-- Fuzzer --"
    ${WD}/fuzzer.R "$1/fuzzer.csv" "${o}/fuzzer.pdf"
fi

if [ -e "$1/inv.csv" ]; then
    echo "-- Invariant length --"
    ${WD}/specLength.R "$1/inv.csv" "${o}/inv-length.pdf"
fi

if [ -e "$1/func.csv" ]; then
    echo "-- Function length --"
    ${WD}/funcLength.R "$1/func.csv" "${o}/func-length.pdf"
fi

if [ -e "$1/func.csv" ]; then
    echo "-- Function frequence --"
    ${WD}/funcFreq.R "$1/func-freq.csv" "${o}/func-freq.pdf"
fi

if [ -e "$1/sloc.csv" ]; then
    echo "-- Test length --"
    ${WD}/testing-len.R "$1/sloc.csv" "${o}/testing-sloc.pdf"
fi
wait

#!/usr/bin/env bash
#Quick script to run 10 times the 'standard benchmarks'
#The output is the current commit ID or a folder given as a parameter

#The run command runs the benchmark and store the resulting numbers
#The stats command computes the average numbers
export MAVEN_OPTS="-Xmx6G -Xms6G"
OUTPUT=`git rev-parse --short HEAD`
if [ $# -eq 2 ]; then
    OUTPUT=$2
fi

case $1 in
run)
    mvn -f ../ install -DskipTests -Dgpg.skip||exit 1
    mvn exec:java -Dexec.mainClass="org.btrplace.bench.Bench" -Dexec.args="-n 10 -l src/test/resources/std-perf/std-perf.txt -v 1 --repair --timeout 300 -o ${OUTPUT}" ||exit 1
    echo "Statistics available in ${OUTPUT}. Run '$0 stats ${OUTPUT}' to generate them"
    ;;
stats)
    #summary per bench
    for t in nr6 li6; do
        echo "---------- ${t} ----------"
        DTA=`grep ${t} ${OUTPUT}/scheduler.csv| awk -F';' '{ core+=$3; spe +=$4; solve+=$5; n++ } END { if (n > 0) printf "%d,%d,%d", core / n, spe / n, solve / n; }'`
        echo "${OUTPUT},${DTA}"
    done
    ;;
*)
    echo "Unsupported operation: $1.\n"
    echo "$0 [run|stats] output?"
    exit 1
esac

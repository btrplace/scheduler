#!/usr/bin/env bash
#Quick script to run 10 times the 'standard benchmarks'
#The output is the current commit ID or a folder given as a parameter

#The run command runs the benchmark and store the resulting numbers
#The stats command computes the average numbers
export MAVEN_OPTS="-Xmx4G -Xms4G"
OUTPUT=`git rev-parse --short HEAD`
if [ $# -eq 2 ]; then
    OUTPUT=$2
fi

case $1 in
run)
    echo "Compiling ..."
    mvn -q -f ../ clean install -DskipTests -Dgpg.skip||exit 1
    #We run 11 times and get rid of the first run because the JIT will not be activated
    echo "Running the benchmark"
@    mvn exec:java -Dexec.mainClass="org.btrplace.bench.Bench" -Dexec.args="-l src/test/resources/std-perf/std-perf.txt --repair --timeout 300 -v 1 -o ${OUTPUT}" ||exit 1
    echo "Statistics available in ${OUTPUT}. Run '$0 stats ${OUTPUT}' to generate them"
    ;;
stats)
    #summary per bench
    for t in nr6 li6; do
        echo "---------- ${t} ----------"
        DTA=`tail -n 10 ${OUTPUT}/scheduler.csv|grep ${t} |awk -F';' '{ core+=$3; spe +=$4; solve+=$5; n++ } END { if (n > 0) printf "%d,%d,%d", core / n, spe / n, solve / n; }'`
        echo "${OUTPUT},${DTA}"
    done
    ;;
*)
    echo "Unsupported operation: $1.\n"
    echo "$0 [run|stats] output?"
    exit 1
esac

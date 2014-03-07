#!/bin/sh

JAVA_OPTS="-Dlogback.configurationFile=conf/logback.xml"
#Define the classpath
JARS=`ls lib/*.jar`

for JAR in $JARS; do
 CLASSPATH=$JAR:$CLASSPATH
done

OUT=`mktemp /tmp/exemple.XXXXXX`
java $JAVA_OPTS -cp $CLASSPATH btrplace.solver.api.cstrSpec.SpecStatistics $1 > $OUT
./specLength.R $OUT $2
rm -rf ${OUT}

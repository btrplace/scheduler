#!/bin/sh

JAVA_OPTS="-Dlogback.configurationFile=conf/logback.xml"
#Define the classpath
JARS=`ls lib/*.jar`

for JAR in $JARS; do
 CLASSPATH=$JAR:$CLASSPATH
done

OUT=`mktemp /tmp/exemple.XXXXXX`
java $JAVA_OPTS -cp $CLASSPATH btrplace.safeplace.DumpSpec|sed "s/^[^=]*= //g"| awk '{ print length }' > $OUT
./specLength.R $OUT $1
rm -rf ${OUT}

#!/bin/sh

#!/bin/sh

JAVA_OPTS="-Dlogback.configurationFile=conf/logback.xml"
#Define the classpath
JARS=`ls lib/*.jar`

for JAR in $JARS; do
 CLASSPATH=$JAR:$CLASSPATH
done

java $JAVA_OPTS -cp $CLASSPATH btrplace.safeplace.BenchSpeed $*

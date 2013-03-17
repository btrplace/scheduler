#!/bin/sh

#Define the classpath
JARS=`ls jar/solver-bundle*.jar`

for JAR in $JARS; do
	CLASSPATH=$JAR:$CLASSPATH
done

JAVA_OPTS="-classpath $CLASSPATH.:src"

if [ $# -ne 1 ]; then
	echo "Usage: $0 tutorial_name"
	echo "\trun: compile and run the expected tutorial"
	echo "\treset: delete all modifications made on the tutorial"
	exit 1
fi

INPUT="src/btrplace/examples/"

#compile
javac $JAVA_OPTS $INPUT/Launcher.java $INPUT/Example.java $INPUT/$1.java || exit 1
java $JAVA_OPTS btrplace.examples.Launcher btrplace.examples.$1 || exit 1
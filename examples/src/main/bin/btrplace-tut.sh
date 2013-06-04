#!/bin/sh

#Define the classpath
JARS=`ls jar/solver-bundle*.jar`

for JAR in $JARS; do
	CLASSPATH=$JAR:$CLASSPATH
done

JAVA_OPTS="-classpath $CLASSPATH.:src"

if [ $# -ne 1 ]; then
	echo "Usage: $0 tutorial_name"
	echo "Available tutorials:"
	ls src/btrplace/examples/*.java|\
	cut -d "." -f1|cut -d'/' -f4|\
	grep -v Launcher|grep -v Example
	exit 1
fi

INPUT="src/btrplace/examples/"

#compile
javac $JAVA_OPTS $INPUT/Launcher.java $INPUT/Example.java $INPUT/$1.java || exit 1
java $JAVA_OPTS btrplace.examples.Launcher btrplace.examples.$1 || exit 1
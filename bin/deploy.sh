#!/bin/bash
if [ -e etc/gpg ]; then
	PASSPHRASE="-Dgpg.passphrase=${GPG_PASSPHRASE}"
	SETTINGS="-q -s etc/sonatype.xml"
fi
mvn ${SETTINGS} clean javadoc:jar source:jar -Dgpg.keyname=BF1447AC ${PASSPHRASE} deploy ||exit 1

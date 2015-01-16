#!/bin/bash
if [ -e etc/gpg ]; then
	PASSPHRASE="-Dgpg.passphrase=${GPG_PASSPHRASE}"
	SETTINGS="-s etc/sonatype.xml"
fi
mvn ${SETTINGS} -Dgpg.keyname=BF1447AC ${PASSPHRASE} clean deploy |grep -v "^Generating"
exit $?

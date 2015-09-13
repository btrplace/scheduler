#!/bin/bash
if [ -e etc/gpg ]; then
	PASSPHRASE="-Dgpg.passphrase=${GPG_PASSPHRASE}"
	SETTINGS="-s etc/sonatype.xml"
fi
mvn ${SETTINGS} -DskipTests=true -Dmaven.repo.local=/tmp/cache -Dgpg.keyname=BF1447AC ${PASSPHRASE} javadoc:jar source:jar deploy
exit $?

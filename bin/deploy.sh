#!/bin/bash
if [ -e etc/private.key ]; then
	gpg --allow-secret-key-import --import etc/private.key
	PASSPHRASE="-Dgpg.passphrase=${GPG_PASSPHRASE}"
	SETTINGS="-s etc/sonatype.xml"
fi
mvn ${SETTINGS} clean javadoc:jar source:jar gpg:sign -Dgpg.keyname=BF1447AC ${PASSPHRASE} install deploy ||exit 1

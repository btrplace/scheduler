#!/bin/bash
gpg --import etc/private.key
mvn -s etc/sonatype.xml clean javadoc:jar source:jar gpg:sign -Dgpg.passphrase=${GPG_PASSPHRASE} deploy ||exit 1

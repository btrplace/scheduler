# Btrplace scheduler #

This repository contains the main sources of the flexible VM scheduler BtrPlace (see http://www.btrplace.org)

[![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/btrplace/chat?utm_source=share-link&utm_medium=link&utm_campaign=share-link) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.btrplace/scheduler/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.btrplace/scheduler) [![javadoc](https://javadoc.io/badge2/org.btrplace/scheduler%20/javadoc.svg)](https://javadoc.io/doc/org.btrplace/scheduler%20)


![Build](https://github.com/btrplace/scheduler/actions/workflows/continuous-integration.yml/badge.svg)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/f748d074b9ad4108a7007c9ebb9a969d)](https://www.codacy.com/gh/btrplace/scheduler/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=btrplace/scheduler&amp;utm_campaign=Badge_Grade) [![Codacy Badge](https://app.codacy.com/project/badge/Coverage/f748d074b9ad4108a7007c9ebb9a969d)](https://www.codacy.com/gh/btrplace/scheduler/dashboard?utm_source=github.com&utm_medium=referral&utm_content=btrplace/scheduler&utm_campaign=Badge_Coverage) [![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=btrplace_scheduler&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=btrplace_scheduler)

Contact: fabien.hermenier@nutanix.com

## Usage ##

Releases are available via [Maven Central](http://search.maven.org/#search%7Cga%7C1%7Corg.btrplace).

Snapshot versions are only available through a dedicated repository. Add the following entry in your `pom.xml` to get
them:

```xml
<repositories>
        <repository>
            <id>sonatype-snapshots</id>
            <url>https://oss.sonatype.org/content/repositories/snapshots</url>
            <layout>default</layout>
        </repository>
</repositories>
```

Next, just declare the useful dependencies:

* `org.btrplace:scheduler-api`: the API defining a VM scheduler and the element it manipulates
* `org.btrplace:scheduler-choco`: the default implementation of the VM scheduler using the Constraint Programming
solver Choco
* `org.btrplace:scheduler-json`: to serialize models using JSON
* `org.btrplace:scheduler-split`: to split the instances to solve
* `org.btrplace:btrpsl`: a scripting language to express constraints
* `org.btrplace:bench`: a simple CLI to perform benchmarks
* `org.btrplace:scheduler-examples`: the examples illustrated in the [tutorials](https://github.com/btrplace/scheduler/wiki/Tutorials) section

## Getting Started ##

See the [tutorials](https://github.com/btrplace/scheduler/wiki/Tutorials)

## General Documentation ##

See the [wiki](https://github.com/btrplace/scheduler/wiki)

## Contributing ##

Anyone can contribute to the project, from the source code to the documentation.
In order to ease the process, see the [contribution guide](CONTRIBUTING.md).

## Building from sources ##

Requirements:
* JDK 8+
* maven 3+

The source of the released versions are directly available in the `Tag` section.
You can also download them using github features.
Once downloaded, move to the source directory then execute the following command
to make the jar:

    $ mvn clean install

If the build succeeded, the resulting jars will be automatically installed in your local maven repository.


## Copyright ##

Copyright (c) The BtrPlace Authors. All rights reserved. Use of this source
code is governed by a LGPL-style license that can be found in the LICENSE.txt
file.

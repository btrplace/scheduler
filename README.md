# Btrplace scheduler #

This repository contains the main sources of the flexible VM scheduler BtrPlace (see http://www.btrplace.org)

[![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/btrplace/chat?utm_source=share-link&utm_medium=link&utm_campaign=share-link) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.btrplace/scheduler/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.btrplace/scheduler) [![javadoc](https://javadoc.io/badge2/org.btrplace/scheduler%20/javadoc.svg)](https://javadoc.io/doc/org.btrplace/scheduler%20)


[![Build Status](https://api.travis-ci.org/btrplace/scheduler.svg?branch=master)](https://travis-ci.org/btrplace/scheduler) [![codecov.io](https://codecov.io/github/btrplace/scheduler/coverage.svg?branch=master)](https://codecov.io/github/btrplace/scheduler?branch=master) [![Codacy Badge](https://api.codacy.com/project/badge/grade/ccaa68ef1c474d4e9f079de2b10d2672)](https://app.codacy.com/manual/fhermeni/scheduler/dashboard) [![Total alerts](https://img.shields.io/lgtm/alerts/g/btrplace/scheduler.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/btrplace/scheduler/alerts/) [![Language grade: Java](https://img.shields.io/lgtm/grade/java/g/btrplace/scheduler.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/btrplace/scheduler/context:java)

Contact: fabien.hermenier@nutanix.com

## Usage ##

Releases are available via [Maven Central](http://search.maven.org/#search%7Cga%7C1%7Corg.btrplace).

Snapshot versions are only available through a dedicated repository.
Add the following entry in your `pom.xml` to get them:

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
Copyright (c) 2019 University of Nice-Sophia Antipolis. See `LICENSE.txt` for details

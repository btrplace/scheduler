# Btrplace scheduler #

This repository contains the main sources of the flexible VM scheduler BtrPlace (see http://www.btrplace.org)

[![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/btrplace/chat?utm_source=share-link&utm_medium=link&utm_campaign=share-link) [![Build Status](https://api.travis-ci.org/btrplace/scheduler.svg)](https://travis-ci.org/btrplace/scheduler) [![codecov.io](https://codecov.io/github/btrplace/scheduler/coverage.svg?branch=master)](https://codecov.io/github/btrplace/scheduler?branch=master)

Contact: fabien.hermenier@unice.fr

## Usage ##

Releases are available via Maven Central (see http://search.maven.org/#search%7Cga%7C1%7Corg.btrplace).

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
* `org.btrplace:btrpsl`: a scripting language to express constraints
* `org.btrplace:bench`: a simple CLI to perform benchmarks
* `org.btrplace:scheduler-examples`: the examples illustrated in the [tutorials](https://github.com/btrplace/scheduler/wiki/Tutorials) section

## Getting Started ##

See the [tutorials](https://github.com/btrplace/scheduler/wiki/Tutorials)

## Documentation ##

### API documentation ###

The javadoc for every version is available as a jar in the repository.
the HTML javadoc is available at:

* http://www.btrplace.org/apidocs for the last release
* http://www.btrplace.org/apidocs-next for the next version to release (the code in the `master` branch)

### General documentation ###

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
Copyright (c) 2015 University of Nice-Sophia Antipolis. See `LICENSE.txt` for details

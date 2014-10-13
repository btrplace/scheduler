# Btrplace scheduler #

This repository contains the main sources of the flexible VM scheduler BtrPlace (see http://www.btrplace.org)

[![Build Status](https://api.travis-ci.org/btrplace/scheduler.svg)](https://travis-ci.org/btrplace/scheduler)

Contact: fabien.hermenier@unice.fr

## Usage ##

### Inside a maven project ###

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

## Documentation ##

The javadoc for every version is available as a jar in the repository.
The javadoc for the last released version is always available at http://www.btrplace.org/apidocs

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
Copyright (c) 2014 University of Nice-Sophia Antipolis. See `LICENSE.txt` for details

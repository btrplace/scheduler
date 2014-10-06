# Btrplace solver #

This repository contains the main sources of the flexible VM scheduler BtrPlace (see http://www.btrplace.org)

[![Build Status](https://api.travis-ci.org/btrplace/scheduler.svg)](https://travis-ci.org/btrplace/scheduler)

Contact: fabien.hermenier@unice.fr

## Usage ##

### Inside a maven project ###

The maven artifacts are in private repositories so you have first to edit your `pom.xml` to declare them:

```xml
<repositories>
    <repository>
        <id>btrp-releases</id>
        <url>http://btrp.inria.fr/repos/releases</url>
    </repository>
    <repository>
        <id>btrp-snapshots</id>
        <url>http://btrp.inria.fr/repos/snapshot-releases</url>
    </repository>
</repositories>
```

Next, just declare the useful dependencies:

* `btrplace:solver-api`: the API defining a VM scheduler and the element it manipulates
* `btrplace:solver-choco`: the default implementation of the VM scheduler using the Constraint Programming
solver Choco
* `btrplace:solver-json`: to serialize models using JSON
* `btrplace:btrpsl`: a scripting language to express constraints
* `btrplace:bench`: a simple CLI to perform benchmarks

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

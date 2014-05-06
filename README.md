# Btrplace solver #

This repository contains the main sources of the flexible VM placement
algorithm btrPlace (http://btrp.inria.fr)

[![Build Status](http://btrp.inria.fr:8080/jenkins/buildStatus/icon?job=DEV btrplace-solver)](http://btrp.inria.fr:8080/jenkins/job/DEV%20btrplace-solver/)

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

Next, just declare the dependency:

```xml
<dependency>
   <groupId>btrplace</groupId>
   <artifactId>solver-bundle</artifactId>
   <version>0.38.1</version>
</dependency>
```

`btrplace:solver-bundle` is an aggregate of different sub-modules. If you don't need all of then, it is still possible
 to declare each dependency separately. In practice, `btrplace:solver-bundle` is composed of:

* `btrplace:solver-api`: the API defining a reconfiguration algorithm and the element it manipulates
* `btrplace:solver-choco`: the default implementation of a reconfiguration algorithm using the Constraint Programming
solver Choco
* `btrplace:solver-json`: to serialize models using JSON

### Inside a non-maven project ###

For each version of Btrplace, a bundle that contains the three basics artifacts and their dependencies is made available.
The jar can be downloaded from this URL:

* for releases: http://btrp.inria.fr/repos/releases/btrplace/solver-bundle
* for snapshot-releases: http://btrp.inria.fr/repos/snapshot-releases/btrplace/solver-bundle

## Documentation ##

### API documentation for released version

The Javadoc for the released versions is always available there:

* aggregated Javadoc: http://btrp.inria.fr/solver
* for sub-modules only:
    * http://btrp.inria.fr/solver-api/
    * http://btrp.inria.fr/solver-json/
    * http://btrp.inria.fr/solver-choco/

`apidocs` always refers to the last release.

### API documentation for the current snapshot version

* aggregated Javadoc: http://btrp.inria.fr/apidocs-snapshot
* for sub-modules only:
    * http://btrp.inria.fr/solver-api/apidocs-snapshot
    * http://btrp.inria.fr/solver-json/apidocs-snapshot
    * http://btrp.inria.fr/solver-choco/apidocs-snapshot


## Building from sources ##

Requirements:
* JDK 7+
* maven 3+

The source of the released versions are directly available in the `Tag` section.
You can also download them using github features.
Once downloaded, move to the source directory then execute the following command
to make the jar:

    $ mvn clean install

If the build succeeded, the resulting jars will be automatically installed in your local maven repository.


## Copyright ##
Copyright (c) 2013 University of Nice-Sophia Antipolis. See `LICENSE.txt` for details

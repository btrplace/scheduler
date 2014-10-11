Entropy to Btrplace
===============================

This module allows to convert configurations in the entropy protobuf format
to models and constraints that can be used by btrplace.

It can be used through an API or a standalone application.

## Usage as a standalone application ##

Download the last release of the application, and uncompress it.
The `entroPlace` script can then be used to convert entropy configurations
into btrplace instances:

    $ ./entroPlace
    Usage: converter src [dst] -o output
        src: the configuration in protobuf format to convert
  	    dst: an optional configuration that will be used to get the VMs and nodes state change
  	    output: the output JSON file. Ends with '.gz' for an automatic compression

## Embedding ##

The maven artifact `btrplace:fromEntropy` is available through a private repository
so you have first to edit your `pom.xml` to declare it:

```xml
<repositories>
    <repository>
        <id>btrp-releases</id>
        <url>http://btrp.inria.fr:8080/repos/releases</url>
    </repository>
    <repository>
        <id>btrp-snapshots</id>
        <url>http://btrp.inria.fr:8080/repos/snapshot-releases</url>
    </repository>
</repositories>
```

Next, just declare the dependency:

```xml
<dependency>
   <groupId>btrplace</groupId>
   <artifactId>from-entropy</artifactId>
   <version>1.1</version>
</dependency>
```

The API documentation is directly available online:

* Last snapshot version: http://btrp.inria.fr:8080/apidocs/snapshots/from-entropy
* Released versions: http://btrp.inria.fr:8080/apidocs/releases/btrplace/from-entropy

## Building from sources ##

Requirements:
* JDK 6+
* maven 3+

The source of the released versions are directly available in the `Tag` section.
You can also download them using github features.
Once downloaded, move to the source directory then execute the following command
to make the jar:

    $ mvn clean install

If the build succeeded, the resulting jar will be automatically
installed in your local maven repository and available in the `target` sub-folder.

Copyright
-------------------------------
Copyright (c) 2013 University of Nice-Sophia Antipolis. See `LICENSE.txt` for details

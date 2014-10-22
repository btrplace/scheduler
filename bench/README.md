Entropy to Btrplace
===============================

This module allows to execute and get the statistics of the btrplace solving process
by taking one or more btrplace instance json files as input.

It can be used through an API or a standalone application.

Examples of benchmarks can be found at https://github.com/btrplace/workloads-tdsc.

## Usage as a standalone application ##

Download the last release of the application, and uncompress it.
The `benchLauncher` script can then be used to launch a single bench from a
btrplace instance json file:

    $ ./benchLauncher [-r] [-m] [-t n_sec] -i file_name -o file_name
	-i (--input-json) VAL : the json instance file to read (can be a .gz)
	-m (--optimize)       : Enable the 'optimize' feature
	-o (--output) VAL     : Output to this file
	-r (--repair)         : Enable the 'repair' feature
	-t (--timeout) N      : Set a timeout (in sec)

The `seqBenchLauncher` script take as input a list of btrplace instances to
solve sequentially:

    $ ./seqBenchLauncher [-r] [-m] [-t n_sec] -i file_name
	-i (--input-list) VAL : the list of benchmarks file name
	-m (--optimize)       : Enable the 'optimize' feature
	-r (--repair)         : Enable the 'repair' feature
	-t (--timeout) N      : Set a timeout for each bench (in sec)

## Embedding ##

The maven artifact `btrplace:bench` is available through a private repository
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
   <artifactId>bench</artifactId>
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

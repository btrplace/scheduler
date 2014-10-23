Btrplace Benchmarks Launcher
===============================

This module allows to execute and get the statistics of the btrplace solving process
by taking one or more btrplace instance json files as input.

It can be used through an API or a standalone application.

Examples of benchmarks can be found at https://github.com/btrplace/workloads-tdsc.


## Integration ##

Add the following dependency in your `pom.xml`:
```xml
<dependency>
	<groupId>org.btrplace</groupId>
	<artifactId>bench</artifactId>
	<version><!-- the version you want --></version>
</dependency>
```

## Building from sources ##

Requirements:
* JDK 8+
* maven 3+

The source of the released versions are directly available in the `Tag` section.
You can also download them using github features.
Once downloaded, move to the source directory then execute the following command
to make the jar:

    $ mvn clean install

If the build succeeded, the resulting jar will be automatically
installed in your local maven repository and available in the `target` sub-folder.


## Usage as a standalone application ##

Download or build the last release of the application, and uncompress it.
The `benchLauncher` script can then be used to launch a single bench from a
btrplace instance json file:

    $ ./benchLauncher [-r] [-m] [-t n_sec] -i file_name -o file_name
	-i (--input-json) VAL : the json instance file to read (can be a .gz)
	-m (--optimize)       : Enable the 'optimize' feature
	-o (--output) VAL     : Output to this file
	-r (--repair)         : Enable the 'repair' feature
	-t (--timeout) N      : Set a timeout (in sec)

The `seqBenchLauncher` script take as input a list of btrplace instances to
solve sequentially, the output files are generated in the same folder than the
input json file:

    $ ./seqBenchLauncher [-r] [-m] [-t n_sec] -i file_name
	-i (--input-list) VAL : the list of benchmarks file name
	-m (--optimize)       : Enable the 'optimize' feature
	-r (--repair)         : Enable the 'repair' feature
	-t (--timeout) N      : Set a timeout for each bench (in sec)

The main output consists of a .csv file, containing the following informations:

Informations about the computed plan:
* planDuration
* planSize
* planActionsSize

Informations about the reconfiguration algorithm:
* craStart
* craNbSolutions
* craSolutionTime
* craCoreRPBuildDuration
* craSpeRPDuration
* craSolvingDuration
* craNbBacktracks
* craNbConstraints
* craNbManagedVMs
* craNbNodes
* craNbSearchNodes
* craNbVMs

Also, the full reconfiguration plan including all actions is written to a .plan file.


Copyright
-------------------------------
Copyright (c) 2014 University of Nice-Sophia Antipolis. See `LICENSE.txt` for details

# Btrplace solver examples #

This maven modules contains the tutorials related to BtrPlace.
The complete documentation attached to each tutorial is available on
the [BtrPlace Wiki](https://github.com/fhermeni/btrplace-solver/wiki/Tutorials).

The tutorials can be compiled and executed as a standalone application on your machine.
The following describe how to compile and how to use the application.

## Download the application ##

The last version of the standalone application is available on the following URL.
The archive contains the solver bundle and the tutorial sources. `README.txt` details
how to use the application.

* http://btrp.inria.fr/repos/snapshot-releases/btrplace/solver-examples/0.38-SNAPSHOT/

## Building from sources ##

Requirements:
* JDK 6+
* maven 3+

The source of the released versions are directly available in the `Tag` section.
You can also download them using github features.
Once downloaded, move to the source directory then execute the following command
to make the distribution:

    $ mvn clean assembly:assembly

The distribution archive will be available in the `target` directory.

## Copyright ##
Copyright (c) 2013 University of Nice-Sophia Antipolis. See `LICENSE.txt` for details

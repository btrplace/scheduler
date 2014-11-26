# Btrplace Specification Language #

The btrplace specification language (btrpsl) allows to express constraints
related to the placement of virtual machines in a datacenters.

## Integration ##

Add the following dependency in your `pom.xml`:
```xml
<dependency>
   <groupId>org.btrplace</groupId>
   <artifactId>btrpsl</artifactId>
   <version><!-- the version you want --></version>
</dependency>
```

## Basic examples ##

### Describing a datacenter ###

The following script specifies a datacenter composed of 251 nodes. Nodes
are stacked by 40 into racks. 250 of the nodes are working nodes, dedicated
 to the hosting of client. They are labelled from `node-1` to `node-250`.
The last node is a service node and run some service VMs. This node is labelled
`node-frontend`. The whole datacenter can not run more than 2000 VMs
simultaneously while each node can not host more than 15 VMs at the same time.

```
namespace datacenter;

@node-[1..250,frontend] : xen<boot=60>;

$nodes = @node-[1..250,frontend];
cumulatedRunningCapacity($nodes, 2000);

for $n in $nodes {
    singleRunningCapacity($n, 15);
}

$R[1..7] = $nodes % 40;

export $nodes,$R[1..7] to *;
```

### Describing a virtualized application ###

Following script is a specification from a application administrator
that describes a 3-tiers Web applications. Each replica of a same
tier should be placed on a distinct nodes for fault tolerance to hardware
failures, while the last tier must be running into a single rack to have a
low latency. Last, the application administrator does not want its VMs to be
collocated with other VMs for security purpose.

```
namespace myApp;

import datacenter;

VM[1..10] : tinyInstance<clone,boot=7,halt=10>;
VM[11..20] : microInstance;
VM[21..24] : largeMemoryInstance;

$T1 = VM[1..10];
$T2 = VM[11..20];
$T3 = VM[21..24];

lonely(VM[1..24]);
for $t in $T[1..3] {
   spread($t);
}

among($T3, $datacenter.R[1..7]);
```

### Read a script ###

The following example parse a script, decorate a model and solve a problem with regards
to the stated constraints.

```java
//Set the environment
Model mo = new DefaultModel();

//Make the builder and add the sources location to the include path
ScriptBuilder scrBuilder = new ScriptBuilder(mo);

//Build the script
Script myScript = scrBuilder.build(...);

Scheduler ra = ...
ra.solve(mo, myApp.getConstraints());
```

## Copyright ##
Copyright (c) 2014 University of Nice-Sophia Antipolis. See `LICENSE.txt` for details

Tutorial
===========================

This document is a tutorial introduction to the basics of btrp. It is not a comprehensive
guide to the language; at this moment the document closest to that is the [Manual](manual.html).

All the scripts described here are available in the `examples` directory and should work out of the box.


Hello (Virtual) World
---------------------------

Let's start with the traditional useless examples to show the basics. This script is available in
'helloVWorld.btrp':
      
    namespace helloVWorld;

    {VM1, VM2, VM3, VM4} : tiny;

    $VMS = VM[1..4];
    spread($VMS);
    lonely($VMS);

This sample script declares the 'example.helloWorld' namespace. It first asks for four instances of the
virtual machines template 'tinyInstance'. Then, the variable '$VMS' is defined and associated to
the VMs VM1 to VM4. Finally, two placement constraints are specified. With 'spread', the VMs have
to be deployed on distinct nodes. With 'lonely', the VMs will not be colocated with VMs other than
VM1, VM2, VM3, or VM4.

### HelloVWorld, step by step

The 'namespace' instruction specifies the namespace identifier. This must be the first line
of the script. The last element of the namespace **must be** the name of the script, without the '.btrp' extension.

After the namespace declaration, we define a set of 4 virtual machines, labeled from VM1 to VM4. The virtual machines
are types to be an instance of the `tiny` template. A template specifies the size of a virtual machine.
In particular, its maximum resource consumption.

Each VM identifier in a namespace has to be unique. However it is restricted to the current namespace. So any identifier
as a local name and a fully qualified name. Here, 'VM1' is the local name of the identifier while 'example.helloVWorld.VM1'
is the fully qualified name. So, one can have a same local identifier in different namespace.

Third line declares a variable, named `$VMS`. Every variable starts with the dollar sign (`$`). The variable
is assigned to a set defined using a range of elements. Here, it is composed of the elements named from VM1 to VM4.

Last two lines declare a placement constraint. Here, each of the constraint takes only one argument as a parameter, that
is a set of virtual machines.


Advanced utilization
--------------------------

The first example describes a sample name that can be managed by BtrPlace. This section presents a more
complex example, using most of the construction facilities of the language. All the files are in the 'example/advanced'
folder.

This example first specifies a datacenter infrastructure. This infrastructure is then used by several application
administrators that want to have some vjobs deployed on it. Last, the datacenter administrator also have
some placement expectations related to the management of the datacenter.

To run the following examples. Either launch them from the directory `examples/advanced` or include `examples/advanced`
in the includes path.

### Specification of the datacenter ###

The sample datacenter is composed of 256 servers stacked by 50 into racks. The script `examples/advanced/datacenter.btrp` makes
the description.

    namespace datacenter;

    $servers = @srv-[0x1..0xff, frontend];

    $racks = $servers % 50;

    export $racks to *;
    export $servers to admin.*;

The variable `$server` is declared and point to the set of servers. An element is considered as a server if it starts
with the arobace character `@`. The name of a server should be its hostname. In that sense, it is not related to the
namespace.

The set is defined using a sequence of
elements generated with an hexadecimal iteration, and an enumeration. The set will be composed of 256 servers: from
'srv-1' to 'srv-ff' and 'srv-frontend'.

On the next line, the set pointed by $server is parted into sets of 50 elements to represent the different racks that
composed the infrastructure. The variable `$racks` is declared to point to the resulting set.
`$racks` may also be written the following way:

    $racks = {@srv-[0x1..0x32], @srv-[0x33..0x64],
              @srv-[0x65..0x96], @srv-[0x97..0xC8],
              @srv-[0xC9..0xFA], @srv-[0xFB..0xFF]} + {@srv-frontend}};


The variables declared here may be useful by the clients or the datacenter administrator to express placement
constraints. So the last two lines export theses variables using the `export` keyword. `$racks` will be available for
anyone. `$servers` will be available for any vjobs that belong to the namespace `admin`.

Further informations about elements composing this declaration: [namespace](namespace.html), [set definition](set.html),
[export](export.html)

### Specification of a client script@ ###

Here, we consider an application administrator that want to deploy a highly-available 3-tiers application on the
datacenter. Every replicas in a tier have to be deployed on a distinct node to provide fault tolerance, while the
service running in the last tier is stateful and should be placed on nodes closely connected to provide a good network
latency to their synchronization protocol. The example script is available into `examples/advanced/clients/app1.btrp`.

    namespace clients.app1;

    import datacenter;

    VM[1..30] : large<clone, boot=10, halt=5>;

    $T[1..3] = VM[1..30] / 3;

    for $t in $T[1..3] {
    	spread($t);
    }

    root($me);
    among($T3,$datacenter.racks);

    export $me to admin.*;
    
Here, the application administrator uses the `import` keyword to import all the exported variables from `datacenter`.
So, the variable `$racks` will  be available through it fully qualified name in this script, but not the variable `$servers` as the export restriction
is not satisfied.

The administrator asks then for 30 VMs of type `large`. Each instantiation is then parametrized with 3 options.
First, `clone` indicates to the plan module that the VMs can be re-instantiated to its new location when the live
migration is not cost effective. `boot` and `halt` options indicate then the estimated boot and halt duration of the VM.

3 variables are then created to point to the 3 tiers of the application. For this example, we use a set division
and multiple assignment to be more concise. We assign one variable to one set by specifying a set of 3 variables on the
left side and a set splitted up in 3 sets on the right side. This is equivalent to the followings :

    {$T1, $T2, $T3} = VM[1..30] % 3;
    {$T1, $T2, $T3} = {VM[1..10], VM[11..20], VM[21..30]};

To maintain fault tolerance to hardware
failures, all the VMs that belong to a same tier must be on distinct servers. The `spread` constraints is available
for that purpose. Here, using a loop, the administrator specifies one constraint per tier. The `for` loop allows to
iterate over a set. Here, the set is dynamically constructed using a range of variable, `$T[1..3]` is then equivalent
to `{$T1, $T2, $T3}`.

With the constraint `root`, the administrator disallows the VMs in the variable `$me` to be moved from their current
server if they are already running. The variable `$me` is a special variable that is created automatically when
the script is interpreted. It is a set that contains all the VMs declared in the script.

With the constraint `among`, the administrator indicates that all the VMs from $T3 must be one exactly one set of
servers, among the sets given in `$racks`. This is motivated by the service that is running the third tier.

The last line uses the `export` statement to export all the VMs belonging to the vjob using the `$me` variable.
There will be available using a variable with a name equals to the namespace of the vjob. This will allow the datacenter
administrator to administrate its datacenter. In practice, this line may be added automatically once the vjob submmitted
to be sure that the datacenter administrator will be available to manage the vjob.

Further informations about elements composing this declaration: [import](import.html), [template](template.html),
[for](for.html), [constraints](constraints.html)


### Datacenter administrator constraints ###

Last script of this advanced tutorial denotes a system administrator that want to express some placement constraints
related to the use of its datacenter. The example script is available into `examples/advanced/admin/app1.btrp`.

    namespace admin.sysadmin;

    import datacenter;
    import clients.*;

    BtrPlace : large;

    root(BtrPlace);
    fence(BtrPlace,@srv-frontend);
    lonely(BtrPlace);

    fence($clients, $servers - {@srv-frontend});

    for $rack in $racks {
    	cumulatedRunningCapacity($rack, 1000);
    }

    for $n in $servers {
    	cumulatedRunningCapacity($n, 10);
    }

    ban($clients, @srv-210);


First, the datacenter administrator imports the datacenter namespace as it will operate on the infrastructure,
then he imports all the namespace belonging to clients. A variable `$clients` is then automatically created
and contains every VMs that was exported by the vjobs belonging to the `clients` namespace.

The administrator run BtrPlace in a virtual machine. The virtual machine, named `BtrPlace`, is an instance of the
 `large` template.

Using a `root` constraint, it disallows the VMs to be moved as it may distrib the VM performance. The virtual machine
 is then fenced on the node `srv-frontend` using a `fence` constraint. Last, the VM is isolated from all the others
 VMs in the datacenter using the `lonely` constraint. This is motivated by a security purpose: the datacenter
 administrator want to have the clients VMs and the service VMs on distinct nodes to avoid a local exploit that
 break an hypervisor, attack a service VM for a client VM.

The `fence` constraint indicates the clients VMs are fenced into the servers in `$servers` minus the server
`srv-frontend`. Note that is constraint is redundant. Indeed, with the previous `lonely` and `root` constraint, no
clients VMs may be deployed on `srv-frontend`.

Using `capacity` constraints, the datacenter administrator restricts first the maximum hosting capacity of each
rack to 1000 VMs and the hosting capacity of each server to 10 VMs.

Finally, a `ban` constraint is used to remove and prevent all the clients VMs to be hosted on the server `srv-210`.
This is for a maintenance purpose. Once the server will be available again, this constraint will be removed.





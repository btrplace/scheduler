BtrPlace Specification Language Manual
======================================


The btrplace specification language (btrpsl) is a descriptive language
to specify constraints related to the placement of virtual machines
in a datacenters.

This language is dedicated to datacenters administrators and applications administrators
that use [Entropy](http://entropy.gforge.inria.fr) to manage their nodes and virtual machines.


General structure
--------------------------------------

The structure of a BtrPlace script is quite common:
- a statement ends with ";"
- C-style (//) and C++-style (/* */) comments are allowed

Values
--------------------------------------

Btrplace support only three kinds of values: integer, nodes and VMs. Here, a node or a VM value point to the identifier of a VM. It can be its name or its UUID.

### Integers

BtrPlace supports decimal integers, octal integers and hexadecimal integers. The following is a pseudo EBNF defining the accepted integers:

 integer     = decimal_integer | octal_integer | hex_integer .
 decimal_integer = ( "1" .. "9" ) ( decimal_digit )* .
 octal_integer   = "0" ( octal_digit )* .
 hex_integer     = "0" ( "x" | "X" ) hex_digit hex_digit* .
 decimal_digit = "0" ... "9" .
 octal_digit   = "0" ... "7" .
 hex_digit     = "0" ... "9" | "A" ... "F" | "a" ... "f" .

Here is some example of valid integers :

<source lang="perl">
 42
 0600
 0xBadFace
 1701411
</source>

### Virtual Machines (VMs) and nodes

VMs and nodes are considered as values by btrPlace. A value is then either the textual name of the element or its UUID.

Currently, there is no namespace for the elements, which means each element must have a unique name. At least two namespace (one for the VMs, one for the nodes) will be available later. The syntax to specify the name of an element is mostly equivalent to the RFC XXX specifying domain names. Only the character "-" is prohibited inside a name.
Another solution to specify an element is to use its UUID[REF]. The following is the pseudo EBNF that specify a VM or node identifier:

 element = uuid | litteral .
 uuid =  "u" hex_digit{8} "-" hex_digit{4} "-" hex_digit{4} "-" hex_digit{4} "-" hex_digit{12} .
 letter = "a" .. "z" | "a" .. "z";
 domain = letter (letter|decimal_digit)*;
 litteral = domain ("." domain)*;

Here is some valid identifiers for VMs or nodes:

<source lang="perl">
 u12345678-1234-EAD2-AAED-234F-1234567890AB
 node1
 node1.myDatacenter.com
</source>

Variables
---------------------------------------

A variable is always prefixed by a dollar "$" character. The following is the pseudo EBNF that specify a variable identifier:

 variable = "$" letter (letter|decimal_digit)*;

Here is some valid variable names :

<source lang="perl">
 $foo
 $Bar
 $foo2
</source>

Before being use, a variable must be defined.

Types
---------------------------------------

Btrplace is not explicitly typed. The type of each litteral is inferred by the interpreter. Currently, accepted types are constant and set.

### Constant type ###

A constant type denotes an integer, a node or a VM. The type of the variable is then inferred from the type of the specified constant.
If a variable is redefined, its type can be changed. In the following examples, litterals starting with "VM" are VMs while litteral starting with "N" are nodes.

<source lang="perl">
 $V1 = VM1; //$V1 has constant type VM
 $V2 = N2; //$V2 has constant type node
 $V1 = N3; //Now $V1 has constant type Node
</source>

### Set type ###

A set is an ordered suite of unique elements. An element is either a constant or a set. However, elements in a same set must have the same type.
Sets can be defined either in extension, using a range or an enumeration of elements. The following is the pseudo EBNF that specify a set.

 set = exploded | range | enum .
 content = set | element | integer .
 exploded = "{" element* "}" .
 range = .
 enum = .

In the following examples, litterals starting with "VM" are VMs while litteral starting with "N" are nodes.

<source lang="perl">
 $T1 = {VM1, VM2, VM3}; //$T1 is a set of 3 VMs
 $T2 = {0xFF, 5}; //$T2 is a set of 2 integers
 $T3 = {N1}; //$T3 is a set composed of 1 node.
</source>

Following set definition are not accepted

<source lang="perl">
 {VM1, N2, VM3}; // We can not mix elements with different type, here, 2 VMs and 1 node
 {VM1, {VM2, VM3}}; //We can not mix constants with sets
 {{VM1, VM2}, {{VM3}}}; //Here the set is not homogeneous. All the subsets does not have the same depth.
</source>


Properties of variables and values
-------------------------------------

- Two sets are equals if there content is equivalent, without consideration of their order

- Variables are equals if there content are equivalent


Variables and values
-------------------------------------

=== Supported values ===
Btrplace manages nodes and VMs. Their identifier, is then considered as a value. nodes and virtual machines are then supposed to
have a unique name to prevent conflicts. In addition, supported values are integer than can be expressed in different bases.

* node or virtual machine name:
* base 10 integer:
* base 16 integer:



### Variables ###

Variables are prefixed with a dollar sign ($). There is no need to declare them. Variables are not explicitly typed, the type is
inferred from its content.

### Sets ###

Btrplace massively relies on set theory and most of the variables are sets. This is a necessary as a lot of constraints
manipulate set of virtual machines or set of nodes. Several primitives are then available to easily specify sets
and manipulate them. Primitives relies mostly on definition of range of element using common naming convention
and set theory to compose sets.

A set may be build in several way, either in extension, using a range of nodes or using union.

### Set definition ###
'''in extension'''

<source lang="perl">
 $S1 = {VM1, VM2, VM3, VM4}
</source>

Here, we define a set in extension and make the variable \$S1 points on it. The set is composed
of 4 elements, named VM1, VM2, VM3, and VM4

'''using range of nodes'''

<source lang="perl">
 $S2 = VM[1..4]
 $S3 = V[M1, 3]
</source>

Here, we define the set using a range of elements and make the variable \$S2 points on it. We only
specify the start and the end of the range in braces. This will create a value per possible increment.
\$S2 will then contains 4 elements, named VM1, VM2, VM3, and VM4. The set \$S3 is
also defined with a range of nodes, but with an enumeration. Here, it will be composed of
the elements VM1 and VM3.


### Operations on values ###

Traditional arithmetic operations are available between numbers: addition '+', substraction '-', mutliplication '*', division '/', remainder '%', and power '^'
<source lang="perl">
 $x = 5 + 2* 3; //$x == 11
 $y = $x % 3; //$y == 2
 $z = -$y^3; //$z == -8
</source>

### Operations on sets ###

Arithmetic operations ease the manipulation of set of elements.

'''Union of sets using '+' '''

Traditional union between two sets having the same type. Duplicates elements are ignored.

<source lang="perl">
 $T1 = {VM1, VM2, VM3, VM4};
 $T2 = {VM4, VM5};
 $RES = $T1 + $T2; // $RES == {VM1, VM2, VM3, VM4, VM5};
</source>

'''Difference of sets using '-''''

Traditional difference between two sets having the same type. Elements in the right operand that are not in the left operand
are ignored.

<source lang="perl">
 $T1 = {VM1, VM2, VM3, VM4};
 $T2 = {VM4, VM5};
 $RES = $T1 - $T2; // $RES == {VM1, VM2, VM3};
</source>

'''Dividing a set using '/''''

An operator two split a set into a specific number of partitions.
The result is a set containing each partition. The number of elements in each
partition has to be balanced to the possible.

<source lang="perl">
 $set = {{VM1, VM2}, {VM3, VM4}, {VM5, VM6}, {VM7}, {VM8, VM9}};
 $partitions = $set / 3; //$partitions == { {{VM1, VM2}, {VM3, VM4}}, {{VM5, VM6}, {VM7}}, {{VM8, VM9}} };
</source>

'''Exploding a set using '\''''
An operator to split a set into several partitions having at most the given size. Partitions must be filled to the possible.

<source lang="perl">
 $set = {VM1, VM2, VM3, VM4, VM5, VM6, VM7, VM8, VM9};
 $partitions = $set \ 4; //$partitions == { {VM1, VM2, VM3, VM4}, {VM5, VM6, VM7, VM8}, {VM9}};
</source>

'''Cartesian product of two sets using '*''''

Common cartesian product. The two operand must have the same type and degree

<source lang="perl">
 $T1 = {VM1, VM2};
 $T2 = {VM3, VM4};
 $RES = $T1 * $T2; //$RES == {{VM1, VM3}, {VM1, VM4}, {VM2, VM3}, {VM2, VM4}};
</source>


'''Cardinality of a set using '|''''

An operator to get the number of elements composing a set

<source lang="perl">
 $T1 = {{VM2, VM3}, {VM4}};
 $x = |$T1|; //$x == 2
</source>

Decomposition of a set
-----------------------------------

It is possible to decompose a set by performing some bindings between a set of variables and a common set. This also
allow to declare several variables simultaneously.

Using the equals '=' operator, it is possible to specify several variables pointing to some elements of a set.
In the following example, we assign the variables \$R1 to \$R3 to each elements composing \$X.

<source lang="perl">
 $X = {N[1..10], N[11..20], N[21..30]};
{$R1, $R2, $R3} = $X; //$R1 == N[1..10]; $R2 == N[11..20]; $R3 == [N21..30];
</source>

It is not a requirement to have the number of elements resulting decomposed set equals to
the number of elements composing the set to decompose. If the sets in the left operand have fewer elements,
then additional elements in the right set will be ignored. If the cardinality of the left set is superior to the cardinality
of the right set, then empty sets are created :

<source lang="perl">
 $X = {N[1..10], N[11..20], N[21..30]};
 {$R1, $R2} = $X; //$R1 == N[1..10]; $R2 == N[11..20];
 {$R5, $R6, $R7, $R8} = $X; //$R5 == N[1..10]; $R6 == N[11..20]; $R7 == [N21..30]; $R8 == {}
</source>


''''The blank identifier''''

As in Go, the blank identifier is available to ignore a value when decomposing a set. In this situation,
no binding to a variable is done

<source lang="perl">
 $X = {N[1..10], N[11..20], N[21..30]};
 {$R1,_,$R3} = $X; //$R1 == N[1..10]; $R3 == N[21..30];
</source>

Finally, definition using range of variables or enumeration of variables is possible when defining multiple variables from a set decomposition.

<source lang="perl">
$ALL_NODES = N[1..100];
$R[1..4] = $ALL_NODES / 4;
/*
$R1 = N[1..25];
$R2 = N[26..50];
$R3 = N[51..75];
$R4 = N[76..100];
*/
</source>

Constraint
------------------------------------

In Btrplace, a constraint can be assimilated to a common function in a programming language.
Each constraint has a unique name then a list of parameters given in parentheses.


'''Constraint signature''

The signature of each constraint is provided. it defines the name of the constraint and the list
of its accepted parameters. Each parameter must be typed. The following is the signature for a pseudo-constraint
named ''foo''

<source lang="perl">
 foo(vs : vmset, ns : nodeset, x : int)
</source>

The constraint ''foo'' takes 3 arguments as parameters. The first has to be a vmset ( a set of virtual machines),
the second parameter must be a nodeset ( a set of nodes), and the last parameter must be an int (an integer)

'''Type inferring'''

The following example show the usage of the constraint ''foo''.

<source lang="perl">
 $S1 = VM[1..3];
 $S2 = {N1, N2, N3};
 foo($S1, $S2, 5);
</source>

Here, we first define the sets \$S1 and \$S2 then we instantiate the constraint ''foo''. At this moment,
the set \$S1 is typed as a vmset, the set \$S2 is typed as a nodeset and 5 is typed as an integer.

Pre-defined variables
------------------------------------

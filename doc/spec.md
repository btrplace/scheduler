# Specification language

Here is an incomplete documentation about the language available to express invariants

## Constraint classifier

*  `core` indicates a constraint that is built-in (not removable)
* `discrete` indicates a constraint that only restricts a given model (not a complete reconfiguration plan)

## Builtin

### Types

* `vm`, `node`, `int`, `float` for atomic types
* `vmState` and `nodeState` for the elements states. See the lifecycles in `nodeLifeCyle.dot` and `VMlifeCycle.dot`. Nodes denotes
stable states while labels denote transitory states
* `col` for a collection with two specialisations: `set` and `list`. Collections are parametrized. For example, `set<vm>`

### Primitives

* `vms` all the VMs in the model
* `nodes` all the nodes in the model

### Builtin functions

* `nodeState(node) -> nodeState` returns the state of a node
* `vmState(vm) -> vmState` returns the state of a VM
* `host(vm) -> node` returns the node hosting the VM
* `hosted(n) -> set<vm>` returns the VMs hosted on the given node
* `colocated(vm) -> set<vm>` returns the VMs that are hosted on the same node than the given VM
* `card(col<?>) -> int` returns the number of elements in the given collection
* `sum(col<int>) -> int` return the sum of the integers in the given collection
* `packings(collection<?>) -> set<set<set<?>>>` returns all the possible packings for a given collection (a packing does not necessarily cover the original set)
* `lists(collection<?>) -> set<list<?>>` returns all the possible lists of a given collection.
* `range(list<?>) -> set<int>` returns all the indexes of a list


## Operators

* `x : y` element `x` is in the collection `y` (negate with `/:`). Following proposition is true: `1 : {1,2,3}`
* `x <: y` element `x` is included in the collection `y` (negate with `/<:`) Following proposition is true: `{1,2} <: {1,2,3}`
* `x = y` element `x` equals element `y` (negate with `/=`).
* `a & b` logical *and* between two propositions
* `a | b` logical *or* between two propositions
* `a --> b` logical *implication* between two propositions
* `a <--> b` logical *equivalent* between two propositions
* `+`,`-`,`*`,`/` plus, minus, multiply divide between integers. `+` and `-` also work for collections (union and difference)

## Binders

* `!(v : vms)` *for all* VM in `vms`
* `?(v : vms)` *it exists* v in `vms`



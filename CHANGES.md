Release notes
=======================

V0.6 - 14 dec. 2012
----------------------
- Events can be attached to actions to
inform nodes about additional operations to execute
- first event: Allocate

V0.5 - 10 dev. 2012
----------------------
- Fix toString() is some actions
- ReconfigurationPlan.size() -> ReconfigurationPlan.getSize()
- new action: Allocate to manage the resource allocation
- API improvement to provide additional checkers

v0.4 - 30 nov. 2012
----------------------
- InstantiateVM becomes ForgeVM
- new action: KillVM
- ReconfigurationAlgorithm.solve() takes the constraints to satisfy as a parameter.

v0.3 - 26 nov. 2012
----------------------
- bug fixes in DefaultReconfigurationPlan
- a package btrplace.solver dedicated to the reconfiguration algorithm
- upgrade to btrplace-model 0.4

v0.2 - 19 nov. 2012
-----------------------
- Clarify actions name
- better apidoc
- bug fixes
- some required getters were missing
- shutdownVM now set the VM back to the waiting state

v0.1 - 15 nov. 2012
-----------------------
- Initial release
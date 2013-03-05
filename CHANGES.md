Release notes
======================

version ???????
----------------------
- JSON conversion for plans
- improve performance

version 0.23 - 28 Feb 2013
----------------------
- code refactoring for the JSON parser

version 0.20 - 25 Feb 2013
----------------------
- merge repositories for solver-api, solver-choco and solver-json
  into a single repository

versions prior to 0.20
----------------------

The following changelogs were dedicated to each of the previous
repositories.

### solver-api ###

#### version 0.12 - 14 Feb 2013 ####
- An helper in DefaultAttribute to cast values if possible

#### version 0.11 - 08 Feb 2013 ####
- Restrict Attributes to only store basic primitives

#### version 0.10 - 06 Feb 2013 ####
- non-critical API update
- documentation and tests

####  version 0.9 - 30 jan. 2012 ####
- ModelView to customize a model with new domain-specific informations.
  ShareableResource becomes possible specialization of ModelView
- documentation fix

#### version 0.8 - 15 jan. 2012 ####
- Continuous resolution for Gather and Lonely
- Bug fixes

#### version 0.7 - 19 dec. 2012 ####
- Integrate btrplace.model
- new constraints : a total of 23 now
- bug fixes
- a better documentation for the constraints

#### version 0.6 - 14 dec. 2012 ####
- Events can be attached to actions to
inform nodes about additional operations to execute
- first event: Allocate

#### version 0.5 - 10 dev. 2012 ####
- Fix toString() is some actions
- ReconfigurationPlan.size() -> ReconfigurationPlan.getSize()
- new action: Allocate to manage the resource allocation
- API improvement to provide additional checkers

#### version 0.4 - 30 nov. 2012 ####
- InstantiateVM becomes ForgeVM
- new action: KillVM
- ReconfigurationAlgorithm.solve() takes the constraints to satisfy as a parameter.

#### version 0.3 - 26 nov. 2012 ####
- bug fixes in DefaultReconfigurationPlan
- a package btrplace.solver dedicated to the reconfiguration algorithm
- upgrade to btrplace-model 0.4

#### version 0.2 - 19 nov. 2012 ####
- Clarify actions name
- better apidoc
- bug fixes
- some required getters were missing
- shutdownVM now set the VM back to the waiting state

#### version 0.1 - 15 nov. 2012 ####
- Initial release


### solver-choco ###

#### 0.2 - 02 Feb 2013 ####
- The repair mode is back to simplify the problems.
- Bug fix in CGather

#### version 0.1.1 - 1 feb. 2013 ####
- Minor API fix to make the ViewMapper available.

#### version 0.1 - 31 jan. 2013 ####
- Initial release. Support all the constraints in solver-api 0.9


### solver-json ###

#### version 0.4 - 19 Feb 2013 ####
- bug fixes
- additional tools to ease the conversion

#### version 0.3 - 08 Feb 2013 ####
- Synchronize with solver-api

Coming next
=======================

- JSON serialization for the plan primitives
- Search heuristic for MinMTTR
- Partition detection

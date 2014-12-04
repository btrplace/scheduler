Release notes
======================

version ????????
----------------------
- update to choco 3.3.0

version 0.39 - 07 Oct 2014
----------------------
- btrpsl joined the repository as org.btrplace:btrpsl with version number 0.39
- project changed from btrplace:solver to org.btrplace:scheduler
  This reflect naming conventions required to be hosted on the central repository
  (and the new website)
- ReconfigurationAlgorithm interface renamed to Scheduler
- solver package renamed to scheduler
- ci made by travis
- Btrplace-solver is now relocated to git@github.com:btrplace/scheduler.git
- new module 'bench' to easily bench instances in JSON
- constraint SequentialVMTransitions becomes Seq
- continuous or discrete restriction mode for state constraints, ban, fence
- major performance improvement with a fast Packing constraint made by
  Sophie Demassey
- ChocoReconfigurationAlgorithmParams renamed to Parameters
- bug fixes
- move to Java 8
- new constraint NoDelay
- upgrade to choco-3.2.0
- remove the logback dependency

version 0.38.2 - 19 Jun 2014
----------------------
- Minor release to fix copyrights

version 0.38.1 - 06 May 2014
----------------------
- fix the release script ? (issue #35)
- the javadoc is now deployed according to the maven conventions
- fix copyright headers (Issue #41)

0.37 - 14 Apr 2014
----------------------
- minor bug fixes
- refactor choco.actionModel to choco.transition. Now xActionModel classes are xTransition classes.
  The TransitionFactory allows to plug your own classes to model transitions (issue #37)
- some package where renamed to fit conventions:
    - btrplace.solver.choco.runner.staticPartitioning is now btrplace.solver.choco.runner.disjoint
    - btrplace.solver.choco.constraint.minMTTR is now btrplace.solver.choco.constraint.mttr
    - btrplace.solver.choco.durationEvaluation is now btrplace.solver.choco.duration
- package model.constraint.checker merged into model.constraint
- views have been refactored
  - basic global constraints are now views too (issue #39)
  - it is possible to express dependencies between views. These dependencies are
    considered at building time in the solver
- improve the overall code quality a bit when possible and needed

0.36 - 28 Mar 2014
----------------------
- Homogenize constraints signature in API
- fix #32, #33
- fix some spell errors in the comments/API
- a basic NamingService to associate a unique name to elements (#34)
- minor improvements and bug fix in the API

version 0.35 - 18 Feb 2014
----------------------
- Move to choco 3. The scalability will be affected temporary but choco 2 is no longer maintained
- The variable labelling is now inferred from the verbosity level (#27)
- Simplify the signature of constraints to reduce the signature to the minimum needed (#29)
- SingleRunningCapacity and SingleResourceCapacity are now merged in RunningCapacity and ResourceCapacity respectively. (#24)

version 0.34 - 26 Nov 2013
----------------------
- Update dependencies
- Bug fix in NodeStateConstraint.toString()
- Additional getters to tune the JSON serialisation of ReconfigurationPlan

version 0.33 - 28 Oct 2013
----------------------
- Bug fix in the json un-marshalling
- API change: ModelViewConverter.getSupportedConstraint() becomes ModelViewConverter.getSupportedView()

version 0.32 - 12 Sep 2013
----------------------
- Bugs fix.
- Reduce the memory footprint of classes in the model package.
- [StaticPartitioning](http://btrp.inria.fr/apidocs/releases/btrplace/solver/last/index.html?btrplace/solver/choco/runner/staticPartitioning/StaticPartitioning.html):
   an experimental partitioning algorithm that splits problem from disjoint set of nodes
- API changes in Model, Instance, Mapping
- new constraint: [MaxOnline](http://btrp.inria.fr/apidocs/releases/btrplace/solver/last/index.html?btrplace/model/constraint/MaxOnline.html)
- API changes due to spell checking

version 0.31 - 16 Jul 2013
----------------------
- Bugs fix
- Optimization constraint is now a part of a reconfiguration algorithm
  at the API level
- It is possible to specify a dedicated solving method using InstanceSolver.
  This will be used later to integrate the parallel resolution of partitioned
  instances.


version 0.30 - 04 Jun 2013
----------------------
- Bugs fix
- Close #16, #18, #19
- MAJOR API change: Type system for elements. No more UUIDS.
- Simplify json package
- 2 new tutorials on customizing a Model and a ChocoReconfigurationAlgorithm.

version 0.29 - 07 May 2013
----------------------
- Multiple Bug fixes
- Fix regressions in the placement subProblems that appeared in 0.28
- Complete the forgeVM model. It is now required to declare
  a template for the VMs to forge using a "template" attribute
  in the model
- Fix bugs reported in #5, #10, #13, #14
- Full support VM relocation through a re-instantiation. BtrPlace can
  now infer if live-migration is preferable over re-instantiation.
- Refactor completely the "satisfaction" process. Now everything takes place
  inside a SatConstraintChecker.
- Move most of the interface and abstract classes to the package of their
  implementations

version 0.28 - 29 Mar 2013
----------------------
- migration to JDK 7
- much better performance for both the placement and the scheduling part
- bug fixes
- finer statistics
- When resources are manipulated, it is no longer a necessary to state a
Overbook constraint. An overbooking ratio of 1 is assumed

version 0.27 - 22 Mar 2013
----------------------
- Bug fix
- Refactor the NodeActionModel implementation. New variables to denote the
  moment the servers are powered up and down
- API documentation
- Interface to specify application protocols for a ReconfigurationPlan.
- close #2, #3, #4

version 0.26 - 20 Mar 2013
----------------------
- a 'examples' module to provide the source code of some
online tutorials
- Improve Preserve implementation for better performance on the scheduling part
- several random bug fix
- close #2
- A zipped version of the Javadoc is generated and deployed
- improve the node state management.

version 0.25 - 11 Mar 2013
----------------------
- New release to fit with the website arrival
- ChocoReconfigurationAlgorithm#setVerbosityLevel to indicate the level of
verbosity

version 0.24 - 08 Mar 2013
----------------------
- JSON conversion for plans
- improve performance
- re-organize the code to provide a module dedicated to test helpers

version 0.23 - 28 Feb 2013
----------------------
- code refactoring for the JSON parser

version 0.20 - 25 Feb 2013
----------------------
- merge repositories for solver-api, solver-choco and solver-json
  into a single repository

versions prior to 0.20
----------------------

The following logs were dedicated to each of the previous
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
- ModelView to customize a model with new domain-specific information.
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

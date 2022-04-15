Release notes
======================

version 1.12.3 - soon come
----------------------
See milestone [1.12.3](https://github.com/btrplace/scheduler/milestones/1.12.3)

This version is mostly about one bug fix(#382), some minor performance
improvements and dependencies update.


version 1.12.2 - 17 Jan 2022
----------------------
See milestone [1.12.2](https://github.com/btrplace/scheduler/milestones/1.12.2)

Minor release to update the Disjoint constraint that was not compliant with
Choco API.


version 1.12.1 - 10 Jan 2022
----------------------
See milestone [1.12.1](https://github.com/btrplace/scheduler/milestones/1.12.1)

This releases is mostly about bug fix and performance improvement.
- bug fixes and API updates in IntMap and IntObjectMap
- performance improvement thanks to the move to choco 4.10.8
- performance improvement in the packing and knapsack


version 1.12.0 - 11 Oct 2021
----------------------
See milestone [1.12.0](https://github.com/btrplace/scheduler/milestones/1.12.0)

- This release is mostly about performance improvement (CPU and memory).
At scale, the initialisation phase of a model is faster.
- The minor version is updated due to the drop of the continuous version of
RunningCapacity (see #314)
- The packing constraint is a bit smarter.


version 1.11.2 - 09 Dec 2020
----------------------
See milestone [1.11.2](https://github.com/btrplace/scheduler/milestones/1.11.2)

- Ticket #266 should lead to a better balancing when ResourceCapacity
  constraints restrict the node capacities.
  

version 1.11.1 - 03 Sep 2020
----------------------
See milestone [1.11.1](https://github.com/btrplace/scheduler/milestones/1.11.1)

- Dependencies update
- Fix an overfiltering in the task scheduler (#241)


version 1.11.0 - 10 Jun 2020
----------------------
See milestone [1.11.0](https://github.com/btrplace/scheduler/milestones/1.11.0)

- users can now provide custom JSON converters for actions and events.
- dependencies update and code quality improvement.


version 1.10.2 - 17 Dec 2019
----------------------
See milestone [1.10.2](https://github.com/btrplace/scheduler/milestones/1.10.2)

Minor update that refresh the dependencies and fix a mis-leading Action.toString().


version 1.10.1 - 23 Nov 2019
----------------------
See milestone [1.10.1](https://github.com/btrplace/scheduler/milestones/1.10.1)

This milestone fixes a few bugs and performance issues on micro benchmarks.
It also bump the version of many dependencies.

Finally, it improves slightly the code and the documentation quality. Long term,
the plan is to prepare for a version 2.0 that will be JPMS compliant and build for
jdk8 and jdk11 until jdk8 End-of-Life.


version 1.10.0 - 08 Nov 2018
----------------------
See milestone [1.10.0](https://github.com/btrplace/scheduler/milestones/1.10.0)

- Close #174
- Fix #173 to be able to stop the solver while processing.


version 1.9.3 - 29 Aug 2018
----------------------
See milestone [1.9.3](https://github.com/btrplace/scheduler/milestones/1.9.3)

- Close #171


version 1.9.2 - 28 Mar 2018
----------------------
See milestone [1.9.2](https://github.com/btrplace/scheduler/milestones/1.9.2)

- Fix #170 that remove an inappropriate tuning.


version 1.9.1 - 17 Nov 2017
----------------------
See milestone [1.9.1](https://github.com/btrplace/scheduler/milestones/1.9.1)

- Fix #167, the performance regression due to new Choco settings


version 1.9.0 - 14 Nov 2017
----------------------
See milestone [1.9.0](https://github.com/btrplace/scheduler/milestones/1.9.0)

- close issue #156, #158, #159, #161, #162, #164, #166
- move to Choco 4.0.5
- Better integration of SafePlace and revise some of its API
- remove a useless dependency (fastutil) to reduce jar size (by 16MB !)


version 1.8.0 - 15 May 2017
----------------------
See milestone [1.8.0](https://github.com/btrplace/scheduler/milestones/1.8.0)

- Improve the filtering using a knapsack propagator (see Issue #154)


version 1.7.0 - 04 May 2017
----------------------
See milestone [1.7.0](https://github.com/btrplace/scheduler/milestones/1.7.0)

- Hook system to catch the computed solutions on the fly (see #147)
- CShareableResource was over pessimistic when identifying misplaced VMs. (see #150)
- Move to choco 4.0.4


version 1.6.1 - 10 Mar 2017
----------------------
See milestone [1.6.1](https://github.com/btrplace/scheduler/milestones/1.6.1)

- More efficient event injection (see Issue #145)


version 1.6.0 - 08 Feb 2017
----------------------
See milestone [1.6.0](https://github.com/btrplace/scheduler/milestones/1.6.0)

Notable news:
- An exception hierarchy to refine SchedulerException (see #130).


version 1.5.1 - 16 Jan 2017
----------------------
See milestone [1.5.1](https://github.com/btrplace/scheduler/milestones/1.5.1)

- fix a performance issue in MinMigrations (a partial #138)


version 1.5.0 - 16 Jan 2017
----------------------
See milestone [1.5](https://github.com/btrplace/scheduler/milestones/1.5)

This version is a maintenance release that fix the critical issue #137.
We also move to a 3-number release number to isolate minor changes.


version 1.4 - 13 Jan 2017
----------------------
See milestone [1.4](https://github.com/btrplace/scheduler/milestones/1.4)

This version moved from Choco-3.x to Choco-4.x. It removed custom hacks
and some extensions that are now mainstream. We are mostly back to
the performance of the 1.2 version of BtrPlace.

The CSP model has been tuned to shrink it in terms of variables. There
is now much more constants. This reduces the memory usage of the
scheduler for large problems. More improvements to come in the next version

- Issue #136: MinMigrations objective to minimize the cumulative duration of
  the migrations
- WorstFit: a new heuristic to place against a worst-fit approach


version 1.3 - 13 Dec 2016
-----------------------
See milestone [1.3](https://github.com/btrplace/scheduler/milestones/1.3)

- Issue #131. The solver is now more robust with heavily constrained
  placement variables.
- Fix minor issue #132


version 1.2 - 02 Nov 2016
----------------------
See milestone [1.2](https://github.com/btrplace/scheduler/milestones/1.2)

- minor bug fixes
- improve a bit some error messages
- the scheduler returns ```null``` when an unsupported state transition
is expected. Previously, it thrown a SchedulerException


version 1.1 - 17 Aug 2016
----------------------
See milestone [1.1](https://github.com/btrplace/scheduler/milestones/1.1)

Another minor release.
- solve a modeling issue in the continuous version of spread
- parameter validation in ShareableResource
- fix a false negative in ShareableResource


version 1.0 - 06 Jul 2016
----------------------
See milestone [1.0](https://github.com/btrplace/scheduler/milestones/1.0)

This version is a minor release. It does not break the user API.
It is flagged as a '1.0' version simply because there was no real reason not
to stay with 0.x versions. It is featured enough since a long time and the
last releases improved the code quality significantly.

- Fix a severe performance regression when parsing JSON instances (#118)


version 0.46 - 02 Jun 2016
----------------------
See milestone [0.46](https://github.com/btrplace/scheduler/milestones/0.46)

This version improves the performance and the codebase quality.
In terms of performance, the solver is faster at generating problems and
solving them at large scale (see https://goo.gl/k5E0pf). The CSP model is
modeled using less constraints and variables. The solver has been tuned
by cooperating with the Choco dev team. 

- API change:
    - ReconfigurationPlanCheckerException becomes SatConstraintViolationException
    - The code to split problems and solve them in parallel moved
       to its own module named 'split'
    - The JSON package has been refactored for simplicity. There is now
       a single entry point for JSON (de-)serialisation in the JSON class
- the bench module has been rewritten to ease instance benching.
  The URL https://goo.gl/k5E0pf will try to track performance gain.
- a new memory environment usable on demand for large scale instances (#116)


version 0.45 - 22 Apr 2016
----------------------
See milestone [0.45](https://github.com/btrplace/scheduler/milestones/0.45)

A released focused on performance and code quality improvement using
Coverity, Findbugs, Sonar & co.

- the serialisation format forces UTF-8 encoding
- less arrays, more immutable lists in the ReconfigurationProblem
- no more Cloneable. Moved to an ad-hoc Copyable interface
- the migration scheduler is faster and more robust


version 0.44 - 17 Jan 2016
----------------------
See milestone [0.44](https://github.com/btrplace/scheduler/milestones/0.44)

A maintenance release but on a critical heisenbug bug that prevented migrations
to be added to plans.

- bug fixes: #91


version 0.43 - 12 Jan 2016
----------------------
See milestone [0.43](https://github.com/btrplace/scheduler/milestones/0.43)

This release proposes significant performance improvement in both placement
oriented problems and migration oriented problems. It also simplifies the
code that map choco objects to their api-side equivalent.

- Improved scalability of the Network model
- Improved the scalability of the placement model (#67)
- rewrite the constraint and the view mapping process (#79,#80,#81).
  No more builders, only dynamic invokation.
- ChocoViews can now provided an estimated of misplaced VMs (#77)
- bug fixes: #87, #88, #86
- remove the standalone examples runner. Now Examples have to be launched as
  unit tests from an IDE
- the apidocs of the master branch is always available online at
  http://www.btrplace.org/apidocs-next/


version 0.42 - 23 Nov 2015
----------------------
- The API allows know to indicate a bandwidth to allocate for VM migration (#52)
- @vincent-k released his migration model to estimate precisely the VM migration duration depending
  on its workload and the network topology. It now infers efficient and realistic reconfiguration plans where the
  parallelism is no longer naive. See the associated [tutorial](https://github.com/btrplace/scheduler/wiki/Network-and-migrations-scheduling)
- Upgrade to Choco 3.2.2
- Improve the performance of the slice scheduler when nodes must be turned off
- Fix issues #70, #69, #68, #66, #65, #64, #54, #53, #72, #73


version 0.41 - 12 Feb 2015
----------------------
- Speed up tasks scheduling. This improves the resolution of problems that manipulate the node states. See #62
- Refer to the associated [issue tracker](https://github.com/btrplace/scheduler/issues?q=milestone%3A%22release+0.41%22+is%3Aclosed) to get the closed issues


version 0.40 - 16 Jan 2015
----------------------
- update to choco 3.3.0
- fix #53
- Refer to the associated [issue tracker](https://github.com/btrplace/scheduler/issues?q=milestone%3A%22release+0.40%22+is%3Aclosed) to get the closed issues


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


version 0.37 - 14 Apr 2014
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


version 0.36 - 28 Mar 2014
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

























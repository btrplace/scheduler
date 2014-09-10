- fuzzer for the inputs OK
- fuzzer for the delay OK
- check for the assertions pb in continuous OK

0- Limits avec parallels OK

1- Un fuzzer continuous pour les core constraints OK
 -> open bar, no restrictions sur les transitions (par table de proba) et l'ordo (ofc)


2- fuzzer core-compliant OK
  -> filter sur les core constraints (noVMsOnOfflineNode && hostForVMs):   
  => genère plan & src/dst models


-> Run, results for placement only constraints OK
 1/ Core: noVMsOnOfflineNodes OK, toRunning OK, toReady OK, toSleeping OK
 2/ States running OK, sleeping OK, ready OK, online OK, offline OK, killed OK
 3/ VM2VM: spread OK, gather OK, among OK, split OK, lonely OK, splitAmong ???
 4/ VM2PM: ban OK, fence OK, root OK, quarantine OK
 5/ Counting: runningCapacity OK, maxOnline OK

 6/ rc: preserve ???, resourceCapacity ???, overbook ???
 7/ seq ?

-> remove duplicates OK
-> reduce failed test cases to a minimum to reduce duplicates OK
-> identify and report false positives / false negatives OK
-> remove symmetries ? KO
-> visu OK
-> filter out instances that have failures in the core constraints OK


-> simplify types. Externalize. Fuzzing and custom types (int, float, ...)

EVAL fussing/reduction:
-> the effect of reduction on the detection of duplicates


-> CDF to test the impact of instance size over the failure detection
-> CDF . . . timeout
   -> useless without categorization potentially (or duplicates)


  -> group by failures ?

-> tester limits

-> test with checkers

COMPARE each:
  spec, rebuild, repair
    -> identify over-agressive optimisation
    

HOW TO VALIDATE THE QUALITY OF THE FUZZING
		-> SPACE EXPLORATION
HOW TO CLASSIFY TESTS

HOW LONG TO TEST ?
  -> last time since discovering of a new unique testcase

  -> cdf, rate of addition (show good coverage or stuck ?)

---
  Distribution
---

As a maven plugin to act on sources directly
-> annotations over the source code

@BtrpConstraint(kind="core",
                inv="!(n : nodes) nodeState(n) /= online --> card(hosted(n)) = 0")

core=false
with=
inv=
discrete=
continuous=


+ annotations inside API

module:
+ btrplace:cspec
+ btrplace:cspec-dump
+ btrplace:cspec-verif
+ btrplace:cspec-verif-plugin



cspec:test [-Dtest=XX] -Dgroup=x,y,z
cspec:dump





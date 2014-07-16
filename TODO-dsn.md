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


-> try to reduce the number of failures
  -> remove duplicates OK
  -> remove symmetries ?




-> CDF to test the impact of instance size over the failure detection
-> CDF . . . timeout
   -> useless without categorization potentially (or duplicates)

-> filter instances that have failures in the core constraints

/!\ Violations are absorbing and hide false positives
  -> in which conditions we can test multiple constraints (special case for core constraints)

-> group by failures ?

-> tester limits


HOW TO VALIDATE THE QUALITY OF THE FUZZING
		-> SPACE EXPLORATION
HOW TO CLASSIFY TESTS

HOW LONG TO TEST ?
  -> last time since discovering of a new unique testcase

  -> cdf, rate of addition (show good coverage or stuck ?)
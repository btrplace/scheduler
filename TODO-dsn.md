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

-> try to reduce the number of failures

-> CDF to test the impact of instance size over the failure detection
-> CDF . . . timeout
   -> useless without categorization potentially (or duplicates)

-> filter instances that have failures in the core constraints

/!\ Violations are absorbing and hide false positives
  -> in which conditions we can test multiple constraints (special case for core constraints)

-> group by failures ?

-> tester limits

-> group de benchs
1/ Core
2/ VM & node State
    running
    sleeping
    ready
    online
    offline
    killed

3/ VM-VM affinity
   spread
   gather
   among
   split
   splitAmong

4/ VM-node affinity
  ban
  fence
  root
  quarantine

5/ Counting
	runningCapacity

6/ Resource-oriented constraints
	preserve
	resourceCapacity
	overbook

HOW TO VALIDATE THE QUALITY OF THE FUZZING
		-> SPACE EXPLORATION
HOW TO CLASSIFY TESTS
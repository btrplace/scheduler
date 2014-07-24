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

-> identify and report false positives / false negatives OK

-> visu OK

-> reduce failed test cases to a minimum to reduce duplicates

-> simplify types. Externalize. Fuzzing and custom types (int, float, ...)

EVAL fussing/reduction:
-> the effect of reduction on the detection of duplicates
-> the effect of times on the detection of unique tests


-> try to reduce the number of failures




  -> remove symmetries ?


-> CDF to test the impact of instance size over the failure detection
-> CDF . . . timeout
   -> useless without categorization potentially (or duplicates)

-> filter instances that have failures in the core constraints

/!\ Violations are absorbing and hide false positives
  -> in which conditions we can test multiple constraints (special case for core constraints)

-> group by failures ?

-> tester limits

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

//Split
@BtrpConstraint(kind="side", args="part: packing(vms)", inv="{ {host(v). v : p , vmState(v) = running}. p : part} : packings(nodes)")


foo:fuzz toto -o foo.json
foo:fuzz toto -i foo.json

bcspec:test

test stuff
-> test every impl,
    - stop after X sec || 10k tests
    - nbWorkers = Math.max(3, #cores)
    - first failure


//Bind a impl and a serializer to an api



@CTestsProvider(name=foo, constraint="foo")
new Fuzzer()
.vmStateTransition()
.nodeStateTransition()
.dom(VerifDomain)
.durations(min,max)
.constraint(foo) //hidden

//checker
@Verify(input=foo.json || fuzzer = method)
void foo(Verifier v)
return v.stopAfter(X)
.maxTests(Y)
.maxUniqueTests(Y)
.maxFailure(Z)
.continuous()
.discrete()
.repair
.rebuild


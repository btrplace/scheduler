# Code generation #

Once a formula is decomposed into a conjunction of terms, it is time to generate the unit tests.

## Unit tests ##

### Discrete restriction ###

Here, the objective is to generate test for a verification of discrete restriction. So, we just have to generate an instance and serialize it into JSON.


## Generative terms ##

### Core terms ###

These generative terms are related to the Core-RP. It basically states everyting that is needed to generate a Model.

#### Vocabular ####

* `online N1`: declare `N1` as an online node

```
public class OnlineDecorator implements InstanceDecorator {

	private String id;
	
	boolean decorate(Instance i) {
		Node n = i.getModel().newNode();
		return i.getModel().getMapping().addOnlineNode(n);
	}
	
	public String getTerm() {
		return "online(" + id + ")";
	}
	
	public List getDependencies() {
		[];
	}
}
```

* `offline N1`: declare `N1` as an offline node
* `ready VM1`: declare `VM1` as a ready VM
* `running VM1 N1`: declare `VM1` as a running VM on online node `N1`.
* `sleeping VM1 N1 `: declare `VM1` as a sleeping VM on online node `N1`
* `terminated VM1`: declare `VM1` as a VM that should be terminated

```
public class PlaceDecorator implements InstanceDecorator {

	private String nId, vmId;
	
	boolean decorate(Instance i) {
		Node n = i.getModel().newNode();
		return i.getModel().getMapping().addOnlineNode(n);
	}
	
	public String getTerm() {
		return "online(" + id + ")";
	}
	
	public List getDependencies() {
		"online(N1)","";
	}
}
```

### Implementation notes ###

- A streaming model. Generate TestUnit on the fly to reduce memory pressure ... and for the fun
- Finish the implementation of the language
- Tests FFS !
- Error reporting at the language level
- Remove native constraint stuff. The marshaling allow to reproduce what you want
- JSON marshalling by default. So @foo should generate json data

## Unit tests generation protocol ##

- Generate every possible models
- Get the good (`GP`) and the no-good proposition (`NGP`)
- foreach possible constraint signature
    - for each model
        - add the constraint to the model
		- evaluate if it is a `GP` or a `NGP`
		- serialize to unit-testable entity
		- profit


### Composition of a test suite ###

```
{
"constraint": {
	"id" : "foo"
	"parameters" : {
		"n" : "node",
		"v" : "vm"
	},
	"proposition": "..."
},
"scenarios": [
	{
	"model": ...,
	"test": [
		{"values": {"n" : 0,"v" : 1},
		 "consistent" : true
		}		
	]	
	}
]
}
```

## Verification process ##

* Detect for the checker and the implementation
	* False positive: accept an unconsistent placement
	* False negative: deny a consistent placement
* For the implementation checker
	* Need to check every possible solution ?
* Merge & reduce the failure to identify minimal explicit scenario ?
* Beware of signature simplification such as for ban && fence	

## Related work ##

* Fuzzing techniques
* Test reduction techniques

## Reporting ##

* How to merge and reduce failures
	* Signatures that lead to violations 
	* models and plan
	* What action leads to the problem ?
	* Can we infer the solution ?
	
* Create a hierarchy over the test units. 
	* Typically, the placement of a VM refines its state which refines its presence.
	* It is also possible to make a hierarchy over the tested constraint ?
	* Should provide a bad-ass graph. With an identifier for each node, it is possible to play a specific set of test units and merge the results.
	
## how much to generate ##

* n: number of nodes
* m: number of VMs
* number of models with only nodes: 2^n (number of states ** nb of nodes)
* number of combination _per_ VM: (2*q + 1)
   * q: number of online nodes
   * 2*q + 1 -> a VM can be either running or sleeping on a node plus ready
* TODO: With $n$ nodes, we must establish the number of models havging 1, 2, ..., n online nodes

* Number of models with only nodes of _n_ nodes having _q_ nodes online: C_n^q.

* Number of models having VMs with 3x3:
	* 0 node online: (2*0 + 1)^3 * C^0_3= 1
	* 1 node online: (2*1 + 1)^3 * C^1_3
	
* Number of model with $n$ and $m$ VMs
fa  

## Generate all the possible resulting plans

* If we considers only the nodes
  * Each node may have its state changed or not: $2^n$ plans
  * For each VM:
     * if running: $n$ placement if stay running + 1 (ready) + 1 (sleeping): $n + 2$
     * if sleeping: 1 (stay sleeping) + 1 (running) =$2$
     * if ready: $n$ (if goes running) + 1 (stay ready) = $n + 1$
	 
* For a given n x m model. How many transitions:
	* \sum_{i = 0}^{i \leq n} C_n^i \times (2i + 2)^m     
	
* Finally, the possible schedules:
   * Each action makes btw 1 and 3 sec to complete
     * $3^s$ number of plans, no delay but all the possible durations
     * for each resulting plan
         * One resulting plan for each possible delay, varying from 0 to horizon - duration
         
      
     		
      
# What to specify

* Constraints
* state/transition model	

```
transitions VM {
	relocate: running -> running
	suspend: running -> sleeping
	boot: ready -> running
	resume: sleeping -> running
	shutdown: running -> ready
}
```

#Genralize a test case

This fail

nodeState(node#0) /= offline | card(hoster(node#0)) = 0

Test 0 result:
-------------
consistent: false, result: falsePositive
constraint: offline(nodes=[node#1], discrete)
error: java.lang.Exception: Should not pass
origin:
node#1: (vm#2)
node#0: vm#0
(node#2)
READY vm#1

plan:
0:3 {action=shutdown(node=node#1)}


Looking for similar causes leading to the same bug.
- get the highest-level description that lead to this bug
  
	- what hangs ?
	
	- hangs when the VM is running ?
	
	- hangs when there is no VM ?
	
	- hangs when there is more VM ?
	
	- VM running, relocated elsewhere ?
		
		- before, during, after ?
	
- second, to guide more efficiently to the root cause
			
- first to generate bugs and test the fix

- third to report _other bugs_

- How to reduce the initial model ?

-> generative formula
on(n0) & on(n1) & off(n2) & run(v0) & ready(v1) & sleeping(n2) & place(v0, n0) & place(v2, n1) & shutdown(n1, 0, 3)

-> bug generative formula
!(n : node). #(v : VM) ^online(n) & sleeping(v) & place(v, n) & offline(n)


	 
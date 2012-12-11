Roadmap to v0.1
----------------

- OK (cause it is not needed) Entailment management in BinPacking
- OK? Slice scheduling
- OK Allocate actions and event
- By default, no transition actions
- By default maintain resource usage
- Implement and check every ActionModel
- Objective && search Heuristic
   -> the default Objective will be the current entropy one (MinMTTR)
- Partitioning
- Repair mode with undefined satisfaction status

Later
-----------------
- The forge as a bounded resource to be consistent
 -> constraint maxForgeCapacity to restrict the maximum number of VMs instantiated simultaneously
- VM reinstantiation
    -> derived from Options

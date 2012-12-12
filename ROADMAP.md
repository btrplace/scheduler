Roadmap to v0.1
----------------

- OK (cause it is not needed) Entailment management in BinPacking
- OK? Slice scheduling
- OK Allocate actions and event
- OK By default, no transition actions
- OK By default maintain resource usage
- Implement and check every VM ActionModel
- Implement and check every node ActionModel

- A default overbooking factor or not ?
  -> why not if it is clean, but no idea for the moment

- Objective && search Heuristic
   -> the default Objective will be the current entropy one (MinMTTR)
- Partitioning
- Repair mode with undefined satisfaction status
- Clarify discrete/continuous restriction

Later
-----------------
- The forge as a bounded resource to be consistent
 -> constraint maxForgeCapacity to restrict the maximum number of VMs instantiated simultaneously
- VM reinstantiation
    -> derived from Options

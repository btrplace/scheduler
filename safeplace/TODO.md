## Parser
OK function operator for *current value*
OK set in extension
OK set in comprehension
OK constraints as function
OK error reporting
OK simplify type system
OK validate every constraint
OK discrete & continuous constraints (an operator for current && end status ?): problem with marshalling
NO predicates

## Verifier ##
- impl: discrete | continuous | repair | full
- checker: discrete | continuous
- spec: discrete | continuous

## Reducer ##
- Return which action violate which constraint
- Remove any output inside btrplace
OK Reduce plan (less actions)
OK Reduce signature ? (lighter constraints)
   -> O(n) implementation
OK Reduce source ? (less elements, constraints)
  -> remove elements not involved in the constraints signature.
     -> VMs in first
     -> nodes after

## Test case generator
- Quantify the quality of the fuzzer ?
- parallel execution
- Symbolic execution ?

# Bug finding
- checker
- local search
- generification

#Evaluation
- concision du language
- nb de bugs et taxonomie
- TCB avec Cobertura ?
- taille des checkers
- performance des spec checkers si online
- efficacité de la réduction
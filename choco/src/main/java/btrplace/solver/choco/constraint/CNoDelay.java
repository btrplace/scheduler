package btrplace.solver.choco.constraint;

import btrplace.model.Model;
import btrplace.model.VM;
import btrplace.model.constraint.Constraint;
import btrplace.model.constraint.NoDelay;
import btrplace.solver.choco.ReconfigurationProblem;
import btrplace.solver.choco.transition.VMTransition;
import solver.Solver;
import solver.constraints.IntConstraintFactory;
import solver.variables.IntVar;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by vkherbac on 01/09/14.
 */
public class CNoDelay implements ChocoConstraint {

    private NoDelay noDelay;

    /**
     * Make a new constraint
     *
     * @param nd the NoDelay constraint to rely on
     */
    public CNoDelay(NoDelay nd) { noDelay = nd; }

    @Override
    public Set<VM> getMisPlacedVMs(Model model) {

        return new HashSet<VM>(noDelay.getInvolvedVMs());
    }

    @Override
    public boolean inject(ReconfigurationProblem rp) {

        // Get the solver
        Solver s = rp.getSolver();

        // For each vm involved in the constraint
        for (VMTransition vt : rp.getVMActions()) {

            if (noDelay.getInvolvedVMs().contains(vt.getVM())) {

                // Get the VMTransition start time
                IntVar start = vt.getStart();

                /*
                //TODO: Something wrong with "vt.getDuration().getValue()" (not instanciated)
                // Special case of a 'possible' migration
                if (vt instanceof RelocatableVM) {

                    if (vt.getDuration().instantiated()) {
                        // Check if the Transition duration is > 0 (effective migration) and set a boolean accordingly
                        BoolVar b = (vt.getDuration().getValue() > 0 ? VariableFactory.one(s) : VariableFactory.zero(s));

                        // Add the constraint "(duration > 0) => start = 0" to the solver
                        s.post(new FastImpliesEq(b, start, 0));
                    }

                } else {
                */
                    // Add the constraint "start = 0" to the solver
                    s.post(IntConstraintFactory.arithm(start, "=", 0));
                //}
            }
        }

        return true;
    }

    /**
     * Builder associated to the constraint.
     */
    public static class Builder implements ChocoConstraintBuilder {
        @Override
        public Class<? extends Constraint> getKey() {
            return NoDelay.class;
        }

        @Override
        public CNoDelay build(Constraint c) {
            return new CNoDelay((NoDelay) c);
        }
    }
}

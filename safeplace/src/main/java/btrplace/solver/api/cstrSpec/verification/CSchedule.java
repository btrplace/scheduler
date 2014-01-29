package btrplace.solver.api.cstrSpec.verification;

import btrplace.model.Model;
import btrplace.model.VM;
import btrplace.model.constraint.Constraint;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ReconfigurationProblem;
import btrplace.solver.choco.actionModel.ActionModel;
import btrplace.solver.choco.constraint.ChocoConstraint;
import btrplace.solver.choco.constraint.ChocoConstraintBuilder;
import choco.kernel.solver.ContradictionException;

import java.util.Collections;
import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class CSchedule implements ChocoConstraint {

    private Schedule cstr;

    public CSchedule(Schedule s) {
        cstr = s;
    }

    @Override
    public boolean inject(ReconfigurationProblem rp) throws SolverException {
        ActionModel am;

        if (cstr.getVM() != null) {
            am = rp.getVMAction(cstr.getVM());
        } else {
            am = rp.getNodeAction(cstr.getNode());
        }
        if (am == null) {
            return false;
        }
        try {
            am.getStart().setVal(cstr.getStart());
            am.getEnd().setVal(cstr.getEnd());
        } catch (ContradictionException ex) {
            //ex.printStackTrace();
            rp.getLogger().error("Unable to force the schedule of " + am + " to " + cstr + ": " + ex.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public Set<VM> getMisPlacedVMs(Model m) {
        return Collections.emptySet();
    }

    public static class Builder implements ChocoConstraintBuilder {

        @Override
        public Class<? extends Constraint> getKey() {
            return Schedule.class;
        }

        @Override
        public CSchedule build(Constraint cstr) {
            return new CSchedule((Schedule) cstr);
        }
    }
}

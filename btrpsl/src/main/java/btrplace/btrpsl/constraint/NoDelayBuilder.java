package btrplace.btrpsl.constraint;

import btrplace.btrpsl.element.BtrpOperand;
import btrplace.btrpsl.tree.BtrPlaceTree;
import btrplace.model.VM;
import btrplace.model.constraint.NoDelay;
import btrplace.model.constraint.SatConstraint;

import java.util.Collections;
import java.util.List;

/**
 * Created by vkherbac on 05/09/14.
 */
public class NoDelayBuilder extends DefaultSatConstraintBuilder {

    /**
     * Make a new builder.
     */
    public NoDelayBuilder() {
        super("noDelay", new ConstraintParam[]{new ListOfParam("$vms", 1, BtrpOperand.Type.VM, false)});
    }

    /**
     * Build an online constraint.
     *
     * @param args must be 1 set of vms. The set must not be empty
     * @return a constraint
     */
    public List<SatConstraint> buildConstraint(BtrPlaceTree t, List<BtrpOperand> args) {
        if (checkConformance(t, args)) {
            List<VM> s = (List<VM>) params[0].transform(this, t, args.get(0));
            return (s != null ? (List) NoDelay.newNoDelay(s) : Collections.emptyList());
        }
        return Collections.emptyList();
    }
}

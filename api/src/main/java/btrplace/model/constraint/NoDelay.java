package btrplace.model.constraint;

import btrplace.model.Node;
import btrplace.model.VM;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 *
 * A constraint to force vms' actions to be executed
 * at the beginning (at time t=0), without any delay.
 *
 * Created by vkherbac on 01/09/14.
 */
public class NoDelay extends SatConstraint {

    /**
     * Instantiate constraints for a collection of VMs.
     *
     * @param vms   the VMs to integrate
     * @return the associated list of constraints
     */
    public static List<NoDelay> newNoDelay(Collection<VM> vms) {
        List<NoDelay> l = new ArrayList<>(vms.size());
        for (VM v : vms) {
            l.add(new NoDelay(v));
        }
        return l;
    }

    /**
     * Make a new constraint.
     *
     * @param vm    the vm to restrict
     */
    public NoDelay(VM vm) {

        super(Collections.singleton(vm), Collections.<Node>emptyList(), true);
    }

    @Override
    public boolean setContinuous(boolean b) {
        return b;
    }

    @Override
    public SatConstraintChecker getChecker() {
        return new NoDelayChecker(this);
    }

    @Override
    public String toString() {
        return "noDelay(" + "vm=" + getInvolvedVMs() + ", " + restrictionToString() + ")";
    }
}

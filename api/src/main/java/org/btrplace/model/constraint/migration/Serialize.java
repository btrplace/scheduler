package org.btrplace.model.constraint.migration;

import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.model.constraint.SatConstraint;
import org.btrplace.model.constraint.SatConstraintChecker;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * A constraint to ensure no overlapping between a set of migrations.
 *
 * @author Vincent Kherbache
 */
public class Serialize extends SatConstraint {

    /**
     * Make a new constraint.
     *
     * @param vms the involved VMs
     */
    public Serialize(Collection<VM> vms) {
        super(vms, Collections.<Node>emptyList(), true);
    }

    /**
     * Make a new constraint.
     *
     * @param vms the involved VMs
     */
    public Serialize(VM... vms) {
        super(Arrays.asList(vms), Collections.<Node>emptyList(), true);
    }

    @Override
    public boolean setContinuous(boolean b) {
        return b;
    }

    @Override
    public SatConstraintChecker getChecker() {
        return new SerializeChecker(this);
    }

    @Override
    public String toString() {
        return "serialize(" + "vms=" + getInvolvedVMs() + ", " + restrictionToString() + ")";
    }
}

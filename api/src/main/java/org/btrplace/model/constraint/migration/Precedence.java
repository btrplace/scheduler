package org.btrplace.model.constraint.migration;

import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.model.constraint.SatConstraint;
import org.btrplace.model.constraint.SatConstraintChecker;

import java.util.*;

/**
 * A constraint to force one or more VMs to migrate before one or more other VMs.
 * 
 * @author Vincent Kherbache
 */
public class Precedence extends SatConstraint {

    /**
     * Instantiate discrete constraints to force a set of VMs to migrate after a single one.
     *
     * @param   vmBefore the (single) VM to migrate before the others {@see vmsAfter}
     * @param   vmsAfter the VMs to migrate after the other one {@see vmBefore}
     * @return  the associated list of constraints
     */
    public static List<Precedence> newPrecedence(VM vmBefore, Collection<VM> vmsAfter) {
        return newPrecedence(Collections.singleton(vmBefore), vmsAfter);
    }

    /**
     * Instantiate discrete constraints to force a single VM to migrate after a set of VMs.
     *
     * @param   vmsBefore the VMs to migrate before the other one {@see vmAfter}
     * @param   vmAfter the (single) VM to migrate after the others {@see vmsBefore}
     * @return  the associated list of constraints
     */
    public static List<Precedence> newPrecedence(Collection<VM> vmsBefore, VM vmAfter) {
        return newPrecedence(vmsBefore, Collections.singleton(vmAfter));
    }

    /**
     * Instantiate discrete constraints to force a set of VMs to migrate after an other set of VMs.
     *
     * @param   vmsBefore the VMs to migrate before the others {@see vmsAfter}
     * @param   vmsAfter the VMs to migrate after the others {@see vmsBefore}
     * @return  the associated list of constraints
     */
    public static List<Precedence> newPrecedence(Collection<VM> vmsBefore, Collection<VM> vmsAfter) {
        List<Precedence> l = new ArrayList<>(vmsBefore.size() * vmsAfter.size());
        for (VM vmb : vmsBefore) {
            for (VM vma : vmsAfter) {
                l.add(new Precedence(vmb, vma));
            }
        }
        return l;
    }

    /**
     * Make a new precedence constraint.
     *
     * @param vmBefore  the vm to schedule before the other one
     * @param vmAfter   the vm to schedule after the other one
     */
    public Precedence(VM vmBefore, VM vmAfter) {
        super(Arrays.asList(vmBefore, vmAfter), Collections.<Node>emptyList(), true);
    }

    @Override
    public boolean setContinuous(boolean b) {
        return b;
    }

    @Override
    public SatConstraintChecker getChecker() {
        return new PrecedenceChecker(this);
    }

    @Override
    public String toString() {
        return "precedence(" + "vms=" + getInvolvedVMs() + ", " + restrictionToString() + ")";
    }
}

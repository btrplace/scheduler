package btrplace.model.constraint.checker;

import btrplace.model.DefaultModel;
import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.constraint.Split;
import btrplace.plan.event.RunningVMPlacement;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Checker for the {@link btrplace.model.constraint.Split} constraint
 *
 * @author Fabien Hermenier
 * @see btrplace.model.constraint.Split
 */
public class SplitChecker extends AllowAllConstraintChecker<Split> {

    /**
     * The group of VMs.
     */
    private List<Set<UUID>> vGroups;

    /**
     * The group of nodes associated to each of vGroup.
     */
    private List<Set<UUID>> pGroups;

    private Mapping curMapping;

    private Model mockModel;

    /**
     * Make a new checker.
     *
     * @param s the associated constraint
     */
    public SplitChecker(Split s) {
        super(s);
        vGroups = new ArrayList<>(s.getSets());
        for (Set<UUID> set : vGroups) {
            track(set);
        }
    }

    @Override
    public boolean endsWith(Model mo) {
        //Catch the booked nodes for each set
        curMapping = mo.getMapping().clone();
        mockModel = new DefaultModel(curMapping);
        return checkModel();
    }

    @Override
    public boolean startRunningVMPlacement(RunningVMPlacement a) {
        if (getConstraint().isContinuous() && getVMs().contains(a.getVM())) {
            a.apply(mockModel);
            return checkModel();
        }
        return true;
    }

    private boolean checkModel() {
        for (Set<UUID> vGroup : vGroups) {
            for (UUID vmId : vGroup) {
                if (curMapping.getRunningVMs().contains(vmId)) {
                    //Get the hosting server
                    //Check if only hosts VMs in its group
                    UUID nId = curMapping.getVMLocation(vmId);
                    for (UUID vm : curMapping.getRunningVMs(nId)) {
                        if (getVMs().contains(vm) && !vGroup.contains(vm)) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    @Override
    public boolean startsWith(Model mo) {
        if (getConstraint().isContinuous()) {
            return endsWith(mo);
        }
        return true;
    }
}

package btrplace.solver.choco.objective.minMTTR;

import btrplace.model.Mapping;
import btrplace.solver.choco.ReconfigurationProblem;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Tools to ease the management of the VM placement.
 *
 * @author Fabien Hermenier
 */
public final class VMPlacementUtils {

    private VMPlacementUtils() {
    }

    /**
     * Map a map where keys are the placement variable of the future-running VMs
     * and values are the VM identifier.
     *
     * @param rp the problem
     * @return the resulting map.
     */
    public static Map<IntDomainVar, UUID> makePlacementMap(ReconfigurationProblem rp) {
        Map<IntDomainVar, UUID> m = new HashMap<>();
        for (UUID vm : rp.getFutureRunningVMs()) {
            IntDomainVar v = rp.getVMAction(vm).getDSlice().getHoster();
            m.put(v, vm);
        }
        return m;
    }

    /**
     * Check if a VM can stay on its current node.
     *
     * @param rp the reconfiguration problem.
     * @param vm the VM
     * @return {@code true} iff the VM can stay
     */
    public static boolean canStay(ReconfigurationProblem rp, UUID vm) {
        Mapping m = rp.getSourceModel().getMapping();
        if (m.getRunningVMs().contains(vm)) {
            int curPos = rp.getNode(m.getVMLocation(vm));
            return rp.getVMAction(vm).getDSlice().getHoster().canBeInstantiatedTo(curPos);
        }
        return false;
    }
}

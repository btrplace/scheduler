package btrplace.solver.choco.objective.minMTTR;

import btrplace.model.Mapping;
import btrplace.solver.choco.ReconfigurationProblem;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Fabien Hermenier
 */
public final class VMPlacementUtils {

    private VMPlacementUtils() {
    }

    public static Map<IntDomainVar, UUID> makePlacementMap(ReconfigurationProblem rp) {
        Map<IntDomainVar, UUID> m = new HashMap<IntDomainVar, UUID>();
        for (UUID vm : rp.getFutureRunningVMs()) {
            IntDomainVar v = rp.getVMAction(vm).getDSlice().getHoster();
            m.put(v, vm);
        }
        return m;
    }

    public static boolean canStay(ReconfigurationProblem rp, UUID vm) {
        Mapping m = rp.getSourceModel().getMapping();
        if (m.getRunningVMs().contains(vm)) {
            int curPos = rp.getNode(m.getVMLocation(vm));
            return rp.getVMAction(vm).getDSlice().getHoster().canBeInstantiatedTo(curPos);
        }
        return false;
    }
}

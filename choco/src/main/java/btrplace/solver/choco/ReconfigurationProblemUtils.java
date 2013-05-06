package btrplace.solver.choco;

import btrplace.solver.SolverException;

import java.util.Collection;
import java.util.UUID;

/**
 * Utilities for {@link btrplace.solver.choco.ReconfigurationProblem}.
 *
 * @author Fabien Hermenier
 */
public final class ReconfigurationProblemUtils {

    /**
     * Utility class, no instantiation.
     */
    private ReconfigurationProblemUtils() {
    }

    /**
     * Check for the existence of VMs in a RP.
     *
     * @param rp  the RP to consult
     * @param vms the VMs to check
     * @throws SolverException if at least one of the given VMs is not in the RP.
     */
    public static void checkVMsExistence(ReconfigurationProblem rp, Collection<UUID> vms) throws SolverException {
        for (UUID vm : vms) {
            if (!rp.getFutureRunningVMs().contains(vm)
                    && !rp.getFutureSleepingVMs().contains(vm)
                    && !rp.getFutureReadyVMs().contains(vm)
                    && !rp.getFutureKilledVMs().contains(vm)) {
                throw new SolverException(rp.getSourceModel(), "Unknown VM '" + vm + "'");
            }
        }
    }

    /**
     * Check for the existence of nodes in a RP.
     *
     * @param rp the RP to consult
     * @param ns the nodes to check
     * @throws SolverException if at least one of the given nodes is not in the RP.
     */
    public static void checkNodesExistence(ReconfigurationProblem rp, Collection<UUID> ns) throws SolverException {
        for (UUID node : ns) {
            if (!rp.getSourceModel().getMapping().getAllNodes().contains(node)) {
                throw new SolverException(rp.getSourceModel(), "Unknown node '" + node + "'");
            }
        }
    }
}


package btrplace.solver.choco.actionModel;

import btrplace.solver.choco.VMActionModel;
import choco.kernel.solver.variables.integer.IntDomainVar;

/**
 * An interface to specify an VM action model related
 * to a VM that is already running and that will keep running.
 *
 * @author Fabien Hermenier
 */
public interface KeepRunningVMModel extends VMActionModel {


    /**
     * Indicates if the VMs is staying on its current hosting node.
     *
     * @return a variable instantiated to {@code 1} iff the VM is staying on its current node.
     *         Instantiated to {@code 0} otherwise
     */
    IntDomainVar isStaying();
}

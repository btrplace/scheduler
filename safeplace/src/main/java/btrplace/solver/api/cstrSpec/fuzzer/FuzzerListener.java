package btrplace.solver.api.cstrSpec.fuzzer;

import btrplace.plan.ReconfigurationPlan;

/**
 * @author Fabien Hermenier
 */
public interface FuzzerListener {

    void recv(ReconfigurationPlan p);
}

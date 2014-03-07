package btrplace.solver.api.cstrSpec.fuzzer;

import btrplace.plan.ReconfigurationPlan;

/**
 * @author Fabien Hermenier
 */
public interface ReconfigurationPlanFuzzerListener {

    void recv(ReconfigurationPlan p);
}

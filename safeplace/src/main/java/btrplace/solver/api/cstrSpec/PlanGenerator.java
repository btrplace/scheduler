package btrplace.solver.api.cstrSpec;

import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.api.cstrSpec.fuzzer.DelaysGenerator;
import btrplace.solver.api.cstrSpec.fuzzer.ReconfigurationPlanFuzzer;

import java.util.Iterator;

/**
 * @author Fabien Hermenier
 */
public class PlanGenerator implements Iterator<ReconfigurationPlan> {

    ReconfigurationPlanFuzzer f;

    private ReconfigurationPlan currentRootPlan;

    private DelaysGenerator curDelayGenerator;

    private int maxRootPlans, nbRootPlans, maxDelays, nbDelays;

    public PlanGenerator(int maxPlans, int maxDelays, ReconfigurationPlanFuzzer f) {
        this.f = f;
        currentRootPlan = f.newPlan();
        curDelayGenerator = new DelaysGenerator(currentRootPlan);

        this.maxRootPlans = maxPlans;
        this.maxDelays = maxDelays;
        this.nbRootPlans = 0;
        this.nbDelays = 0;
    }

    @Override
    public boolean hasNext() {
        return true;
    }

    @Override
    public ReconfigurationPlan next() {
        if (nbRootPlans == maxRootPlans && nbDelays == maxDelays) {
            return null;
        } else if (nbDelays == maxDelays) {
            nbRootPlans++;
            nbDelays = 0;
            currentRootPlan = f.newPlan();
            curDelayGenerator = new DelaysGenerator(currentRootPlan);
        }
        return curDelayGenerator.next();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}

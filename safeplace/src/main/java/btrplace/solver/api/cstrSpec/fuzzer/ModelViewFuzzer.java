package btrplace.solver.api.cstrSpec.fuzzer;

import btrplace.model.view.ModelView;
import btrplace.plan.ReconfigurationPlan;

/**
 * @author Fabien Hermenier
 */
public interface ModelViewFuzzer<E extends ModelView> {

    void decorate(ReconfigurationPlan p);
}

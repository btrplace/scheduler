package btrplace.solver.api.cstrSpec;

import btrplace.model.DefaultModel;
import btrplace.model.Instance;
import btrplace.model.Model;
import btrplace.model.constraint.MinMTTR;

/**
 * @author Fabien Hermenier
 */
public class InstanceGenerator {

    public InstanceGenerator() {

    }

    public Instance generate(And a) {
        Model mo = new DefaultModel();
        Instance i = new Instance(mo, new MinMTTR());
        for (Proposition p : a) {
            p.inject(i.getModel());
        }
        return i;
    }
}

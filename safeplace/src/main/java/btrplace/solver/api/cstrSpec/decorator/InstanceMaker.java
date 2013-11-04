package btrplace.solver.api.cstrSpec.decorator;

import btrplace.model.DefaultModel;
import btrplace.model.Instance;
import btrplace.model.Model;
import btrplace.model.constraint.MinMTTR;
import btrplace.solver.api.cstrSpec.decorator.core.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Fabien Hermenier
 */
public class InstanceMaker {

    private Map<String, InstanceDecorator> decorators;

    public InstanceMaker() {
        decorators = new HashMap<>();
    }

    public static InstanceMaker makeCoreInstancesMaker() {
        InstanceMaker m = new InstanceMaker();
        m.add(new OnlineDecorator());
        m.add(new OfflineDecorator());
        m.add(new ReadyDecorator());
        m.add(new RunningDecorator());
        m.add(new SleepingDecorator());
        return m;
    }
    public void add(InstanceDecorator d) {
        this.decorators.put(d.id(), d);
    }

    public Instance build(List<String> terms) {
        Model mo = new DefaultModel();
        Instance inst = new Instance(mo, new MinMTTR());
        Registry r = new Registry(mo);
        for (String term : terms) {
            String [] tokens = explode(term);
            String k = tokens[0];
            InstanceDecorator d = decorators.get(k);
            if (d == null) {
                throw new UnsupportedOperationException("No decorator available for '" + k + "'");
            }
            if (!d.decorate(inst, tokens, r)) {
                throw new UnsupportedOperationException("Unable to decorate the instance with '" + k + "'");
            }
        }
        return inst;
    }

    private String [] explode(String term) {
        return term.split(" ");
    }
}

package btrplace.solver.api.cstrSpec.decorator.core;

import btrplace.model.Instance;
import btrplace.model.Node;
import btrplace.solver.api.cstrSpec.decorator.InstanceDecorator;
import btrplace.solver.api.cstrSpec.decorator.Registry;

import java.util.Collections;
import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class OnlineDecorator implements InstanceDecorator {

    @Override
    public List<String> require() {
        return Collections.emptyList();
    }

    @Override
    public String id() {
        return "online";
    }

    @Override
    public List<String> provide() {
        return Collections.emptyList();
    }

    @Override
    public String toString() {
        return id();
    }

    @Override
    public boolean decorate(Instance i, String[] params, Registry reg) {
        Node n = reg.getNode(params[1]);
        return i.getModel().getMapping().addOnlineNode(n);
    }
}

package btrplace.solver.api.cstrSpec.decorator;

import btrplace.model.Instance;

import java.util.List;

/**
 * @author Fabien Hermenier
 */
public interface InstanceDecorator {

    boolean decorate(Instance i, String[] params, Registry reg);

    String id();

    List<String> provide();

    List<String> require();

}

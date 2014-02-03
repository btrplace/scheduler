package btrplace.solver.api.cstrSpec.spec.term;

import btrplace.model.Node;
import btrplace.solver.api.cstrSpec.spec.type.NodeType;
import btrplace.solver.api.cstrSpec.verification.specChecker.SpecModel;

import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class AllNodes extends Primitive {

    public AllNodes() {
        super("nodes", NodeType.getInstance());
    }

    @Override
    public Set<Node> eval(SpecModel m) {
        return m.nodes();
    }
}

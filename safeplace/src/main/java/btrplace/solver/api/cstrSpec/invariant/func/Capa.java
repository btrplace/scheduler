package btrplace.solver.api.cstrSpec.invariant.func;

import btrplace.model.Model;
import btrplace.model.Node;
import btrplace.model.view.ShareableResource;
import btrplace.solver.api.cstrSpec.invariant.Term;
import btrplace.solver.api.cstrSpec.invariant.type.NatType;
import btrplace.solver.api.cstrSpec.invariant.type.Type;

import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class Capa extends Function {

    private Term node;

    private Term id;

    public Capa(List<Term> stack) {
        this.node = stack.get(0);
        this.id = stack.get(1);
    }

    @Override
    public String toString() {
        return (currentValue() ? "$" : "") + "capa(" + node + "," + id + ")";
    }

    @Override
    public Integer eval(Model mo) {
        String rc = (String) id.eval(mo);
        ShareableResource r = (ShareableResource) mo.getView(ShareableResource.VIEW_ID_BASE + rc);
        if (r == null) {
            throw new RuntimeException("View '" + ShareableResource.VIEW_ID_BASE + rc + "' is missing");
        }
        return r.getCapacity((Node) node.eval(mo));
    }

    @Override
    public Type type() {
        return NatType.getInstance();
    }
}

package btrplace.solver.api.cstrSpec.invariant.func;

import btrplace.model.Model;
import btrplace.model.Node;
import btrplace.model.view.ShareableResource;
import btrplace.solver.api.cstrSpec.invariant.Term;
import btrplace.solver.api.cstrSpec.invariant.type.IntType;
import btrplace.solver.api.cstrSpec.invariant.type.NodeType;
import btrplace.solver.api.cstrSpec.invariant.type.StringType;
import btrplace.solver.api.cstrSpec.invariant.type.Type;

import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class Capa extends Function {

    private Term node;

    private Term id;

    public static final String ID = "capa";

    public Capa(Term node, Term id) {
        this.node = node;
        this.id = id;
    }

    @Override
    public String toString() {
        return (currentValue() ? "$" : "") + ID + "(" + node + "," + id + ")";
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
        return IntType.getInstance();
    }

    public static class Builder extends FunctionBuilder {
        @Override
        public Capa build(List<Term> args) {
            return new Capa(asNode(args.get(0)), asString(args.get(1)));
        }

        @Override
        public String id() {
            return Capa.ID;
        }

        @Override
        public Type[] signature() {
            return new Type[]{NodeType.getInstance(), StringType.getInstance()};
        }
    }
}

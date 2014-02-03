package btrplace.solver.api.cstrSpec.spec.term.func;

import btrplace.solver.api.cstrSpec.spec.type.IntType;
import btrplace.solver.api.cstrSpec.spec.type.NodeType;
import btrplace.solver.api.cstrSpec.spec.type.StringType;
import btrplace.solver.api.cstrSpec.spec.type.Type;
import btrplace.solver.api.cstrSpec.verification.specChecker.SpecModel;

import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class Capa extends Function<Integer> {

    @Override
    public String id() {
        return "capa";
    }

    @Override
    public Integer eval(SpecModel mo, List<Object> args) {
        throw new UnsupportedOperationException();
        /*String rc = args.get(1).toString();
        ShareableResource r = (ShareableResource) mo.getView(ShareableResource.VIEW_ID_BASE + rc);
        if (r == null) {
            throw new RuntimeException("View '" + ShareableResource.VIEW_ID_BASE + rc + "' is missing");
        }
        return r.getCapacity((Node) args.get(0)); */
    }

    @Override
    public Type[] signature() {
        return new Type[]{NodeType.getInstance(), StringType.getInstance()};
    }


    @Override
    public Type type() {
        return IntType.getInstance();
    }
}

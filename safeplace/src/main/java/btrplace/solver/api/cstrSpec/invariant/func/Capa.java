package btrplace.solver.api.cstrSpec.invariant.func;

import btrplace.model.Model;
import btrplace.model.Node;
import btrplace.model.view.ShareableResource;
import btrplace.solver.api.cstrSpec.invariant.type.IntType;
import btrplace.solver.api.cstrSpec.invariant.type.NodeType;
import btrplace.solver.api.cstrSpec.invariant.type.StringType;
import btrplace.solver.api.cstrSpec.invariant.type.Type;

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
    public Integer eval(Model mo, List<Object> args) {
        String rc = args.get(1).toString();
        ShareableResource r = (ShareableResource) mo.getView(ShareableResource.VIEW_ID_BASE + rc);
        if (r == null) {
            throw new RuntimeException("View '" + ShareableResource.VIEW_ID_BASE + rc + "' is missing");
        }
        return r.getCapacity((Node) args.get(0));
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

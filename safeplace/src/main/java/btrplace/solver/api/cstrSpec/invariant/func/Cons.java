package btrplace.solver.api.cstrSpec.invariant.func;

import btrplace.model.Model;
import btrplace.model.VM;
import btrplace.model.view.ShareableResource;
import btrplace.solver.api.cstrSpec.invariant.type.IntType;
import btrplace.solver.api.cstrSpec.invariant.type.StringType;
import btrplace.solver.api.cstrSpec.invariant.type.Type;
import btrplace.solver.api.cstrSpec.invariant.type.VMType;

import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class Cons extends Function<Integer> {

    @Override
    public Integer eval(Model mo, List<Object> args) {
        String rc = (String) args.get(1);
        ShareableResource r = (ShareableResource) mo.getView(ShareableResource.VIEW_ID_BASE + rc);
        if (r == null) {
            throw new RuntimeException("View '" + ShareableResource.VIEW_ID_BASE + rc + "' is missing");
        }
        return r.getConsumption((VM) args.get(0));
    }

    @Override
    public Type type() {
        return IntType.getInstance();
    }

    @Override
    public String id() {
        return "cons";
    }

    @Override
    public Type[] signature() {
        return new Type[]{VMType.getInstance(), StringType.getInstance()};
    }
}

package btrplace.solver.api.cstrSpec.invariant.func;

import btrplace.model.Model;
import btrplace.model.VM;
import btrplace.model.view.ShareableResource;
import btrplace.solver.api.cstrSpec.invariant.Term;
import btrplace.solver.api.cstrSpec.invariant.type.IntType;
import btrplace.solver.api.cstrSpec.invariant.type.Type;

import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class Cons extends Function {

    private Term vm;

    private Term id;

    public Cons(List<Term> stack) {
        this.vm = stack.get(0);
        this.id = stack.get(1);
    }

    @Override
    public String toString() {
        return (currentValue() ? "$" : "") + "cons(" + vm + "," + id + ")";
    }

    @Override
    public Integer eval(Model mo) {
        String rc = (String) id.eval(mo);
        ShareableResource r = (ShareableResource) mo.getView(ShareableResource.VIEW_ID_BASE + rc);
        if (r == null) {
            throw new RuntimeException("View '" + ShareableResource.VIEW_ID_BASE + rc + "' is missing");
        }
        return r.getConsumption((VM) vm.eval(mo));
    }

    @Override
    public Type type() {
        return IntType.getInstance();
    }
}

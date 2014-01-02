package btrplace.solver.api.cstrSpec.invariant.func;

import btrplace.model.Model;
import btrplace.model.VM;
import btrplace.model.view.ShareableResource;
import btrplace.solver.api.cstrSpec.invariant.Term;
import btrplace.solver.api.cstrSpec.invariant.type.IntType;
import btrplace.solver.api.cstrSpec.invariant.type.StringType;
import btrplace.solver.api.cstrSpec.invariant.type.Type;
import btrplace.solver.api.cstrSpec.invariant.type.VMType;

import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class Cons extends Function {

    private Term vm;

    private Term id;

    public static final String ID = "cons";

    public Cons(Term vm, Term rcId) {
        this.vm = vm;
        this.id = rcId;
    }

    @Override
    public String toString() {
        return (currentValue() ? "$" : "") + ID + "(" + vm + "," + id + ")";
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

    public static class Builder extends FunctionBuilder {
        @Override
        public Cons build(List<Term> args) {
            return new Cons(asVM(args.get(0)), asString(args.get(1)));
        }

        @Override
        public String id() {
            return Cons.ID;
        }

        @Override
        public Type[] signature() {
            return new Type[]{VMType.getInstance(), StringType.getInstance()};
        }
    }
}

package btrplace.solver.api.cstrSpec.spec.term;

import btrplace.solver.api.cstrSpec.spec.type.Type;
import btrplace.solver.api.cstrSpec.util.AllTuplesGenerator;
import btrplace.solver.api.cstrSpec.verification.spec.SpecModel;
import edu.emory.mathcs.backport.java.util.Arrays;

import java.util.*;

/**
 * @author Fabien Hermenier
 */
public class UserVar<T> extends Var<T> {

    private Term<Collection> backend;

    private boolean incl;

    private boolean not;

    public UserVar(String lbl, boolean incl, boolean not, Term backend) {
        super(lbl);
        this.incl = incl;
        this.backend = backend;
        this.not = not;
    }

    @Override
    public Type type() {
        return incl ? backend.type().inside() : backend.type();
    }

    @Override
    public String pretty() {
        return label() + (not ? " /" : " ") + (incl ? ": " : "<: ") + backend;
    }

    public Term<Collection> getBackend() {
        return backend;
    }

    @Override
    public T eval(SpecModel m) {
        return (T) m.getValue(label());
    }

    public List<Constant> domain(SpecModel mo) {
        Collection col = backend.eval(mo);
        if (incl) {
            List<Constant> s = new ArrayList<>();
            for (Object o : col) {
                s.add(new Constant(o, type()));
            }
            return s;
        } else {
            List<Object> s = new ArrayList<>();
            for (Object o : col) {
                s.add(o);
            }
            List<List<Object>> tuples = new ArrayList<>();
            for (Object o : s) {
                tuples.add(s);
            }
            AllTuplesGenerator<Object> tg = new AllTuplesGenerator<>(Object.class, tuples);
            Set<Constant> res = new HashSet<>();
            for (Object[] tuple : tg) {
                res.add(new Constant(new HashSet(Arrays.asList(tuple)), backend.type()));
            }
            return new ArrayList<>(res);
        }
    }
}

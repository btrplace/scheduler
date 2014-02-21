package btrplace.solver.api.cstrSpec.spec.term;

import btrplace.solver.api.cstrSpec.spec.type.Type;
import btrplace.solver.api.cstrSpec.util.AllTuplesGenerator;
import btrplace.solver.api.cstrSpec.verification.spec.SpecModel;
import edu.emory.mathcs.backport.java.util.Arrays;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class UserVar<T> extends Var<T> {

    private Term<Collection> backend;

    private boolean incl;

    private T val;

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
        return new StringBuilder(label()).append(not ? " /" : " ").append(incl ? ": " : "<: ").append(backend).toString();
    }

    public Term<Collection> getBackend() {
        return backend;
    }

    public boolean set(SpecModel mo, T o) {
        Collection s = backend.eval(mo);
        if (s == null) {
            throw new IllegalArgumentException("'" + o + "' is outside the domain of  " + backend + " (" + backend.eval(mo) + ")");
        }
        /*if (incl && !backend.eval(mo).contains(o)) {
            throw new IllegalArgumentException("'" + o + "' is outside the domain of  " + backend + " (" + backend.eval(mo) + ")");
        } else if (!incl && !backend.eval(mo).containsAll((Collection) o)) {
            throw new IllegalArgumentException("'" + o + "' is outside the domain of  " + backend + " (" + backend.eval(mo) + ")");
        } */
        val = o;
        return true;
    }

    public void unset() {
        val = null;
    }

    @Override
    public T eval(SpecModel m) {
        return val;
    }

    public List<Constant> domain(SpecModel mo) {
        if ((Term) backend instanceof Primitive) {
            if (incl) {
                List<Constant> s = new ArrayList<>();
                Collection col = backend.eval(mo);
                for (Object o : col) {
                    s.add(new Constant(o, type()));
                }
                return s;
            } else {
                List<Object> s = new ArrayList<>();
                Collection col = backend.eval(mo);
                for (Object o : col) {
                    s.add(o);
                }


                List<List<Object>> tuples = new ArrayList<>();
                for (Object o : s) {
                    tuples.add(s);
                }
                AllTuplesGenerator<Object> tg = new AllTuplesGenerator<>(Object.class, tuples);
                List<Constant> res = new ArrayList<>();
                for (Object[] tuple : tg) {
                    res.add(new Constant(new HashSet(Arrays.asList(tuple)), backend.type()));
                }
                return res;
            }

        }
        return null;
    }
}

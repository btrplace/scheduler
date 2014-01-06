package btrplace.solver.api.cstrSpec.spec.prop;

import btrplace.model.Model;
import btrplace.solver.api.cstrSpec.spec.term.Term;
import btrplace.solver.api.cstrSpec.spec.term.UserVar;
import btrplace.solver.api.cstrSpec.spec.term.Var;
import btrplace.solver.api.cstrSpec.util.AllTuplesGenerator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class Exists implements Proposition {

    private List<UserVar> vars;

    private Proposition prop;

    private Term from;

    public Exists(List<UserVar> iterator, Proposition p) {
        this.vars = iterator;
        prop = p;
        this.from = vars.get(0).getBackend();
    }

    @Override
    public Proposition not() {
        return new ForAll(vars, prop.not());
    }

    @Override
    public Boolean eval(Model m) {
        boolean ret = false;
        List<List<Object>> values = new ArrayList<>(vars.size());
        for (int i = 0; i < vars.size(); i++) {
            values.add(new ArrayList<>((Collection<Object>) from.eval(m)));
        }
        AllTuplesGenerator<Object> tg = new AllTuplesGenerator<>(Object.class, values);
        for (Object[] tuple : tg) {
            for (int i = 0; i < tuple.length; i++) {
                vars.get(i).set(tuple[i]);
            }
            Boolean r = prop.eval(m);
            if (r == null) {
                return null;
            }
            ret |= r;
        }
        for (Var v : vars) {
            v.unset();
        }
        return ret;
    }

    public void associate(Proposition p) {
        this.prop = p;
    }

    public String toString() {
        StringBuilder b = new StringBuilder("?(");
        Iterator<UserVar> ite = vars.iterator();
        while (ite.hasNext()) {
            Var v = ite.next();
            if (ite.hasNext()) {
                b.append(v.label());
                b.append(",");
            } else {
                b.append(v.pretty());
            }
        }
        return b.append(") ").append(prop).toString();
    }

    private String enumerate() {
        Iterator<UserVar> ite = vars.iterator();
        StringBuilder b = new StringBuilder(ite.next().label());
        while (ite.hasNext()) {
            b.append(",");
            b.append(ite.next().label());
        }
        return b.toString();
    }
}

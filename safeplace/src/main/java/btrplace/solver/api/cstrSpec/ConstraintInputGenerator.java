package btrplace.solver.api.cstrSpec;

import btrplace.model.Model;
import btrplace.solver.api.cstrSpec.generator.AllTuplesGenerator;
import btrplace.solver.api.cstrSpec.generator.Generator;
import btrplace.solver.api.cstrSpec.generator.RandomTuplesGenerator;
import btrplace.solver.api.cstrSpec.invariant.Var;

import java.util.*;

/**
 * @author Fabien Hermenier
 */
public class ConstraintInputGenerator implements Generator<Map<String, Object>> {


    private String[] ids;
    private Generator<Object[]> tg;

    public ConstraintInputGenerator(Constraint cstr, Model mo, boolean seq) {

        List<Var> params = cstr.getParameters();
        List<List<Object>> values = new ArrayList<>(params.size());
        ids = new String[params.size()];
        for (int i = 0; i < params.size(); i++) {
            Var v = params.get(i);
            Object o = v.eval(null);
            if (o != null) {
                values.add(Collections.singletonList(o));
            } else {
                values.add(new ArrayList<Object>(v.type().domain(mo)));
            }
            ids[i] = v.label();
        }

        if (seq) {
            tg = new AllTuplesGenerator<>(Object.class, values);
        } else {
            tg = new RandomTuplesGenerator<>(Object.class, values);
        }
    }

    @Override
    public boolean hasNext() {
        return tg.hasNext();
    }

    @Override
    public Map<String, Object> next() {
        Object[] tuple = tg.next();
        Map<String, Object> m = new HashMap<>(tuple.length);
        for (int i = 0; i < tuple.length; i++) {
            m.put(ids[i], tuple[i]);
        }
        return m;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<Map<String, Object>> iterator() {
        return this;
    }

    @Override
    public int count() {
        return tg.count();
    }

    @Override
    public int done() {
        return tg.done();
    }

    @Override
    public void reset() {
        tg.reset();
    }
}

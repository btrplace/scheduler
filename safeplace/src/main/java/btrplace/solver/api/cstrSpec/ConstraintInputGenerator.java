package btrplace.solver.api.cstrSpec;

import btrplace.model.Model;
import btrplace.solver.api.cstrSpec.generator.AllTuplesGenerator;
import btrplace.solver.api.cstrSpec.generator.Generator;
import btrplace.solver.api.cstrSpec.generator.RandomTuplesGenerator;
import btrplace.solver.api.cstrSpec.spec.term.Var;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class ConstraintInputGenerator implements Generator<List<Object>> {


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
    public List<Object> next() {
        Object[] tuple = tg.next();
        List<Object> m = new ArrayList<>(tuple.length);
        for (int i = 0; i < tuple.length; i++) {
            m.add(tuple[i]);
        }
        return m;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<List<Object>> iterator() {
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

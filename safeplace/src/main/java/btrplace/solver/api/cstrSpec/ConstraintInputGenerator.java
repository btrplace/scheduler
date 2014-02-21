package btrplace.solver.api.cstrSpec;

import btrplace.solver.api.cstrSpec.fuzzer.RandomTuplesGenerator;
import btrplace.solver.api.cstrSpec.spec.term.Constant;
import btrplace.solver.api.cstrSpec.spec.term.UserVar;
import btrplace.solver.api.cstrSpec.util.AllTuplesGenerator;
import btrplace.solver.api.cstrSpec.util.Generator;
import btrplace.solver.api.cstrSpec.verification.spec.SpecModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class ConstraintInputGenerator implements Generator<List<Constant>> {

    private Generator<Constant[]> tg;

    public ConstraintInputGenerator(Constraint cstr, SpecModel mo, boolean seq) {

        List<UserVar> params = cstr.getParameters();
        List<List<Constant>> values = new ArrayList<>(params.size());

        for (UserVar v : params) {
            List<Constant> dom = v.domain(mo);
            values.add(dom);
        }
        //System.out.println(cstr.pretty());
        //System.out.println(values);
        if (seq) {
            tg = new AllTuplesGenerator<>(Constant.class, values);
        } else {
            tg = new RandomTuplesGenerator<>(Constant.class, values);
        }
    }

    @Override
    public boolean hasNext() {
        return tg.hasNext();
    }

    @Override
    public List<Constant> next() {
        Constant[] tuple = tg.next();
        List<Constant> m = new ArrayList<>(tuple.length);
        Collections.addAll(m, tuple);
        return m;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<List<Constant>> iterator() {
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

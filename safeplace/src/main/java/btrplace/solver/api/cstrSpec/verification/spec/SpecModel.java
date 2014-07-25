package btrplace.solver.api.cstrSpec.verification.spec;

import btrplace.model.DefaultModel;
import btrplace.model.Model;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class SpecModel {

    private SpecMapping sm;

    private Map<String, VerifDomain> vDoms;

    private Model mo;

    private Map<String, Object> vars;

    public SpecModel() {
        this(new DefaultModel());
    }

    public SpecModel(Model mo) {
        this.mo = mo;
        sm = new SpecMapping(mo.getMapping());
        vDoms = new HashMap<>();
        this.vars = new HashMap<>();
    }

    public Model getModel() {
        return mo;
    }

    public SpecMapping getMapping() {
        return sm;
    }

    public void setValue(String label, Object o) {
        vars.put(label, o);
    }

    public Object getValue(String label) {
        Object o = vars.get(label);
        if (o == null) {
            throw new RuntimeException("No value for " + label);
        }
        return o;
    }

    public void add(VerifDomain d) {
        vDoms.put(d.type(), d);
    }

    public Set getVerifDomain(String lbl) {
        VerifDomain v = vDoms.get(lbl);
        if (v == null) {
            return null;
        }
        return v.domain();
    }
}

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

    public SpecModel() {
        this(new DefaultModel());
    }

    public SpecModel(Model mo) {
        this.mo = mo;
        sm = new SpecMapping(mo.getMapping());
        vDoms = new HashMap<>();
    }

    public Model getModel() {
        return mo;
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

    public SpecMapping getMapping() {
        return sm;
    }
}

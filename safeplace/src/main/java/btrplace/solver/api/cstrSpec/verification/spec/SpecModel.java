package btrplace.solver.api.cstrSpec.verification.spec;

import btrplace.model.DefaultModel;
import btrplace.model.Model;
import btrplace.plan.ReconfigurationPlan;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class SpecModel {

    private SpecMapping sm;

    private Map<String, VerifDomain> vDoms;

    private Model mo;

    private ReconfigurationPlan plan;

    //private Map<String, Object> vars;

    private LinkedList<Map<String, Object>> stack;

    public SpecModel() {
        this(new DefaultModel());
    }

    public ReconfigurationPlan getPlan() {
        return plan;
    }

    public SpecModel(Model mo) {
        this.mo = mo;
        sm = new SpecMapping(mo.getMapping());
        vDoms = new HashMap<>();
        //this.vars = new HashMap<>();
        stack = new LinkedList<>();
        stack.add(new HashMap<String, Object>());
    }

    public Model getModel() {
        return mo;
    }

    public SpecMapping getMapping() {
        return sm;
    }

    public void setValue(String label, Object o) {
        stack.getFirst().put(label, o);
        //vars.put(label, o);
    }

    public Object getValue(String label) {
        return stack.getFirst().get(label);
        /*Object o = vars.get(label);
        if (o == null) {
            throw new RuntimeException("No value for " + label);
        }
        return o;*/
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

    @Override
    public String toString() {
        return stack.toString();
        //return vars.toString();
    }

    public void saveStack() {
        stack.push(new HashMap<String, Object>());
    }

    public void restoreStack() {
        stack.pop();
    }
}

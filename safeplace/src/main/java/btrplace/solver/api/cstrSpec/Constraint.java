package btrplace.solver.api.cstrSpec;

import btrplace.model.Model;
import btrplace.plan.ReconfigurationPlan;
import net.minidev.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author Fabien Hermenier
 */
public class Constraint {

    private Proposition p;

    private Proposition not;
    private List<Variable> params;

    private Map<String, Variable> vars;

    private String cstrName;

    private String marshal;

    public Constraint(String n, String m, Proposition p, List<Variable> params) {
        this.p = p;
        this.not = p.not();
        this.cstrName = n;
        this.params = params;
        this.marshal = m;
        vars = new HashMap<>(params.size());
        for (Variable v : params) {
            vars.put(v.label(), v);
        }
    }

    public Proposition getProposition() {
        return p;
    }

    public List<Variable> getParameters() {
        return params;
    }

    public Boolean instantiate(Map<String, Object> values, ReconfigurationPlan p) {
        for (Map.Entry<String, Object> val : values.entrySet()) {
            Variable var = vars.get(val.getKey());
            var.set(val.getValue());
        }
        Model res = p.getResult();
        if (res == null) {     //TODO: flaw ?
            return false;
        }
        Boolean bOk = this.p.evaluate(res);
        Boolean bKO = this.not.evaluate(res);

        if (bOk == null || bKO == null) {
            throw new RuntimeException("Both null !\ngood:" + this.p + "\nnotGood: " + not + "\n" + p.getOrigin().getMapping().toString());
        }
        if (bOk && bKO) {
            throw new RuntimeException(values + " good and bad !\ngood:" + this.p + "\nnotGood: " + not + "\n" + res.getMapping().toString());
        } else if (!(bOk || bKO)) {
            throw new RuntimeException("Nor good or bad !\ngood:" + this.p + "\nnotGood: " + not + "\n" + p.getOrigin().getMapping().toString());
        }
        this.reset();
        return bOk;
    }

    public void reset() {
        for (Variable var : params) {
            var.unset();
        }
    }

    public String getConstraintName() {
        return cstrName;
    }

    public String getMarshal() {
        return marshal;
    }

    public JSONObject toJSON() {
        JSONObject o = new JSONObject();
        o.put("id", cstrName);
        o.put("proposition", p.toString());
        JSONObject jps = new JSONObject();
        for (Variable v : params) {
            jps.put(v.label(), v.type().toString());
        }
        o.put("parameters", jps);
        return o;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append(cstrName).append(" ");
        b.append(" ::= ").append(p);
        return b.toString();
    }

    private String pretty(List<Variable> ps) {
        StringBuilder b = new StringBuilder("(");
        Iterator<Variable> ite = ps.iterator();
        b.append(ite.next().label());
        while (ite.hasNext()) {
            b.append(", ").append(ite.next().label());
        }
        return b.append(")").toString();
    }
}

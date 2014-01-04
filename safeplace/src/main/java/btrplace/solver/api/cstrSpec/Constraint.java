package btrplace.solver.api.cstrSpec;

import btrplace.model.Model;
import btrplace.model.Node;
import btrplace.model.VM;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.api.cstrSpec.invariant.Proposition;
import btrplace.solver.api.cstrSpec.invariant.Var;
import btrplace.solver.api.cstrSpec.invariant.Variable;
import net.minidev.json.JSONObject;

import java.util.*;

/**
 * @author Fabien Hermenier
 */
public class Constraint {

    private Proposition p;

    private Proposition not;
    private List<Var> params;

    private Map<String, Var> vars;

    private String cstrName;

    private String marshal;

    public Constraint(String n, String m, Proposition p, List<Var> params) {
        this.p = p;
        this.not = p.not();
        this.cstrName = n;
        this.params = params;
        this.marshal = m;
        vars = new HashMap<>(params.size());
        for (Var v : params) {
            vars.put(v.label(), v);
        }
    }

    public Proposition getProposition() {
        return p;
    }

    public List<Var> getParameters() {
        return params;
    }

    private Set<Node> getInvolvedNodes(Map<String, Object> values, ReconfigurationPlan p) {
        return Collections.emptySet();
    }

    private Set<VM> getInvolvedVMs(Map<String, Object> values, ReconfigurationPlan p) {
        for (Map.Entry<String, Object> val : values.entrySet()) {
            Var var = vars.get(val.getKey());
            var.set(val.getValue());
        }

        return Collections.emptySet();
    }


    public Boolean instantiate(Map<String, Object> values, Model res) {
        for (Map.Entry<String, Object> val : values.entrySet()) {
            Var var = vars.get(val.getKey());
            var.set(val.getValue());
        }
        if (res == null) {     //TODO: flaw ?
            //throw new RuntimeException("Unable to apply the plan");
            return false;
        }
        Boolean bOk = this.p.evaluate(res);
        Boolean bKO = this.not.evaluate(res);

        if (bOk == null || bKO == null) {
            throw new RuntimeException("Both null !\ngood:" + this.p + "\nnotGood: " + not + "\n" + res.getMapping().toString());
        }
        if (bOk && bKO) {
            throw new RuntimeException(values + " good and bad !\ngood:" + this.p + "\nnotGood: " + not + "\n" + res.getMapping().toString());
        } else if (!(bOk || bKO)) {
            throw new RuntimeException("Nor good or bad !\ngood:" + this.p + "\nnotGood: " + not + "\n" + res.getMapping().toString());
        }
        this.reset();
        return bOk;
    }

    public void reset() {
        for (Var var : params) {
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
        for (Var v : params) {
            jps.put(v.label(), v.type().toString());
        }
        o.put("parameters", jps);
        return o;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append(cstrName).append("(");
        Iterator<Var> ite = params.iterator();
        if (ite.hasNext()) {
            Var v = ite.next();
            b.append(v.pretty());
        }
        while (ite.hasNext()) {
            Var v = ite.next();
            b.append(", ").append(v.pretty());
        }
        b.append(") ::=\n");
        b.append("\t\"\"\"").append(marshal).append("\"\"\"\n");
        b.append('\t').append(p);
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

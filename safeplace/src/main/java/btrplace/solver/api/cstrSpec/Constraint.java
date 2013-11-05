package btrplace.solver.api.cstrSpec;

import net.minidev.json.JSONObject;

import java.io.Reader;
import java.util.*;

/**
 * @author Fabien Hermenier
 */
public class Constraint {

    private Proposition p;

    private List<Variable> params;

    private Map<String, Variable> vars;

    private String cstrName;

    private String marshal;

    public Constraint(Reader in) {

    }
    public Constraint(String n, String m, Proposition p, List<Variable> params) {
        this.p = p;
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

    public void instantiate(Map<String, Object> values) {
        for (Map.Entry<String, Object> val : values.entrySet()) {
            Variable var = vars.get(val.getKey());
            var.set(val.getValue());
        }
    }

    public void reset() {
        for (Variable var : params) {
            var.unset();
        }
    }

    public List<Map<String,Object>> expandParameters() {
        Object [][] doms = new Object[params.size()][];
        int [] indexes = new int[params.size()];
        int i = 0;
        int nbStates = 1;
        List<Map<String, Object>> all = new ArrayList<>();
        for (Variable v : params) {
            indexes[i] = 0;
            Set<Object> sDom = v.domain();
            doms[i] = sDom.toArray(new Object[sDom.size()]);
            nbStates *= doms[i].length;
            i++;
        }
        for (int k = 0; k < nbStates; k++) {
            Map<String, Object> entries = new HashMap<>(params.size());
            for (int x = 0; x < params.size(); x++) {
                entries.put(params.get(x).label(), doms[x][indexes[x]]);
            }
            for (int x = 0; x < params.size(); x++) {
                indexes[x]++;
                if (indexes[x] < doms[x].length) {
                    break;
                }
                indexes[x] = 0;
            }
            all.add(entries);
        }
        return all;
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
}

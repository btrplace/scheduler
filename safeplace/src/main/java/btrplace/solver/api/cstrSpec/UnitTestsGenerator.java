package btrplace.solver.api.cstrSpec;

import btrplace.json.JSONConverterException;
import btrplace.json.model.ModelConverter;
import btrplace.model.Model;
import btrplace.solver.api.cstrSpec.type.VMType;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

import java.io.IOException;
import java.io.Writer;
import java.util.*;

/**
 * @author Fabien Hermenier
 */
public class UnitTestsGenerator {

    private JSONObject toJSON(Map<String, Object> m) {
        JSONObject o = new JSONObject();
        for (Map.Entry<String, Object> e : m.entrySet()) {
            o.put(e.getKey(), e.toString());
        }
        return o;
    }
    public void generate(Constraint c, Writer w) throws IOException, JSONConverterException {
        ModelGenerator gen = new ModelGenerator();
        List<Model> models = gen.all(VMType.getInstance().domain().size(), VMType.getInstance().domain().size());
        Proposition good = c.getProposition();
        Proposition noGood = good.not();
        JSONObject o = new JSONObject();
        o.put("constraint", c.toJSON());
        JSONArray scenarios = new JSONArray();
        ModelConverter mc = new ModelConverter();
        for (Model mo : models) {
            JSONObject jsonModel = mc.toJSON(mo);
            JSONObject scenario = new JSONObject();
            scenario.put("model", jsonModel);
            JSONArray tests = new JSONArray();
            for (Map<String, Object> vals : expandParameters(c)) {
                JSONObject test = new JSONObject();
                test.put("values", toJSON(vals));
                c.instantiate(vals);
                Boolean gr = good.evaluate(mo);
                Boolean ngr = noGood.evaluate(mo);
                if (gr == null || ngr == null) {
                    throw new RuntimeException(mo.toString());
                }
                if (!(gr||ngr)) {
                    throw new RuntimeException("Nor good or bad !\n" + mo.toString());
                }
                if (gr && ngr) {
                    throw new RuntimeException("Good and bad !\n" + mo.toString());
                }
                test.put("consistent", gr);
                tests.add(test);
                c.reset();
            }
            scenario.put("tests", tests);
            scenarios.add(scenario);
        }
        o.put("scenarios", scenarios);
        o.writeJSONString(w);
    }

    private List<Map<String,Object>> expandParameters(Constraint c) {
        List<Variable> params = c.getParameters();
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
}

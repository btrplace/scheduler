package btrplace.solver.api.cstrSpec;

import btrplace.json.JSONConverterException;
import btrplace.json.model.constraint.ConstraintsConverter;
import btrplace.model.Element;
import btrplace.model.Model;
import btrplace.model.constraint.SatConstraint;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.api.cstrSpec.generator.ModelsGenerator;
import btrplace.solver.api.cstrSpec.generator.ReconfigurationPlansGenerator;
import btrplace.solver.api.cstrSpec.type.NodeType;
import btrplace.solver.api.cstrSpec.type.VMType;

import java.io.IOException;
import java.util.*;

/**
 * @author Fabien Hermenier
 */
public class TestCasesGenerator {

    public String marshal(String json, Map<String, Object> params) {
        for (Map.Entry<String, Object> e : params.entrySet()) {
            Object v = e.getValue();
            Object s = e.getValue().toString();
            if (v instanceof Element) {
                s = ((Element) v).id();
            } else if (v instanceof Collection) {
                StringBuilder str = new StringBuilder();
                for (Iterator<Object> ite = ((Collection) v).iterator(); ite.hasNext(); ) {
                    Object o = ite.next();
                    str.append(((Element) o).id());
                    if (ite.hasNext()) {
                        str.append(", ");
                    }
                }
                s = str;
            }
            json = json.replaceAll("@" + e.getKey(), s.toString());
        }
        return json;
    }

    public List<TestCase> generate(Constraint c) throws JSONConverterException, IOException {
        List<TestCase> tests = new ArrayList<>();
        Proposition good = c.getProposition();
        Proposition noGood = good.not();
        ConstraintsConverter cstrC = ConstraintsConverter.newBundle();


        ModelsGenerator gen = new ModelsGenerator(NodeType.getInstance().domain().size(), VMType.getInstance().domain().size());
        for (Model mo : gen) {
            cstrC.setModel(mo);

            ReconfigurationPlansGenerator pg = new ReconfigurationPlansGenerator(mo);
            for (ReconfigurationPlan p : pg) {
                for (Map<String, Object> vals : expandParameters(c)) {
                    SatConstraint cstr = (SatConstraint) cstrC.fromJSON(marshal(c.getMarshal(), vals));
                    //c.instantiate(vals);
                    Boolean gr = good.evaluate(mo);
                    Boolean ngr = noGood.evaluate(mo);
                    if (gr == null || ngr == null) {
                        throw new RuntimeException("Both null !\ngood:" + good + "\nnotGood: " + noGood + "\n" + mo.getMapping().toString());
                    }
                    if (!(gr || ngr)) {
                        throw new RuntimeException("Nor good or bad !\ngood:" + good + "\nnotGood: " + noGood + "\n" + mo.getMapping().toString());
                    }
                    /*if (gr && ngr) {
                        throw new RuntimeException("good and bad !\ngood:" + good + "\nnotGood: " + noGood + "\n" + mo.getMapping().toString());
                    } */
                    c.reset();
                    TestCase tu = new TestCase(p, cstr, gr);
                    tests.add(tu);
                }
            }

        }
        return tests;
    }

    private List<Map<String, Object>> expandParameters(Constraint c) {
        List<Variable> params = c.getParameters();
        Object[][] doms = new Object[params.size()][];
        int[] indexes = new int[params.size()];
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
package btrplace.solver.api.cstrSpec.verification;

import btrplace.model.constraint.SatConstraint;
import btrplace.plan.ReconfigurationPlan;

/**
 * @author Fabien Hermenier
 */
public class TestCase {

    private ReconfigurationPlan plan;

    private SatConstraint cstr;

    private boolean isConsistent;

    private int num;

    public TestCase(int num, ReconfigurationPlan p, SatConstraint cstr, boolean c) {
        this.num = num;
        this.plan = p;
        this.cstr = cstr;
        this.isConsistent = c;
    }

/*    public static TestCase fromJSON(JSONObject o) throws JSONConverterException {
        ConstraintsConverter cv = ConstraintsConverter.newBundle();
        ModelConverter mc = new ModelConverter();
        Model mo = mc.fromJSON((JSONObject) o.get("model"));
        cv.setModel(mo);
        SatConstraint cstr = (SatConstraint) cv.fromJSON((JSONObject) o.get("cstr"));
        boolean c = Boolean.parseBoolean(o.get("isConsistent").toString());
        return new TestCase(mo, cstr, c);
    }    */

    public boolean isConsistent() {
        return this.isConsistent;
    }

    public ReconfigurationPlan getPlan() {
        return plan;
    }

    public SatConstraint getSatConstraint() {
        return this.cstr;
    }

    /*public JSONObject toJSON() throws JSONConverterException {
        JSONObject jo = new JSONObject();
        ConstraintsConverter cv = ConstraintsConverter.newBundle();
        ModelConverter mc = new ModelConverter();
        cv.setModel(mo);
        jo.put("cstr", cv.toJSON(cstr));
        jo.put("model", mc.toJSON(mo));
        jo.put("consistent", isConsistent);
        return jo;
    }     */

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("cstr: ").append(cstr).append("\n");
        b.append("plan:\n").append(plan).append("\n");
        b.append("consistent:").append(isConsistent);
        return b.toString();
    }

    public int num() {
        return num;
    }
}


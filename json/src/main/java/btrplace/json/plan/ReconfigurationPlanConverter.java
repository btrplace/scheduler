package btrplace.json.plan;

import btrplace.json.JSONConverter;
import btrplace.json.JSONConverterException;
import btrplace.json.model.ModelConverter;
import btrplace.model.Model;
import btrplace.plan.DefaultReconfigurationPlan;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.event.Action;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

/**
 * JSON converter for {@link ReconfigurationPlan}.
 *
 * @author Fabien Hermenier
 */
public class ReconfigurationPlanConverter extends JSONConverter<ReconfigurationPlan> {

    @Override
    public ReconfigurationPlan fromJSON(JSONObject ob) throws JSONConverterException {

        if (!ob.containsKey("origin")) {
            throw new JSONConverterException("Key 'origin' is expected to extract the source model from the plan");
        }

        if (!ob.containsKey("actions")) {
            throw new JSONConverterException("Key 'actions' is expected to extract the list of actions from the plan");
        }

        ModelConverter c = new ModelConverter();
        ActionConverter ac = new ActionConverter();
        Model m = c.fromJSON((JSONObject) ob.get("origin"));
        ReconfigurationPlan plan = new DefaultReconfigurationPlan(m);
        for (Action a : ac.fromJSON((JSONArray) ob.get("actions"))) {
            plan.add(a);
        }
        return plan;
    }

    @Override
    public JSONObject toJSON(ReconfigurationPlan plan) throws JSONConverterException {

        JSONObject ob = new JSONObject();
        ModelConverter c = new ModelConverter();
        ActionConverter ac = new ActionConverter();
        Model src = plan.getOrigin();
        ob.put("origin", c.toJSON(src));

        JSONArray actions = new JSONArray();
        for (Action a : plan.getActions()) {
            actions.add(ac.toJSON(a));
        }
        ob.put("actions", actions);
        return ob;
    }

}

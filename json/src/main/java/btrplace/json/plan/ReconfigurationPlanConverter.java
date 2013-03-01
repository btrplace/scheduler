package btrplace.json.plan;

import btrplace.json.JSONConverter;
import btrplace.json.JSONConverterException;
import btrplace.json.model.ModelConverter;
import btrplace.model.Model;
import btrplace.plan.Action;
import btrplace.plan.ReconfigurationPlan;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

/**
 * JSON converter for {@link ReconfigurationPlan}.
 *
 * @author Fabien Hermenier
 */
public class ReconfigurationPlanConverter implements JSONConverter<ReconfigurationPlan> {

    @Override
    public ReconfigurationPlan fromJSON(JSONObject in) throws JSONConverterException {
        throw new UnsupportedOperationException();
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

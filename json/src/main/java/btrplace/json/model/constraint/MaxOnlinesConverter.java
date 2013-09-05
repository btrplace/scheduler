package btrplace.json.model.constraint;

import btrplace.json.JSONConverterException;
import btrplace.model.constraint.MaxOnline;
import net.minidev.json.JSONObject;

/**
 * JSON Converter for the constraint {@link MaxOnlinesConverter}.
 *
 * @author Tu Huynh Dang
 */
public class MaxOnlinesConverter extends ConstraintConverter<MaxOnline> {
    @Override
    public Class<MaxOnline> getSupportedConstraint() {
        return MaxOnline.class;
    }

    @Override
    public String getJSONId() {
        return "maxOnlines";
    }

    @Override
    public MaxOnline fromJSON(JSONObject in) throws JSONConverterException {
        checkId(in);
        return new MaxOnline(requiredNodes(in, "nodes"), requiredInt(in, "amount"),
                requiredBoolean(in, "continuous"));
    }

    @Override
    public JSONObject toJSON(MaxOnline maxOnline) throws JSONConverterException {
        JSONObject c = new JSONObject();
        c.put("id", getJSONId());
        c.put("nodes", nodesToJSON(maxOnline.getInvolvedNodes()));
        c.put("amount", maxOnline.getAmount());
        c.put("continuous", maxOnline.isContinuous());
        return c;
    }
}

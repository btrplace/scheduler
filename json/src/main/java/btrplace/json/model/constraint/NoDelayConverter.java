package btrplace.json.model.constraint;

import btrplace.json.JSONConverterException;
import btrplace.model.constraint.NoDelay;
import net.minidev.json.JSONObject;

/**
 * Created by vkherbac on 01/09/14.
 */
public class NoDelayConverter extends ConstraintConverter<NoDelay> {

    @Override
    public Class<NoDelay> getSupportedConstraint() {
        return NoDelay.class;
    }

    @Override
    public String getJSONId() {
        return "noDelay";
    }

    @Override
    public NoDelay fromJSON(JSONObject in) throws JSONConverterException {
        checkId(in);
        return new NoDelay(requiredVM(in, "vm"));
    }

    @Override
    public JSONObject toJSON(NoDelay noDelay) throws JSONConverterException {
        JSONObject c = new JSONObject();
        c.put("id", getJSONId());
        c.put("vm", toJSON(noDelay.getInvolvedVMs().iterator().next()));
        c.put("continuous", noDelay.isContinuous());
        return c;
    }
}

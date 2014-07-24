package btrplace.solver.api.cstrSpec;

import btrplace.json.JSONConverterException;
import btrplace.json.plan.ReconfigurationPlanConverter;
import btrplace.plan.ReconfigurationPlan;
import com.google.gson.*;

import java.lang.reflect.Type;

/**
 * @author Fabien Hermenier
 */
public class JSONUtils {

    private static final JSONUtils instance = new JSONUtils();

    private Gson gson;

    private JSONUtils() {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Constraint.class, new JsonSerializer<Constraint>() {
            @Override
            public JsonElement serialize(Constraint src, Type typeOfSrc, JsonSerializationContext context) {
                return new JsonPrimitive(src.pretty());
            }
        });
        builder.registerTypeAdapter(ReconfigurationPlan.class, new JsonSerializer<ReconfigurationPlan>() {
            @Override
            public JsonElement serialize(ReconfigurationPlan src, Type typeOfSrc, JsonSerializationContext context) {
                ReconfigurationPlanConverter conv = new ReconfigurationPlanConverter();
                try {
                    return new JsonPrimitive(conv.toJSONString(src));
                } catch (JSONConverterException ex) {
                    throw new RuntimeException();
                }
            }
        });

        gson = builder.create();
    }

    public static JSONUtils getInstance() {
        return instance;
    }

    public String toJSON(CTestCase c) {
        return gson.toJson(c);
    }

    public Gson getGson() {
        return gson;
    }


}

/*
 * Copyright (c) 2014 University Nice Sophia Antipolis
 *
 * This file is part of btrplace.
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.btrplace.safeplace;

import com.google.gson.*;
import org.btrplace.json.JSONConverterException;
import org.btrplace.json.plan.ReconfigurationPlanConverter;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.safeplace.fuzzer.TestCase;

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

    public String toJSON(TestCase c) {
        return gson.toJson(c);
    }

    public Gson getGson() {
        return gson;
    }


}

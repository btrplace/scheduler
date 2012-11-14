/*
 * Copyright (c) 2012 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package btrplace.instance.json;

import btrplace.model.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

/**
 * Class to serialize/unserialize a model using the JSON format.
 *
 * @author Fabien Hermenier
 */
public class JSONModel {

    private JSONParser p;

    private JSONMapping cfgParser;

    private JSONIntResource intRcParser;

    private JSONSatConstraints cstrParser;

    public JSONModel() {
        p = new JSONParser();
        cfgParser = new JSONMapping();
        intRcParser = new JSONIntResource();
        cstrParser = new JSONSatConstraints();
    }

    public JSONObject toJSON(Model i) {
        JSONArray rcs = new JSONArray();

        for (IntResource rc : i.getResources()) {
            rcs.add(intRcParser.toJSON(rc));
        }

        JSONObject o = new JSONObject();
        o.put("mapping", cfgParser.toJSON(i.getMapping()));
        o.put("resources", rcs);

        JSONArray jcstrs = new JSONArray();
        for (SatConstraint cstr : i.getConstraints()) {
            jcstrs.add(cstrParser.toJSON(cstr));
        }
        o.put("constraints", jcstrs);
        return o;
    }

    public Model fromJSON(Reader in) throws IOException {
        try {
            JSONObject o = (JSONObject) p.parse(in);
            if (!o.containsKey("mapping") || !o.containsKey("resources")) {
                return null;
            }
            JSONObject jc = (JSONObject) o.get("mapping");
            Mapping cfg = cfgParser.fromJSON(jc.toJSONString());

            Model i = new DefaultModel(cfg);
            JSONArray jrc = (JSONArray) o.get("resources");
            for (Object ob : jrc) {
                i.attach(intRcParser.fromJSON(((JSONObject) ob).toJSONString()));
            }

            JSONArray jcstrs = (JSONArray) o.get("constraints");
            for (Object ob : jcstrs) {
                i.attach(cstrParser.fromJSON((JSONObject) ob));
            }

            return i;
        } catch (ParseException e) {
            return null;
        }
    }

    public Model fromJSON(String str) {
        StringReader in = null;
        try {
            in = new StringReader(str);
            return fromJSON(in);
        } catch (IOException e) {
            return null;
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }
}

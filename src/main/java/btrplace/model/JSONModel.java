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

package btrplace.model;

import btrplace.JSONConverter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * Class to serialize/unserialize a model using the JSON format.
 *
 * @author Fabien Hermenier
 */
public class JSONModel implements JSONConverter<Model> {

    private JSONParser p;

    private JSONMapping cfgParser;

    private JSONAttributes attrsParser;

    //private JSONModelViewConverters viewsConverter;

    public JSONModel() {
        p = new JSONParser();
        cfgParser = new JSONMapping();
        attrsParser = new JSONAttributes();
        //  viewsConverter = new JSONModelViewConverters();
    }

    /*public JSONModelViewConverters getViewsConverter() {
        return viewsConverter;
    }

    public void setViewsConverter(JSONModelViewConverters c) {
        this.viewsConverter = c;
    } */

    @Override
    public JSONObject toJSON(Model i) {
        JSONArray rcs = new JSONArray();

        /*for (ModelView v : i.getViews()) {
            rcs.add(viewsConverter.toJSON(v));
        } */

        JSONObject o = new JSONObject();
        o.put("mapping", cfgParser.toJSON(i.getMapping()));
        o.put("resources", rcs);
        return o;
    }

    @Override
    public Model fromJSON(JSONObject o) {
        if (!o.containsKey("mapping") || !o.containsKey("resources")) {
            return null;
        }
        JSONObject jc = (JSONObject) o.get("mapping");
        Mapping cfg = cfgParser.fromJSON(jc);

        Model i = new DefaultModel(cfg);

        return i;
    }
}

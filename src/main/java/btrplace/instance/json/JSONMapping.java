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

import btrplace.model.DefaultMapping;
import btrplace.model.Mapping;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.UUID;

/**
 * Class to serialize and un-serialize {@link Mapping}.
 *
 * @author Fabien Hermenier
 */
public class JSONMapping {

    private JSONParser parser = new JSONParser();

    public JSONObject toJSON(Mapping c) {
        JSONObject o = new JSONObject();
        o.put("offlineNodes", Utils.toJSON(c.getOfflineNodes()));
        o.put("waitingVMs", Utils.toJSON(c.getWaitingVMs()));

        JSONObject ons = new JSONObject();
        for (UUID n : c.getOnlineNodes()) {
            JSONObject w = new JSONObject();
            w.put("runningVMs", Utils.toJSON(c.getRunningVMs(n)));
            w.put("sleepingVMs", Utils.toJSON(c.getRunningVMs(n)));
            ons.put(n.toString(), w);
        }
        o.put("onlineNodes", ons);
        return o;
    }

    public Mapping fromJSON(String in) {
        StringReader r = null;
        try {
            r = new StringReader(in);
            return fromJSON(r);
        } catch (IOException e) {
            return null;
        } finally {
            if (r != null) {
                r.close();
            }
        }
    }

    public Mapping fromJSON(Reader in) throws IOException {
        Mapping c = new DefaultMapping();
        try {
            JSONObject o = (JSONObject) parser.parse(in);
            if (!o.containsKey("offlineNodes") || !o.containsKey("waitingVMs")
                    || !o.containsKey("onlineNodes")) {
                return null;
            }
            for (UUID u : Utils.fromJSON((JSONArray) o.get("offlineNodes"))) {
                c.addOfflineNode(u);
            }
            for (UUID u : Utils.fromJSON((JSONArray) o.get("waitingVMs"))) {
                c.addWaitingVM(u);
            }
            JSONObject ons = (JSONObject) o.get("onlineNodes");
            for (Object k : ons.keySet()) {
                UUID u = UUID.fromString((String) k);
                JSONObject on = (JSONObject) ons.get(k);
                if (!on.containsKey("runningVMs") || !on.containsKey("sleepingVMs")) {
                    return null;
                }
                c.addOnlineNode(u);
                for (UUID vmId : Utils.fromJSON((JSONArray) on.get("runningVMs"))) {
                    c.setVMRunOn(vmId, u);
                }
                for (UUID vmId : Utils.fromJSON((JSONArray) on.get("sleepingVMs"))) {
                    c.setVMRunOn(vmId, u);
                }
            }


        } catch (ParseException ex) {
            return null;
        }
        return c;
    }
}

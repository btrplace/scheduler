/*
 * Copyright (c) 2013 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
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

package btrplace.json.model;

import btrplace.json.AbstractJSONObjectConverter;
import btrplace.json.JSONConverterException;
import btrplace.model.DefaultMapping;
import btrplace.model.Mapping;
import net.minidev.json.JSONObject;


/**
 * Class to serialize and un-serialize {@link Mapping}.
 *
 * @author Fabien Hermenier
 */
public class MappingConverter extends AbstractJSONObjectConverter<Mapping> {

    @Override
    public JSONObject toJSON(Mapping c) {
        JSONObject o = new JSONObject();
        o.put("offlineNodes", elementsToJSON(c.getOfflineNodes()));
        o.put("readyVMs", elementsToJSON(c.getReadyVMs()));

        JSONObject ons = new JSONObject();
        for (int n : c.getOnlineNodes()) {
            JSONObject w = new JSONObject();
            w.put("runningVMs", elementsToJSON(c.getRunningVMs(n)));
            w.put("sleepingVMs", elementsToJSON(c.getSleepingVMs(n)));
            ons.put(Integer.toString(n), w);
        }
        o.put("onlineNodes", ons);
        return o;
    }

    @Override
    public Mapping fromJSON(JSONObject o) throws JSONConverterException {
        Mapping c = new DefaultMapping();
        for (int u : requiredElements(o, "offlineNodes")) {
            c.addOfflineNode(u);
        }
        for (int u : requiredElements(o, "readyVMs")) {
            c.addReadyVM(u);
        }
        JSONObject ons = (JSONObject) o.get("onlineNodes");
        for (Object k : ons.keySet()) {
            int u = Integer.parseInt((String) k);
            JSONObject on = (JSONObject) ons.get(k);
            c.addOnlineNode(u);
            for (int vmId : requiredElements(on, "runningVMs")) {
                c.addRunningVM(vmId, u);
            }
            for (int vmId : requiredElements(on, "sleepingVMs")) {
                c.addSleepingVM(vmId, u);
            }
        }

        return c;
    }
}

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
import btrplace.model.Node;
import btrplace.model.VM;
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
        o.put("offlineNodes", nodesToJSON(c.getOfflineNodes()));
        o.put("readyVMs", vmsToJSON(c.getReadyVMs()));

        JSONObject ons = new JSONObject();
        for (Node n : c.getOnlineNodes()) {
            JSONObject w = new JSONObject();
            w.put("runningVMs", vmsToJSON(c.getRunningVMs(n)));
            w.put("sleepingVMs", vmsToJSON(c.getSleepingVMs(n)));
            ons.put(Integer.toString(n.id()), w);
        }
        o.put("onlineNodes", ons);
        return o;
    }

    @Override
    public Mapping fromJSON(JSONObject o) throws JSONConverterException {
        Mapping c = new DefaultMapping();
        for (Node u : requiredNodes(o, "offlineNodes")) {
            c.addOfflineNode(u);
        }
        for (VM u : requiredVMs(o, "readyVMs")) {
            c.addReadyVM(u);
        }
        JSONObject ons = (JSONObject) o.get("onlineNodes");
        for (String nId : ons.keySet()) {
            Node u = getOrMakeNode(Integer.parseInt(nId));
            JSONObject on = (JSONObject) ons.get(nId);
            c.addOnlineNode(u);
            for (VM vm : requiredVMs(on, "runningVMs")) {
                c.addRunningVM(vm, u);
            }
            for (VM vm : requiredVMs(on, "sleepingVMs")) {
                c.addSleepingVM(vm, u);
            }
        }

        return c;
    }
}

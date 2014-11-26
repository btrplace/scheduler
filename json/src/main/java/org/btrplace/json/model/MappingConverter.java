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

package org.btrplace.json.model;

import net.minidev.json.JSONObject;
import org.btrplace.json.AbstractJSONObjectConverter;
import org.btrplace.json.JSONConverterException;
import org.btrplace.model.Mapping;
import org.btrplace.model.Node;
import org.btrplace.model.VM;

import java.util.Map;


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
        if (getModel() == null) {
            throw new JSONConverterException("Unable to extract VMs without a model to use as a reference");
        }
        Mapping c = getModel().getMapping();
        for (Node u : requiredNodes(o, "offlineNodes")) {
            c.addOfflineNode(u);
        }
        for (VM u : requiredVMs(o, "readyVMs")) {
            c.addReadyVM(u);
        }
        JSONObject ons = (JSONObject) o.get("onlineNodes");
        for (Map.Entry<String, Object> e : ons.entrySet()) {
            String nId = e.getKey();
            Node u = getOrMakeNode(Integer.parseInt(nId));
            JSONObject on = (JSONObject) e.getValue();
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

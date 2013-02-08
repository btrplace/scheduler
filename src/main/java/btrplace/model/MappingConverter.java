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
import btrplace.JSONConverterException;
import btrplace.Utils;
import net.minidev.json.JSONObject;

import java.util.UUID;

/**
 * Class to serialize and un-serialize {@link Mapping}.
 *
 * @author Fabien Hermenier
 */
public class MappingConverter implements JSONConverter<Mapping> {

    @Override
    public JSONObject toJSON(Mapping c) {
        JSONObject o = new JSONObject();
        o.put("offlineNodes", Utils.toJSON(c.getOfflineNodes()));
        o.put("readyVMs", Utils.toJSON(c.getReadyVMs()));

        JSONObject ons = new JSONObject();
        for (UUID n : c.getOnlineNodes()) {
            JSONObject w = new JSONObject();
            w.put("runningVMs", Utils.toJSON(c.getRunningVMs(n)));
            w.put("sleepingVMs", Utils.toJSON(c.getSleepingVMs(n)));
            ons.put(n.toString(), w);
        }
        o.put("onlineNodes", ons);
        return o;
    }

    @Override
    public Mapping fromJSON(JSONObject o) throws JSONConverterException {
        Mapping c = new DefaultMapping();
        for (UUID u : Utils.requiredUUIDs(o, "offlineNodes")) {
            c.addOfflineNode(u);
        }
        for (UUID u : Utils.requiredUUIDs(o, "readyVMs")) {
            c.addReadyVM(u);
        }
        JSONObject ons = (JSONObject) o.get("onlineNodes");
        for (Object k : ons.keySet()) {
            UUID u = UUID.fromString((String) k);
            JSONObject on = (JSONObject) ons.get(k);
            c.addOnlineNode(u);
            for (UUID vmId : Utils.requiredUUIDs(on, "runningVMs")) {
                c.addRunningVM(vmId, u);
            }
            for (UUID vmId : Utils.requiredUUIDs(on, "sleepingVMs")) {
                c.addSleepingVM(vmId, u);
            }
        }

        return c;
    }
}

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

package btrplace.model.view;

import btrplace.JSONConverter;
import btrplace.model.ModelView;
import org.json.simple.JSONObject;

import java.util.Set;
import java.util.UUID;

/**
 * Serialize/Un-serialize an {@link btrplace.model.view.ShareableResource}.
 *
 * @author Fabien Hermenier
 */
public class JSONShareableResource implements JSONConverter<ShareableResource> {

    //@Override
    public Class<? extends ModelView> getKey() {
        return ShareableResource.class;
    }

    @Override
    public JSONObject toJSON(ShareableResource rc) {
        JSONObject o = new JSONObject();
        o.put("id", rc.getIdentifier());
        Set<UUID> elems = rc.getDefined();
        JSONObject values = new JSONObject();
        for (UUID u : elems) {
            values.put(u, rc.get(u));
        }
        o.put("values", values);
        return o;
    }

    @Override
    public ShareableResource fromJSON(JSONObject o) {
        if (!o.containsKey("id") || !o.containsKey("values")) {
            return null;
        }
        String fullId = o.get("id").toString();
        String rcId = fullId.substring(fullId.lastIndexOf('.') + 1);
        ShareableResource rc = new ShareableResource(rcId);
        JSONObject values = (JSONObject) o.get("values");
        for (Object k : values.keySet()) {
            UUID u = UUID.fromString(k.toString());
            int v = Integer.parseInt(values.get(k).toString());
            rc.set(u, v);
        }
        return rc;
    }
}

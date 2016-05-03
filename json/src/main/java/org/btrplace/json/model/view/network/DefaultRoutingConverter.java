/*
 * Copyright (c) 2016 University Nice Sophia Antipolis
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

package org.btrplace.json.model.view.network;

import net.minidev.json.JSONObject;
import org.btrplace.json.JSONConverterException;
import org.btrplace.model.Model;
import org.btrplace.model.view.network.DefaultRouting;
/**
 * A converter to (un-)serialise a {@link DefaultRouting}.
 * @author Fabien Hermenier
 */
public class DefaultRoutingConverter implements RoutingConverter<DefaultRouting> {

    @Override
    public Class<DefaultRouting> getSupportedRouting() {
        return DefaultRouting.class;
    }

    /**
     * Return the routing identifier.
     *
     * @return {@value "default"}
     */
    @Override
    public String getJSONId() {
        return "default";
    }

    @Override
    public DefaultRouting fromJSON(Model mo, JSONObject in) throws JSONConverterException {
        return new DefaultRouting();
    }

    @Override
    public JSONObject toJSON(DefaultRouting defaultRouting) {
        JSONObject o = new JSONObject();
        o.put("type", getJSONId());
        return o;
    }
}

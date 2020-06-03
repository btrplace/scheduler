/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.json.model.view.network;

import net.minidev.json.JSONObject;
import org.btrplace.json.JSONConverterException;
import org.btrplace.model.Model;
import org.btrplace.model.view.network.DefaultRouting;
/**
 * A converter to (un-)serialise a {@link DefaultRouting}.
 *
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
     * @return {@code "default"}
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

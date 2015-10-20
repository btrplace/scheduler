package org.btrplace.json.model.view.network;

import net.minidev.json.JSONObject;
import org.btrplace.json.JSONConverterException;
import org.btrplace.model.view.network.DefaultRouting;
import org.btrplace.model.view.network.Routing;

/**
 * A converter to (un-)serialise a {@link DefaultRouting}.
 * @author Fabien Hermenier
 */
public class DefaultRoutingConverter extends RoutingConverter<DefaultRouting> {

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
    public Routing fromJSON(JSONObject in) throws JSONConverterException {
        return new DefaultRouting();
    }

    @Override
    public JSONObject toJSON(Routing defaultRouting) throws JSONConverterException {
        JSONObject o = new JSONObject();
        o.put("type", getJSONId());
        return o;
    }
}

/*
 * Copyright  2021 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.json.model.view;

import net.minidev.json.JSONObject;
import org.btrplace.json.JSONConverterException;
import org.btrplace.model.Model;
import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.model.view.ShareableResource;

import java.util.Map;

import static org.btrplace.json.JSONs.*;

/**
 * Serialize/Un-serialize an {@link org.btrplace.model.view.ShareableResource}.
 *
 * @author Fabien Hermenier
 */
public class ShareableResourceConverter implements ModelViewConverter<ShareableResource> {

    /**
     * JSON label for default VM consumption.
     */
    public static final String DEFAULT_CONSUMPTION = "defConsumption";

    /**
     * JSON label for default node capacity.
     */
    public static final String DEFAULT_CAPACITY = "defCapacity";

    /**
     * The label describing nodes.
     */
    public static final String NODES_LABEL = "nodes";

    @Override
    public Class<ShareableResource> getSupportedView() {
        return ShareableResource.class;
    }

    @Override
    public String getJSONId() {
        return "shareableResource";
    }

    @Override
    public JSONObject toJSON(ShareableResource rc) {
        JSONObject o = new JSONObject();
        o.put(ModelViewConverter.IDENTIFIER, getJSONId());
        o.put(DEFAULT_CONSUMPTION, rc.getDefaultConsumption());
        o.put(DEFAULT_CAPACITY, rc.getDefaultCapacity());
        o.put("rcId", rc.getResourceIdentifier());

        final JSONObject vmValues = new JSONObject();
        rc.forEachVMId((id, c) -> {
            vmValues.put(Integer.toString(id), c);
            return true;
        });
        o.put("vms", vmValues);

        final JSONObject nodeValues = new JSONObject();
        rc.forEachNodeId((id, c) -> {
            nodeValues.put(Integer.toString(id), c);
            return true;
        });
        o.put(NODES_LABEL, nodeValues);

        return o;
    }

    @Override
    public ShareableResource fromJSON(Model mo, JSONObject o) throws JSONConverterException {
        checkKeys(o, "vms", NODES_LABEL, DEFAULT_CAPACITY, DEFAULT_CONSUMPTION, ModelViewConverter.IDENTIFIER);

        String id = requiredString(o, ModelViewConverter.IDENTIFIER);
        if (!id.equals(getJSONId())) {
            return null;
        }

        String rcId = requiredString(o, "rcId");
        int defConsumption = requiredInt(o, DEFAULT_CONSUMPTION);
        int defCapacity = requiredInt(o, DEFAULT_CAPACITY);

        ShareableResource rc = new ShareableResource(rcId, defCapacity, defConsumption);

        parseVMs(mo, rc, o.get("vms"));
        parseNodes(mo, rc, o.get(NODES_LABEL));

        return rc;
    }

    private static void parseVMs(Model mo, ShareableResource rc, Object o) throws JSONConverterException {
        if (o != null) {
            try {
                JSONObject values = (JSONObject) o;
                for (Map.Entry<String, Object> e : values.entrySet()) {
                    String k = e.getKey();
                    VM u = getVM(mo, Integer.parseInt(k));
                    int v = Integer.parseInt(e.getValue().toString());
                    rc.setConsumption(u, v);
                }
            } catch (ClassCastException cc) {
                throw new JSONConverterException("Unable to read the VMs at key 'vms'. Expect a JSONObject but got a '" + o.getClass().getName() + "'", cc);
            }
        }
    }

    private static void parseNodes(Model mo, ShareableResource rc, Object o) throws JSONConverterException {
        if (o != null) {
            try {
                JSONObject values = (JSONObject) o;
                for (Map.Entry<String, Object> e : values.entrySet()) {
                    String k = e.getKey();
                    Node u = getNode(mo, Integer.parseInt(k));
                    int v = Integer.parseInt(e.getValue().toString());
                    rc.setCapacity(u, v);
                }
            } catch (ClassCastException cc) {
                throw new JSONConverterException("Unable to read the nodes at key '" + NODES_LABEL + "'. Expect a JSONObject but got a '" + o.getClass().getName() + "'", cc);
            }
        }
    }
}

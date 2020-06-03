/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.json.model.view;

import net.minidev.json.JSONObject;
import org.btrplace.json.JSONConverterException;
import org.btrplace.model.Element;
import org.btrplace.model.Model;
import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.model.view.NamingService;

import java.util.Map;

import static org.btrplace.json.JSONs.checkKeys;
import static org.btrplace.json.JSONs.getNode;
import static org.btrplace.json.JSONs.getVM;
import static org.btrplace.json.JSONs.requiredString;

/**
 * Serialize/Un-serialize an {@link org.btrplace.model.view.NamingService}.
 *
 * @author Fabien Hermenier
 */
public class NamingServiceConverter implements ModelViewConverter<NamingService> {

    @Override
    public Class<NamingService> getSupportedView() {
        return NamingService.class;
    }

    @Override
    public String getJSONId() {
        return "ns";
    }

    @Override
    public JSONObject toJSON(NamingService rc) {
        JSONObject container = new JSONObject();
        container.put(ModelViewConverter.IDENTIFIER, getJSONId());
        container.put("type", rc.getElementIdentifier());
        JSONObject map = new JSONObject();
        for (Object o : rc.getNamedElements()) {
            Element e = (Element) o;
            map.put(rc.resolve(e), e.id());
        }
        container.put("map", map);
        return container;
    }

    @Override
    public NamingService<? extends Element> fromJSON(Model mo, JSONObject o) throws JSONConverterException {
        String id = requiredString(o, ModelViewConverter.IDENTIFIER);
        if (!id.equals(getJSONId())) {
            return null;
        }

        NamingService ns;
        String type = requiredString(o, "type");
        switch (type) {
            case VM.TYPE:
                ns = NamingService.newVMNS();
                break;
            case Node.TYPE:
                ns = NamingService.newNodeNS();
                break;
            default:
                throw new JSONConverterException("Unsupported type of element '" + type + "'");
        }

        checkKeys(o, "map");
        JSONObject map = (JSONObject) o.get("map");
        for (Map.Entry<String, Object> e : map.entrySet()) {
            String n = e.getKey();
            int v = Integer.parseInt(e.getValue().toString());
            Element el = VM.TYPE.equals(type) ? getVM(mo, v) : getNode(mo, v);
            if (!ns.register(el, n)) {
                throw new JSONConverterException("Duplicated name '" + n + "'");
            }
        }
        return ns;
    }
}

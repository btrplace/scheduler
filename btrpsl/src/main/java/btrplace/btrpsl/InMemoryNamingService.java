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

package btrplace.btrpsl;

import btrplace.btrpsl.element.BtrpElement;
import btrplace.btrpsl.element.BtrpOperand;
import btrplace.model.Element;
import btrplace.model.Node;
import btrplace.model.VM;

import java.util.*;

/**
 * Basic non-persistent implementation of a {@link NamingService}.
 *
 * @author Fabien Hermenier
 */
public class InMemoryNamingService implements NamingService {

    private Map<String, BtrpElement> resolve;

    private Map<Element, String> rev;

    /**
     * Make a new service.
     */
    public InMemoryNamingService() {
        resolve = new HashMap<>();
        rev = new HashMap<>();
    }

    @Override
    public String getIdentifier() {
        return NamingService.ID;
    }

    @Override
    public BtrpElement register(String id, Element e) throws NamingServiceException {
        if (resolve.containsKey(id)) {
            throw new NamingServiceException(id, " Name already registered");
        }

        BtrpElement be;
        //Naming consistency
        if (e instanceof Node) {
            if (!id.startsWith("@")) {
                throw new NamingServiceException(id, "Node labels must start with a '@'");
            }
            be = new BtrpElement(BtrpOperand.Type.node, id, e);
        } else if (e instanceof VM) {
            be = new BtrpElement(BtrpOperand.Type.VM, id, e);
        } else {
            throw new NamingServiceException(id, "Unsupported type of element " + e.getClass().getSimpleName());
        }
        resolve.put(id, be);
        rev.put(e, id);
        return be;
    }

    @Override
    public String resolve(Element el) {
        return rev.get(el);
    }

    @Override
    public BtrpElement resolve(String n) {
        return resolve.get(n);
    }

    @Override
    public InMemoryNamingService clone() {
        InMemoryNamingService cpy = new InMemoryNamingService();
        for (Map.Entry<String, BtrpElement> e : resolve.entrySet()) {
            cpy.resolve.put(e.getKey(), e.getValue());
        }
        for (Map.Entry<Element, String> e : rev.entrySet()) {
            cpy.rev.put(e.getKey(), e.getValue());
        }
        return cpy;
    }

    @Override
    public boolean substituteVM(VM curId, VM nextId) {
        String fqn = rev.get(curId);
        if (fqn != null) {
            rev.put(nextId, fqn);
            resolve.put(fqn, new BtrpElement(BtrpOperand.Type.VM, fqn, nextId));
        }
        return true;
    }


    @Override
    public Set<Element> getRegisteredElements() {
        return rev.keySet();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NamingService)) {
            return false;
        }

        NamingService that = (NamingService) o;
        if (!getRegisteredElements().equals(that.getRegisteredElements())) {
            return false;
        }
        for (Element e : getRegisteredElements()) {
            String s = resolve(e);
            if (!s.equals(that.resolve(e))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(resolve, rev);
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        Iterator<Map.Entry<Element, String>> ite = rev.entrySet().iterator();
        Map.Entry<Element, String> e = ite.next();
        if (e != null) {
            b.append('<').append(e.getKey()).append(" : ").append(e.getValue()).append('>');
        }
        while (ite.hasNext()) {
            e = ite.next();
            b.append(", <").append(e.getKey()).append(" : ").append(e.getValue()).append('>');
        }
        return b.toString();
    }
}

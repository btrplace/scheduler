/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Default implementation for {@link Attributes}.
 *
 * @author Fabien Hermenier
 */
public class DefaultAttributes implements Attributes {

    private final Map<VM, Map<String, Object>> vmAttrs;
    private final Map<Node, Map<String, Object>> nodeAttrs;

    /**
     * Make a new empty list of attributes.
     */
    public DefaultAttributes() {
        vmAttrs = new HashMap<>();
        nodeAttrs = new HashMap<>();
    }

    private boolean putObject(Element e, String k, Object v) {
        Map<String, Object> m;
        if (e instanceof VM) {
            m = vmAttrs.get(e);
            if (m == null) {
                m = new HashMap<>();
                vmAttrs.put((VM) e, m);
            }
        } else if (e instanceof Node) {
            m = nodeAttrs.get(e);
            if (m == null) {
                m = new HashMap<>();
                nodeAttrs.put((Node) e, m);
            }
        } else {
            return false;
        }
        return m.put(k, v) != null;
    }

    @Override
    public Object get(Element e, String k) {
        Map<String, Object> m;
        if (e instanceof Node) {
            m = nodeAttrs.get(e);
        } else if (e instanceof VM) {
            m = vmAttrs.get(e);
        } else {
            return null;
        }
        return m == null ? null : m.get(k);
    }

    @Override
    public int get(Element e, String k, int def) {
        if (isSet(e, k)) {
            return (int) get(e, k);
        }
        return def;
    }

    @Override
    @SuppressWarnings("squid:S1166")
    public double get(Element e, String k, double def) {
        if (isSet(e, k)) {
            try {
                return (int) get(e, k);
            } catch (@SuppressWarnings("unused") ClassCastException ex) {
                //Not an integer
            }
            //Try the double
            return (double) get(e, k);
        }
        return def;

    }

    @Override
    public String get(Element e, String k, String def) {
        if (isSet(e, k)) {
            return (String) get(e, k);
        }
        return def;

    }

    @Override
    public boolean get(Element e, String k, boolean def) {
        if (isSet(e, k)) {
            return (Boolean) get(e, k);
        }
        return def;

    }

    @Override
    public boolean isSet(Element e, String k) {
        Map<String, Object> m;
        if (e instanceof Node) {
            m = nodeAttrs.get(e);
        } else if (e instanceof VM) {
            m = vmAttrs.get(e);
        } else {
            return false;
        }
        return m != null && m.containsKey(k);
    }

    @Override
    public boolean unset(Element e, String k) {
        Map<String, Object> m;
        if (e instanceof Node) {
            m = nodeAttrs.get(e);
        } else if (e instanceof VM) {
            m = vmAttrs.get(e);
        } else {
            return false;
        }
        return m != null && m.remove(k) != null;
    }

    @Override
    public Attributes copy() {
        DefaultAttributes cpy = new DefaultAttributes();
        for (Map.Entry<VM, Map<String, Object>> e : vmAttrs.entrySet()) {
            cpy.vmAttrs.put(e.getKey(), new HashMap<>(e.getValue()));
        }

        for (Map.Entry<Node, Map<String, Object>> e : nodeAttrs.entrySet()) {
            cpy.nodeAttrs.put(e.getKey(), new HashMap<>(e.getValue()));
        }

        return cpy;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        for (Map.Entry<VM, Map<String, Object>> e : vmAttrs.entrySet()) {
            b.append(String.format("%s:%s%n", e.getKey(), stringify(e.getValue())));
        }
        for (Map.Entry<Node, Map<String, Object>> e : nodeAttrs.entrySet()) {
            b.append(String.format("%s:%s%n", e.getKey(), stringify(e.getValue())));
        }
        return b.toString();
    }

    private static String stringify(Map<String, Object> map) {
        StringBuilder b = new StringBuilder();
        for (Map.Entry<String, Object> attr : map.entrySet()) {
            b.append(" <").append(attr.getKey()).append(',');
            Object val = attr.getValue();
            if (val instanceof String) {
                b.append('"').append(val).append('"');
            } else {
                b.append(val);
            }
            b.append('>');
        }
        return b.toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(vmAttrs, nodeAttrs);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o == this) {
            return true;
        }
        if (!o.getClass().equals(getClass())) {
            return false;
        }
        DefaultAttributes that = (DefaultAttributes) o;
        return vmAttrs.equals(that.vmAttrs) && nodeAttrs.equals(that.nodeAttrs);
    }

    @Override
    public Set<Element> getDefined() {
        Set<Element> s = new HashSet<>(vmAttrs.size() + nodeAttrs.size());
        s.addAll(vmAttrs.keySet());
        s.addAll(nodeAttrs.keySet());
        return s;
    }

    @Override
    public void clear() {
        this.vmAttrs.clear();
        this.nodeAttrs.clear();
    }

    @Override
    public boolean put(Element e, String k, boolean b) {
        return putObject(e, k, b);
    }

    @Override
    public boolean put(Element e, String k, int n) {
        return putObject(e, k, n);
    }

    @Override
    public boolean put(Element e, String k, String s) {
        return putObject(e, k, s);
    }

    @Override
    public boolean put(Element e, String k, double d) {
        return putObject(e, k, d);
    }

    @Override
    public Set<String> getKeys(Element e) {
        Map<String, Object> m;
        if (e instanceof Node) {
            m = nodeAttrs.get(e);
        } else if (e instanceof VM) {
            m = vmAttrs.get(e);
        } else {
            return Collections.emptySet();
        }
        return m == null ? Collections.emptySet() : m.keySet();
    }

    @Override
    @SuppressWarnings("squid:S1166")
    public boolean castAndPut(Element e, String k, String v) {
        String x = v.toLowerCase().trim();
        if ("true".equals(x)) {
            return put(e, k, true);
        } else if ("false".equals(x)) {
            return put(e, k, false);
        }
        try {
            return put(e, k, Integer.parseInt(x));
        } catch (@SuppressWarnings("unused") NumberFormatException ignored) {
            //Not an int
        }

        try {
            return put(e, k, Double.parseDouble(x));
        } catch (@SuppressWarnings("unused") NumberFormatException ignored) {
            //not a double either
        }

        return put(e, k, v);
    }

    @Override
    public void clear(Element e) {
        if (e instanceof VM) {
            this.vmAttrs.remove(e);
        } else if (e instanceof Node) {
            this.nodeAttrs.remove(e);
        }
    }
}

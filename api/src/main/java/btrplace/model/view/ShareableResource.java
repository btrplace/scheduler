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

package btrplace.model.view;

import java.util.*;

/**
 * An interface to denote a resource that nodes share among the VMs they host
 * <p/>
 * The interface allows to specify the physical resource capacity of the nodes
 * and the amount of virtual resources to allocate to the VMs.
 * By default, one unit of virtual resource corresponds to one unit of physical resource.
 * It is however possible to create a overbooking factor using
 * {@link btrplace.model.constraint.Overbook} constraints.
 *
 * @author Fabien Hermenier
 */
public class ShareableResource implements ModelView, Cloneable, Comparator<Integer> {

    /**
     * The base of the view identifier. Once instantiated, it is completed
     * by the resource identifier.
     */
    public static final String VIEW_ID_BASE = "ShareableResource.";

    private Map<Integer, Integer> values;

    private int noValue;

    private String viewId;

    private String rcId;

    public static final int DEFAULT_NO_VALUE = 0;

    /**
     * Make a new resource that use {@link #DEFAULT_NO_VALUE} as value to denote an undefined value.
     *
     * @param id the resource identifier
     */
    public ShareableResource(String id) {
        this(id, DEFAULT_NO_VALUE);
    }

    /**
     * Make a new resource.
     *
     * @param id      the resource identifier
     * @param noValue the value to use to denote an undefined value
     */
    public ShareableResource(String id, int noValue) {
        values = new HashMap<>();
        this.rcId = id;
        this.viewId = new StringBuilder(VIEW_ID_BASE).append(rcId).toString();
        this.noValue = noValue;
    }

    /**
     * Get the resource value associated to an element.
     * The resource must be defined for the element.
     *
     * @param n the element
     * @return the resource if it was defined.
     */
    public int get(int n) {
        if (values.containsKey(n)) {
            return values.get(n);
        }
        return noValue;
    }

    /**
     * Get the resource associated to a list of element.
     * The ordering is maintained
     *
     * @param ids the element identifiers
     * @return a list of values.
     */
    public List<Integer> get(List<Integer> ids) {
        List<Integer> res = new ArrayList<>(ids.size());
        for (int u : ids) {
            res.add(get(u));
        }
        return res;
    }

    /**
     * Get the identifiers that are defined.
     *
     * @return a set that may be empty.
     */
    public Set<Integer> getDefined() {
        return values.keySet();
    }

    /**
     * Define a value for an element.
     * If the element is a VM, the value denotes its allocation of virtual resources
     * If the element is a node, the value denotes its physical capacity
     *
     * @param n   the element identifier
     * @param val the value to set
     * @return the current resource
     */
    public ShareableResource set(int n, int val) {
        values.put(n, val);
        return this;
    }

    /**
     * Un-define a resource for a given element.
     *
     * @param n the element identifier
     * @return {@code true} iff a value was previously defined for {@code n}.
     */
    public boolean unset(int n) {
        return values.remove(n) != null;
    }

    /**
     * Check if the resource is defined for an element.
     *
     * @param n the element to check
     * @return {@code true} iff the resource is defined for {@code n}.
     */
    public boolean defined(int n) {
        return values.containsKey(n);
    }

    @Override
    public int compare(Integer o1, Integer o2) {
        return get(o1) - get(o2);
    }

    /**
     * Get the view identifier.
     *
     * @return "ShareableResource.rcId" where rcId is the resource identifier provided to the constructor
     */
    @Override
    public String getIdentifier() {
        return viewId;
    }

    /**
     * Get the resource identifier
     *
     * @return a non-empty string
     */
    public String getResourceIdentifier() {
        return rcId;
    }

    /**
     * Get the maximum resource value that is assigned to the given elements
     *
     * @param ids   the element to browse
     * @param undef {@code true} to include the undefined elements using the default value
     * @return the value
     */
    public int max(Collection<Integer> ids, boolean undef) {
        int m = Integer.MIN_VALUE;
        for (int u : ids) {
            if (defined(u) || undef) {
                int x = defined(u) ? values.get(u) : noValue;
                if (x > m) {
                    m = x;
                }
            }
        }
        return m;
    }

    /**
     * Get the minimal resource value that is assigned to the given elements
     *
     * @param ids   the element to browse
     * @param undef {@code true} to include the undefined elements using the default value
     * @return the value
     */
    public int min(Collection<Integer> ids, boolean undef) {
        int m = Integer.MAX_VALUE;
        for (int u : ids) {
            if (defined(u) || undef) {
                int x = defined(u) ? values.get(u) : noValue;
                if (x < m) {
                    m = x;
                }
            }
        }
        return m;
    }

    /**
     * Sum the values that are assigned to the given elements
     *
     * @param ids   the element to brows
     * @param undef {@code true} to include the undefined elements using the default value
     * @return the value
     */
    public int sum(Collection<Integer> ids, boolean undef) {
        int s = 0;
        for (int u : ids) {
            if (defined(u) || undef) {
                s += defined(u) ? values.get(u) : noValue;
            }
        }
        return s;
    }

    /**
     * Get the value that is used to denote an undefined value.
     *
     * @return the value.
     */
    public int getDefaultValue() {
        return noValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ShareableResource that = (ShareableResource) o;

        if (!that.getDefined().equals(values.keySet())) {
            return false;
        }

        for (int k : values.keySet()) {
            if (!values.get(k).equals(that.get(k))) {
                return false;
            }
        }
        return rcId.equals(that.getResourceIdentifier());
    }

    @Override
    public int hashCode() {
        return Objects.hash(rcId, values);
    }

    @Override
    public ShareableResource clone() {
        ShareableResource rc = new ShareableResource(rcId, noValue);
        for (Map.Entry<Integer, Integer> e : values.entrySet()) {
            rc.values.put(e.getKey(), e.getValue());
        }
        return rc;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder("rc:").append(rcId).append(":");
        for (Iterator<Map.Entry<Integer, Integer>> ite = values.entrySet().iterator(); ite.hasNext(); ) {
            Map.Entry<Integer, Integer> e = ite.next();
            buf.append('<').append(e.getKey().toString()).append(',').append(e.getValue()).append('>');
            if (ite.hasNext()) {
                buf.append(',');
            }
        }
        return buf.toString();
    }

    @Override
    public boolean substitute(int oldint, int newint) {
        set(newint, get(oldint));
        return true;
    }
}

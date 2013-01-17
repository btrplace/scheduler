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

import java.util.*;

/**
 * An interface to denote a resource that a node shares among the VMs it hosts
 * <p/>
 * The interface allows to specify the physical resource capacity of the nodes
 * but also the amount of virtual resources to allocate to the VMs.
 * By default, nodes capacity and VMs usages are not directly linked. It is
 * then a necessary to express a mapping between the physical resources and
 * the virtual resources. This can be made through a {@link btrplace.model.constraint.Overbook}
 * constraint to indicate the virtual capacity of a node from its physical
 * capacity.
 *
 * @author Fabien Hermenier
 */
public interface ShareableResource extends Comparator<UUID>, Cloneable {

    /**
     * The resource identifier.
     *
     * @return a String denoting the identifier resource
     */
    String getIdentifier();

    /**
     * Check if the resource is defined for an element.
     *
     * @param n the element to check
     * @return {@code true} iff the resource is defined for {@code n}.
     */
    boolean defined(UUID n);

    /**
     * Undefine a resource for a given element.
     *
     * @param n the element identifier
     * @return {@code true} iff a value was previously defined for {@code n}.
     */
    boolean unset(UUID n);

    /**
     * Define a value for an element.
     * If the element is a VM, the value denotes its resource usage.
     * If the element is a node, the value denotes its capacity
     *
     * @param n the element identifier
     * @param o the value to set
     * @return the current resource
     */
    ShareableResource set(UUID n, int o);

    /**
     * get the resource associated to an element.
     * The resource must be defined for the element.
     *
     * @param n the element
     * @return the resource if it was defined.
     */
    int get(UUID n);

    /**
     * Get the resource associated to a list of element.
     * Order is maintained
     *
     * @param ids the element identifiers
     * @return a list of values.
     */
    List<Integer> get(List<UUID> ids);

    /**
     * Get the elements identifiers that are defined.
     *
     * @return a set that may be empty.
     */
    Set<UUID> getDefined();

    @Override
    int compare(UUID o1, UUID o2);

    /**
     * Get the maximum resource value that is assigned to the given elements
     *
     * @param ids   the element to browse
     * @param undef {@code true} to include the undefined elements using the default value
     * @return the value
     */
    int max(Collection<UUID> ids, boolean undef);

    /**
     * Get the minimal resource value that is assigned to the given elements
     *
     * @param ids   the element to browse
     * @param undef {@code true} to include the undefined elements using the default value
     * @return the value
     */
    int min(Collection<UUID> ids, boolean undef);

    /**
     * Sum the values that are assigned to the given elements
     *
     * @param ids   the element to brows
     * @param undef {@code true} to include the undefined elements using the default value
     * @return the value
     */
    int sum(Collection<UUID> ids, boolean undef);

    /**
     * Get the value that is used to denote an undefined value.
     *
     * @return the value.
     */
    int getDefaultValue();

    /**
     * Copy the resource.
     *
     * @return a new resource object
     */
    ShareableResource clone();
}

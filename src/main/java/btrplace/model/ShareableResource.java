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
 * An interface to denote a resource that nodes share among the VMs they host
 * <p/>
 * The interface allows to specify the physical resource capacity of the nodes
 * and the amount of virtual resources to allocate to the VMs.
 * By default, their is no mapping between the physical and the virtual resources.
 * To limit the hosting capacity of the nodes it is then a necessary to express this mapping,
 * as an exemple, using {@link btrplace.model.constraint.Overbook} constraints.
 *
 * @author Fabien Hermenier
 */
public interface ShareableResource extends Comparator<UUID>, ModelView {

    /**
     * The base of the view identifier. Once instantiated, it is completed
     * by the resource identifier.
     */
    static final String VIEW_ID_BASE = ShareableResource.class.getSimpleName() + '.';

    /**
     * Check if the resource is defined for an element.
     *
     * @param n the element to check
     * @return {@code true} iff the resource is defined for {@code n}.
     */
    boolean defined(UUID n);

    /**
     * Un-define a resource for a given element.
     *
     * @param n the element identifier
     * @return {@code true} iff a value was previously defined for {@code n}.
     */
    boolean unset(UUID n);

    /**
     * Define a value for an element.
     * If the element is a VM, the value denotes its allocation of virtual resources
     * If the element is a node, the value denotes its physical capacity
     *
     * @param n the element identifier
     * @param o the value to set
     * @return the current resource
     */
    ShareableResource set(UUID n, int o);

    /**
     * Get the resource value associated to an element.
     * The resource must be defined for the element.
     *
     * @param n the element
     * @return the resource if it was defined.
     */
    int get(UUID n);

    /**
     * Get the resource associated to a list of element.
     * The ordering is maintained
     *
     * @param ids the element identifiers
     * @return a list of values.
     */
    List<Integer> get(List<UUID> ids);

    /**
     * Get the identifiers that are defined.
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

    /**
     * Get the resource identifier.
     *
     * @return a non-empty string
     */
    String getResourceIdentifier();
}

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

import java.util.Collection;

/**
 * A model depicts a consistent snapshot of an infrastructure.
 * Basically, a model is composed by a {@link Mapping} to indicate the state
 * and the location of the elements, a variety of {@link ShareableResource} object
 * to indicate the resource usage or capacity of the elements and finally,
 * a set of {@link SatConstraint} that indicate the constraint that should be satisfied.
 *
 * @author Fabien Hermenier
 */
public interface Model extends Cloneable {

    /**
     * Get a particular resource attached to this model.
     *
     * @param id the resource identifier
     * @return the resource if it was attached, {@code null} otherwise
     */
    ShareableResource getResource(String id);

    /**
     * Get the resources attached to the model.
     *
     * @return a collection of resources that may be empty.
     */
    Collection<ShareableResource> getResources();

    /**
     * Attach a resource to this model.
     * No resources with the same identifier must have been attached earlier.
     *
     * @param rc the resource to attach
     * @return {@code true} iff the resource has been attached
     */
    boolean attach(ShareableResource rc);

    /**
     * Detach a resource from this model.
     *
     * @param rc the resource
     * @return {@code true} iff the resource was removed
     */
    boolean detach(ShareableResource rc);

    /**
     * Detach all the resources from this model.
     */
    void clearResources();

    /**
     * Get the mapping associated to this model.
     *
     * @return the mapping
     */
    Mapping getMapping();

    /**
     * Get the attributes of the elements in the model.
     *
     * @return the attributes
     */
    Attributes getAttributes();

    /**
     * Set the attributes for the elements in the model.
     *
     * @param attrs the attributes to set
     */
    void setAttributes(Attributes attrs);

    /**
     * Clone a model.
     *
     * @return a new model
     */
    Model clone();
}

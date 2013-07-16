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

package btrplace.model;

import btrplace.model.view.ModelView;

import java.util.Collection;
import java.util.Set;

/**
 * A model depicts a consistent snapshot of an infrastructure.
 * Basically, a model is composed by a {@link Mapping} to indicate the state
 * and the location of the elements, and a variety of {@link ModelView} to
 * provide additional data about the elements.
 * <p/>
 * In addition, it is possible to declare attributes for specific elements.
 *
 * @author Fabien Hermenier
 */
public interface Model extends Cloneable {

    /**
     * Get a view already attached to the model
     *
     * @param id the view identifier
     * @return the view if it was attached, {@code null} otherwise
     */
    ModelView getView(String id);

    /**
     * Get all the view attached to the model.
     *
     * @return a collection of views that may be empty.
     */
    Collection<ModelView> getViews();

    /**
     * Attach a view to the model.
     * No view with the same identifier must have been attached earlier.
     *
     * @param v the view to attach
     * @return {@code true} iff the view has been attached
     */
    boolean attach(ModelView v);

    /**
     * Detach a view from this model.
     *
     * @param v the view
     * @return {@code true} iff the view was removed
     */
    boolean detach(ModelView v);

    /**
     * Detach all the views from this model.
     */
    void clearViews();

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

    /**
     * Generate a new VM for this model.
     * The VM will not be included in the mapping associated to the model.
     *
     * @return {@code null} if no identifiers are available for the VM.
     */
    VM newVM();

    /**
     * Generate a new VM for this model.
     * The VM will not be included in the mapping associated to the model.
     *
     * @param id the identifier to use for that VM
     * @return a VM or {@code null} if the identifier is already used
     */
    VM newVM(int id);

    /**
     * Generate a new Node for this model.
     * The node will not be included in the mapping associated to the model.
     *
     * @return {@code null} if no identifiers are available for the Node.
     */
    Node newNode();

    /**
     * Generate a new node for this model.
     * The node will not be included in the mapping associated to the model.
     *
     * @param id the identifier to use for that node
     * @return a Node or {@code null} if the identifier is already used
     */
    Node newNode(int id);

    /**
     * Get all the registered nodes.
     *
     * @return a set of nodes, may be empty
     */
    Set<Node> getNodes();

    /**
     * Get all the registered VMs.
     *
     * @return a set of VMs, may be empty
     */
    Set<VM> getVMs();
}

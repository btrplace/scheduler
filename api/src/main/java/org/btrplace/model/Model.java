/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.model;

import org.btrplace.model.view.ModelView;

import java.util.Collection;

/**
 * A model depicts a consistent snapshot of an infrastructure.
 * Basically, a model is composed by a {@link Mapping} to indicate the state
 * and the location of the elements, and a variety of {@link ModelView} to
 * provide additional data about the elements.
 * <p>
 * In addition, it is possible to declare attributes for specific elements.
 *
 * @author Fabien Hermenier
 */
public interface Model extends ElementBuilder {

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
     * {@inheritDoc}
     *
     * @return a model copy
     */
    @Override
    Model copy();
}

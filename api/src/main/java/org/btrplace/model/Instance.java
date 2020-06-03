/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.model;

import org.btrplace.model.constraint.OptConstraint;
import org.btrplace.model.constraint.SatConstraint;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * An instance aggregates a model and a list of constraints.
 *
 * @author Fabien Hermenier
 */
public class Instance {

  private final Model mo;

  private final List<SatConstraint> cstrs;

  private final OptConstraint opt;

    /**
     * Make a new instance.
     *
     * @param m  the model to use
     * @param cs the list of satisfaction oriented constraints to consider
     * @param o  the optimization constraint
     */
    public Instance(Model m, Collection<? extends SatConstraint> cs, OptConstraint o) {
        cstrs = new ArrayList<>(cs);
        this.mo = m;
        this.opt = o;
    }

    /**
     * Make a new instance.
     *
     * @param m the model to use
     * @param o the optimization constraint
     */
    public Instance(Model m, OptConstraint o) {
        this(m, Collections.emptyList(), o);
    }

    /**
     * Get the model.
     *
     * @return a model
     */
    public Model getModel() {
        return mo;
    }

    /**
     * Get the declared constraints.
     *
     * @return a collection of satisfaction oriented constraints that may be empty
     */
    public Collection<SatConstraint> getSatConstraints() {
        return cstrs;
    }

    /**
     * Get the declared optimization constraint.
     *
     * @return the optimization constraint to consider (not null)
     */
    public OptConstraint getOptConstraint() {
        return opt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Instance instance = (Instance) o;

        return cstrs.equals(instance.cstrs) && mo.equals(instance.mo) && opt.equals(instance.opt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mo, cstrs);
    }
}

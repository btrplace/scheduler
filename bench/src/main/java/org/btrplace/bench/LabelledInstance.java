/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.bench;

import org.btrplace.model.Instance;

import java.util.Objects;

/**
 * An instance with a label.
 * @author Fabien Hermenier
 */
public class LabelledInstance extends Instance {

    /**
     * The instance label.
     */
    public final String label;

    /**
     * Make a new instance.
     *
     * @param label the instance label
     * @param i     the underlying instance
     */
    public LabelledInstance(String label, Instance i) {
        super(i.getModel(), i.getSatConstraints(), i.getOptConstraint());
        this.label = label;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        LabelledInstance that = (LabelledInstance) o;
        return Objects.equals(label, that.label);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), label);
    }
}

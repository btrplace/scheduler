/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.btrpsl.constraint;

import java.util.Set;

/**
 * A catalog that contains several constraints builder, associated by their name.
 *
 * @author Fabien Hermenier
 */
public interface ConstraintsCatalog {
    /**
     * Get all the available constraints.
     *
     * @return a set of constraint identifier. May be empty
     */
    Set<String> getAvailableConstraints();

    /**
     * Get a placement constraints builder from its identifier.
     *
     * @param id the identifier of the constraint
     * @return the constraints builder or {@code null} if the identifier is unknown
     */
    SatConstraintBuilder getConstraint(String id);
}

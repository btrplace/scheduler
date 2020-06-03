/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.model.constraint;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to provide the specification of a side-constraint.
 * The annotation should be on a class extending {@link org.btrplace.model.constraint.SatConstraint}.
 *
 * @author Fabien Hermenier
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
public @interface SideConstraint {

    /**
     * @return the constraint parameters.
     */
    String[] args();

    /**
     * @return the constraint invariant.
     */
    String inv();
}

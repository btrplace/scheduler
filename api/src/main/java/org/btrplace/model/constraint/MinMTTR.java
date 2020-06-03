/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.model.constraint;

/**
 * An optimization constraint that minimizes the time to repair a non-viable model.
 * In practice it minimizes the sum of the ending moment for each actions.
 *
 * @author Fabien Hermenier
 */
public class MinMTTR extends OptConstraint {

    @Override
    public String id() {
        return "minimizeMTTR";
    }
}

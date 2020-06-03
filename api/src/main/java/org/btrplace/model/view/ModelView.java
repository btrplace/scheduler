/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.model.view;

import org.btrplace.Copyable;
import org.btrplace.model.VM;

/**
 * A view provides some domain-specific information about the elements of a model.
 * A view must have a unique identifier
 *
 * @author Fabien Hermenier
 */
public interface ModelView extends Copyable<ModelView> {

    /**
     * Get the view identifier.
     *
     * @return a non-empty String
     */
    String getIdentifier();

    /**
     * Notify the view a VM that already exist
     * will be substituted by another VM during the reconfiguration process.
     *
     * @param curId  the current VM identifier
     * @param nextId the new VM identifier
     * @return {@code true} iff the operation succeeded
     */
    boolean substituteVM(VM curId, VM nextId);
}

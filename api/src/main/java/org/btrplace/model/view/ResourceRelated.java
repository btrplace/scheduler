/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.model.view;

/**
 * Specify an element related to a resource.
 *
 * @author Fabien Hermenier
 */
@FunctionalInterface
public interface ResourceRelated {

    /**
     * Get the resource identifier.
     *
     * @return the identifier
     */
    String getResource();
}

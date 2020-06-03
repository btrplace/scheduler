/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.examples;

/**
 * An interface to define a runnable example.
 *
 * @author Fabien Hermenier
 */
@FunctionalInterface
@SuppressWarnings("squid:S106")
public interface Example {

    /**
     * Run the example.
     *
     */
    void run();
}

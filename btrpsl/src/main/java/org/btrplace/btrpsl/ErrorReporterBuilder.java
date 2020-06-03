/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.btrpsl;

/**
 * The builder used to instantiate a custom {@link ErrorReporter}.
 *
 * @author Fabien Hermenier
 */
@FunctionalInterface
public interface ErrorReporterBuilder {

    /**
     * Make a new reporter.
     *
     * @param v the script under construction associated to this builder
     * @return the built reporter
     */
    ErrorReporter build(Script v);
}

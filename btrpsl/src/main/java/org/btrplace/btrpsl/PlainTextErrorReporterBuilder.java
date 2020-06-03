/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.btrpsl;

/**
 * The builder associated to {@link PlainTextErrorReporter}.
 *
 * @author Fabien Hermenier
 */
public class PlainTextErrorReporterBuilder implements ErrorReporterBuilder {

    @Override
    public ErrorReporter build(Script v) {
        return new PlainTextErrorReporter(v);
    }
}

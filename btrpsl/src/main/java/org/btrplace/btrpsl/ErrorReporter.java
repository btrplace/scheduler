/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.btrpsl;

import java.util.List;

/**
 * Interface used to specify a object that collect errors.
 *
 * @author Fabien Hermenier
 */
public interface ErrorReporter {

    /**
     * Report an error.
     *
     * @param lineNo the line index
     * @param colNo  the column index
     * @param msg    the error message
     */
    void append(int lineNo, int colNo, String msg);

    /**
     * Get the reported errors.
     *
     * @return a list of error messages
     */
    List<ErrorMessage> getErrors();

    /**
     * Propagate the script namespace.
     */
    void updateNamespace();
}

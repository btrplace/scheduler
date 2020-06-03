/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.btrpsl.tree;

import org.antlr.runtime.Token;
import org.btrplace.btrpsl.ErrorReporter;
import org.btrplace.btrpsl.element.BtrpOperand;
import org.btrplace.btrpsl.element.BtrpString;

/**
 * A tree that is only a root to parse a String.
 *
 * @author Fabien Hermenier
 */
public class StringTree extends BtrPlaceTree {


    /**
     * Make a new tree
     *
     * @param payload the token containing the string
     * @param errors  to report error
     */
    public StringTree(Token payload, ErrorReporter errors) {
        super(payload, errors);
    }

    @Override
    public BtrpOperand go(BtrPlaceTree parent) {
        //Remove the \" \"
        return new BtrpString(getText().substring(1, getText().length() - 1));
    }
}

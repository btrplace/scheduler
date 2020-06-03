/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.btrpsl.tree;

import org.antlr.runtime.Token;
import org.btrplace.btrpsl.element.BtrpOperand;
import org.btrplace.btrpsl.element.IgnorableOperand;

/**
 * @author Fabien Hermenier
 */
public class DiscreteToken extends BtrPlaceTree {

    /**
     * Make a new token.
     *
     * @param t the ANTLR token to use
     */
    public DiscreteToken(Token t) {
        super(t, null);
    }

    @Override
    public BtrpOperand go(BtrPlaceTree parent) {
        return IgnorableOperand.getInstance();
    }
}

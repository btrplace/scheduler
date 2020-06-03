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
 * Tree to handle errors returned by the lexer.
 *
 * @author Fabien Hermenier
 */
public class ErrorTree extends BtrPlaceTree {

  private final Token end;

    /**
     * A tree signaling an error.
     * @param start the first token signaling the error
     * @param stop the last token involved in the error
     */
    public ErrorTree(Token start, Token stop) {
        super(start, null);
        end = stop;
    }

    @Override
    public int getLine() {
        return end.getLine();
    }

    @Override
    public int getCharPositionInLine() {
        return end.getCharPositionInLine();
    }

    @Override
    public BtrpOperand go(BtrPlaceTree parent) {
        return IgnorableOperand.getInstance();
    }
}

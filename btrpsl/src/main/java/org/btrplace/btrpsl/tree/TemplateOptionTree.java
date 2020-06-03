/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.btrpsl.tree;

import org.antlr.runtime.Token;
import org.btrplace.btrpsl.ErrorReporter;
import org.btrplace.btrpsl.antlr.ANTLRBtrplaceSL2Parser;
import org.btrplace.btrpsl.element.BtrpOperand;
import org.btrplace.btrpsl.element.IgnorableOperand;

/**
 * A tree to get an option identifier and, if exists, the value of the option.
 *
 * @author Fabien Hermenier
 */
public class TemplateOptionTree extends BtrPlaceTree {

    /**
     * The option identifier.
     */
    private String key;

    /**
     * The optional value.
     */
    private String value = null;

    /**
     * Make a new tree.
     *
     * @param t    the token to handle. The root of this tree
     * @param errs the errors to report
     */
    public TemplateOptionTree(Token t, ErrorReporter errs) {
        super(t, errs);
    }

    @Override
    public BtrpOperand go(BtrPlaceTree parent) {
        key = getChild(0).getText();

        if (getChildCount() == 2) {
            BtrPlaceTree t = getChild(1);
            String txt = t.getText();
            if (t.getType() == ANTLRBtrplaceSL2Parser.STRING) {
                txt = txt.substring(1, txt.length() - 1);
            }
            value = txt;

        }
        return IgnorableOperand.getInstance();
    }

    /**
     * Get the identifier of the option.
     *
     * @return a non empty string
     */
    public String getKey() {
        return this.key;
    }

    /**
     * Get the optional value attached to the option identifier.
     *
     * @return the value if it was specified. {@code null} otherwise
     */
    public String getValue() {
        return this.value;
    }
}

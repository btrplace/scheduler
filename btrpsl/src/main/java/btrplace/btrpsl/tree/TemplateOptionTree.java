/*
 * Copyright (c) 2013 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package btrplace.btrpsl.tree;

import btrplace.btrpsl.ANTLRBtrplaceSL2Parser;
import btrplace.btrpsl.ErrorReporter;
import btrplace.btrpsl.element.BtrpOperand;
import btrplace.btrpsl.element.IgnorableOperand;
import org.antlr.runtime.Token;

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

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

import btrplace.btrpsl.ErrorReporter;
import btrplace.btrpsl.element.BtrpOperand;
import org.antlr.runtime.Token;

/**
 * A parser to check if the left operand is > or < to the right operand
 * Return 1 if equals, 0 otherwise. Types must be the same
 *
 * @author Fabien Hermenier
 */
public class StrictComparisonOperator extends BtrPlaceTree {

    private boolean reverse = false;

    /**
     * Make a new parser.
     *
     * @param t    the root token
     * @param errs the errors to report
     */
    public StrictComparisonOperator(Token t, boolean rev, ErrorReporter errs) {
        super(t, errs);
        this.reverse = rev;
    }

    @Override
    public BtrpOperand go(BtrPlaceTree parent) {
        BtrpOperand l = getChild(0).go(this);
        BtrpOperand r = getChild(1).go(this);

        if (!reverse) {
            return l.gt(r);
        } else {
            return r.gt(l);
        }

    }

}

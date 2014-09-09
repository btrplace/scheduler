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
import btrplace.btrpsl.element.BtrpNumber;
import btrplace.btrpsl.element.BtrpOperand;
import btrplace.btrpsl.element.BtrpSet;
import org.antlr.runtime.Token;

/**
 * A parser get the cardinality of a set.
 *
 * @author Fabien Hermenier
 */
public class CardinalityOperator extends BtrPlaceTree {

    /**
     * Make a new parser.
     *
     * @param t    the root token
     * @param errs the errors to report
     */
    public CardinalityOperator(Token t, ErrorReporter errs) {
        super(t, errs);
    }

    @Override
    public BtrpOperand go(BtrPlaceTree parent) {
        BtrpOperand c = getChild(0).go(this);
        if (c.degree() == 0) {
            return ignoreError("Cardinality operator only applies to a set");
        }
        return new BtrpNumber(((BtrpSet) c).size(), BtrpNumber.Base.base10);
    }
}

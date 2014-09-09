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
import btrplace.btrpsl.element.IgnorableOperand;
import org.antlr.runtime.Token;

/**
 * @author Fabien Hermenier
 */
public class BlockStatement extends BtrPlaceTree {

    /**
     * Make a new parser.
     *
     * @param t    the root token
     * @param errs the errors to report
     */
    public BlockStatement(Token t, ErrorReporter errs) {
        super(t, errs);
    }

    @Override
    public BtrpOperand go(BtrPlaceTree parent) {

        for (int i = 0; i < this.getChildCount(); i++) {
            this.getChild(i).go(this);
        }
        return IgnorableOperand.getInstance();
    }
}

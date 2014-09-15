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
import btrplace.btrpsl.element.BtrpString;
import org.antlr.runtime.Token;

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

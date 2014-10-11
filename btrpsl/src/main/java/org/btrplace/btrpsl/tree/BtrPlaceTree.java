/*
 * Copyright (c) 2014 University Nice Sophia Antipolis
 *
 * This file is part of btrplace.
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.btrplace.btrpsl.tree;

import org.antlr.runtime.Token;
import org.antlr.runtime.tree.CommonTree;
import org.btrplace.btrpsl.ErrorReporter;
import org.btrplace.btrpsl.element.BtrpOperand;
import org.btrplace.btrpsl.element.IgnorableOperand;


/**
 * An abstract parser for a tree.
 *
 * @author Fabien Hermenier
 */
public class BtrPlaceTree extends CommonTree {

    /**
     * All the errors to report.
     */
    protected final ErrorReporter errors;

    /**
     * Make a new tree.
     *
     * @param t    the token to handle. The root of this tree
     * @param errs the errors to report
     */
    public BtrPlaceTree(Token t, ErrorReporter errs) {
        super(t);
        errors = errs;
    }

    /**
     * Parse the root of the tree.
     *
     * @param parent the parent of the root
     * @return a content
     */
    public BtrpOperand go(BtrPlaceTree parent) {
        append(token, "Unhandled token " + token.getText() + " (type=" + token.getType() + ")");
        return IgnorableOperand.getInstance();
    }

    /**
     * Report an error for the current token and generate a content to ignore.
     *
     * @param msg the error message
     * @return an empty content
     */
    public IgnorableOperand ignoreError(String msg) {
        append(token, msg);
        return IgnorableOperand.getInstance();
    }

    /**
     * Add all the error messages included in a reporter.
     *
     * @param err the error reporter
     * @return {@link IgnorableOperand} to indicate to skip the operand
     */
    public IgnorableOperand ignoreErrors(ErrorReporter err) {
        errors.getErrors().addAll(err.getErrors());
        return IgnorableOperand.getInstance();
    }

    /**
     * Add an error message to a given error
     *
     * @param t   the token that points to the error
     * @param msg the error message
     * @return {@link IgnorableOperand} to indicate to skip the operand
     */
    public IgnorableOperand ignoreError(Token t, String msg) {
        append(t, msg);
        return IgnorableOperand.getInstance();
    }

    /**
     * Add an error message related to a given token
     *
     * @param t   the token that points to the error
     * @param msg the error message
     */
    public void append(Token t, String msg) {
        errors.append(t.getLine(), t.getCharPositionInLine(), msg);
    }

    @Override
    public BtrPlaceTree getChild(int i) {
        return (BtrPlaceTree) super.getChild(i);
    }
}

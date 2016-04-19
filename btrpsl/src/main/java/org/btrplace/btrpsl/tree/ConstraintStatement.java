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
import org.btrplace.btrpsl.ErrorReporter;
import org.btrplace.btrpsl.Script;
import org.btrplace.btrpsl.constraint.ConstraintsCatalog;
import org.btrplace.btrpsl.constraint.SatConstraintBuilder;
import org.btrplace.btrpsl.element.BtrpOperand;
import org.btrplace.btrpsl.element.IgnorableOperand;
import org.btrplace.model.constraint.SatConstraint;

import java.util.ArrayList;
import java.util.List;

/**
 * A tree to check a constraint. Root
 * of the tree is the constraint identifier while children are the parameters.
 *
 * @author Fabien Hermenier
 */
public class ConstraintStatement extends BtrPlaceTree {

    private Script script;

    private ConstraintsCatalog catalog;

    /**
     * Make a new Tree parser.
     *
     * @param t    the root symbol
     * @param cat  the catalog of available constraints
     * @param errs the errors to report
     */
    public ConstraintStatement(Token t, Script scr, ConstraintsCatalog cat, ErrorReporter errs) {
        super(t, errs);
        this.catalog = cat;
        this.script = scr;
    }

    /**
     * Build the constraint.
     * The constraint is built if it exists in the catalog and if the parameters
     * are compatible with the constraint signature.
     *
     * @param parent the parent of the root
     * @return {@code Content.empty} if the constraint is successfully built.
     * {@code Content.ignore} if an error occurred (the error is already reported)
     */
    @Override
    public BtrpOperand go(BtrPlaceTree parent) {
        String cname = getText();

        if (catalog == null) {
            return ignoreError("No constraints available");
        }
        SatConstraintBuilder b = catalog.getConstraint(cname);
        if (b == null) {
            ignoreError("Unknown constraint '" + cname + "'");
        }

        //Get the params
        int i = 0;
        boolean discrete = false;
        if (getChild(0).getText().equals(">>")) {
            i = 1;
            discrete = true;
        }
        List<BtrpOperand> params = new ArrayList<>();
        for (; i < getChildCount(); i++) {
            params.add(getChild(i).go(this));
        }
        if (b != null) {
            List<SatConstraint> constraints = b.buildConstraint(this, params);
            for (SatConstraint c : constraints) {
                if (c != null) {
                    if (discrete) {
                        if (!c.setContinuous(false)) {
                            return ignoreError("Discrete restriction is not supported by constraint '" + cname + "'");
                        }
                    } else {
                        //force the continuous mode, if available
                        c.setContinuous(true);
                    }
                    script.addConstraint(c);
                }
            }
        }
        return IgnorableOperand.getInstance();
    }
}

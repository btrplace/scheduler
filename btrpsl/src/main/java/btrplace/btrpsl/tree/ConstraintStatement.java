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
import btrplace.btrpsl.Script;
import btrplace.btrpsl.constraint.ConstraintsCatalog;
import btrplace.btrpsl.constraint.SatConstraintBuilder;
import btrplace.btrpsl.element.BtrpOperand;
import btrplace.btrpsl.element.IgnorableOperand;
import btrplace.model.constraint.SatConstraint;
import org.antlr.runtime.Token;

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

/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
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

  private final Script script;

  private final ConstraintsCatalog catalog;

    /**
     * Make a new Tree parser.
     *
     * @param t    the root symbol
     * @param scr the script we analyse
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
        if (">>".equals(getChild(0).getText())) {
            i = 1;
            discrete = true;
        }
        List<BtrpOperand> params = new ArrayList<>();
        for (; i < getChildCount(); i++) {
            params.add(getChild(i).go(this));
        }
        if (b != null) {
            List<? extends SatConstraint> constraints = b.buildConstraint(this, params);
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

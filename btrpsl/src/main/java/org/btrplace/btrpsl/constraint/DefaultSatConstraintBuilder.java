/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.btrpsl.constraint;

import org.btrplace.btrpsl.element.BtrpOperand;
import org.btrplace.btrpsl.element.DefaultBtrpOperand;
import org.btrplace.btrpsl.element.IgnorableOperand;
import org.btrplace.btrpsl.tree.BtrPlaceTree;

import java.util.List;

/**
 * A toolkit class to ease the implementation of {@link SatConstraintBuilder}.
 *
 * @author Fabien Hermenier
 */
public abstract class DefaultSatConstraintBuilder implements SatConstraintBuilder {

    /**
     * The constraint parameters.
     */
    protected final ConstraintParam<?>[] params;

  private final String id;

    /**
     * Make a new builder.
     *
     * @param n  the constraint name
     * @param ps the expected parameters
     */
    public DefaultSatConstraintBuilder(String n, ConstraintParam<?>[] ps) {
        this.id = n;
        params = ps;
    }

    @Override
    public ConstraintParam<?>[] getParameters() {
        return params;
    }

    @Override
    public String getIdentifier() {
        return id;
    }

    @Override
    public String getSignature() {
        StringBuilder b = new StringBuilder();
        b.append(getIdentifier()).append('(');
        for (int i = 0; i < params.length; i++) {
            b.append(params[i].prettySignature());
            if (i != params.length - 1) {
                b.append(", ");
            }
        }
        b.append(')');
        return b.toString();
    }

    @Override
    public String getFullSignature() {
        StringBuilder b = new StringBuilder();
        b.append(getIdentifier()).append('(');
        for (int i = 0; i < params.length; i++) {
            b.append(params[i].fullSignature());
            if (i != params.length - 1) {
                b.append(", ");
            }
        }
        b.append(')');
        return b.toString();
    }

    /**
     * Check if the provided parameters match the constraint signature
     *
     * @param t   the constraint token
     * @param ops the constraint arguments
     * @return {@code true} iff the arguments match the constraint signature
     */
    public boolean checkConformance(BtrPlaceTree t, List<BtrpOperand> ops) {
        //Arity error
        if (ops.size() != getParameters().length) {
            t.ignoreError("'" + pretty(ops) + "' cannot be casted to '" + getSignature() + "'");
            return false;
        }

        //Type checking
        for (int i = 0; i < ops.size(); i++) {
            BtrpOperand o = ops.get(i);
            ConstraintParam<?> p = params[i];
            if (o == IgnorableOperand.getInstance()) {
                return false;
            }
            if (!p.isCompatibleWith(t, o)) {
                t.ignoreError("'" + pretty(ops) + "' cannot be casted to '" + getSignature() + "'");
                return false;
            }
        }
        return true;
    }

    private String pretty(List<BtrpOperand> ops) {
        StringBuilder b = new StringBuilder();
        b.append(getIdentifier()).append('(');
        for (int i = 0; i < ops.size(); i++) {
            b.append(DefaultBtrpOperand.prettyType(ops.get(i)));
            if (i != ops.size() - 1) {
                b.append(", ");
            }
        }
        b.append(")");
        return b.toString();
    }
}

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
    protected final ConstraintParam[] params;

    private String id;

    /**
     * Make a new builder.
     *
     * @param n  the constraint name
     * @param ps the expected parameters
     */
    public DefaultSatConstraintBuilder(String n, ConstraintParam[] ps) {
        this.id = n;
        params = ps;
    }

    @Override
    public ConstraintParam[] getParameters() {
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
            ConstraintParam p = params[i];
            if (!p.isCompatibleWith(t, o)) {
                if (o != IgnorableOperand.getInstance()) {
                    t.ignoreError("'" + pretty(ops) + "' cannot be casted to '" + getSignature() + "'");
                }
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

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

import org.btrplace.btrpsl.element.BtrpElement;
import org.btrplace.btrpsl.element.BtrpOperand;
import org.btrplace.btrpsl.element.BtrpSet;
import org.btrplace.btrpsl.element.IgnorableOperand;
import org.btrplace.btrpsl.tree.BtrPlaceTree;

import java.util.ArrayList;
import java.util.List;

/**
 * A parameter for a constraint that denotes a set of elements.
 *
 * @author Fabien Hermenier
 */
public class ListOfParam extends DefaultConstraintParam<List> {

    protected boolean canBeEmpty = true;

    protected BtrpOperand.Type type;

    protected int depth;

    /**
     * Make a new parameter for a simple set, possibly empty, of elements.
     *
     * @param n the parameter name
     * @param t the type of the element inside the set
     */
    public ListOfParam(String n, BtrpOperand.Type t) {
        this(n, 1, t, true);
    }

    /**
     * Make a new set parameter
     *
     * @param n the parameter name
     * @param d the set depth
     * @param t the type of the elements inside the set
     * @param e {@code true} to allow empty sets.
     */
    public ListOfParam(String n, int d, BtrpOperand.Type t, boolean e) {
        super(n, "set");
        this.canBeEmpty = e;
        this.type = t;
        this.depth = d;
    }

    @Override
    public String prettySignature() {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < depth; i++) {
            b.append("set<");
        }
        b.append(type);
        for (int i = 0; i < depth; i++) {
            b.append(">");
        }
        return b.toString();
    }

    @Override
    public String fullSignature() {
        return getName() + ": " + prettySignature();
    }

    @Override
    public List transform(SatConstraintBuilder cb, BtrPlaceTree tree, BtrpOperand op) {

        if (op == IgnorableOperand.getInstance()) {
            return null;
        }

        List s = makeList(depth, op);

        if (!canBeEmpty && s.isEmpty()) {
            tree.ignoreError("In '" + cb.getFullSignature() + "', '" + getName() + "' expects a non-empty set");
            return null;
        }
        return s;
    }

    private List makeList(int d, BtrpOperand o) {
        List<Object> h = new ArrayList<>();
        if (d == 0) {
            if (o.type() == BtrpOperand.Type.VM || o.type() == BtrpOperand.Type.node) {
                h.add(((BtrpElement) o).getElement());
            }
        } else {
            if (o instanceof BtrpElement && d == 1) {
                h.add(((BtrpElement) o).getElement());
            } else {
                BtrpSet x = (BtrpSet) o;
                if (d == 1) {

                    for (BtrpOperand op : x.getValues()) {
                        if (op.type() == BtrpOperand.Type.VM || op.type() == BtrpOperand.Type.node) {
                            h.add(((BtrpElement) op).getElement());
                        }
                    }
                } else {
                    for (BtrpOperand op : x.getValues()) {
                        h.add(makeList(d - 1, op));
                    }
                }
            }
        }
        return h;
    }

    @Override
    public boolean isCompatibleWith(BtrPlaceTree t, BtrpOperand o) {
        return (o == IgnorableOperand.getInstance() || (o.type() == type && (o.degree() == depth || (depth == 1 && o.degree() == 0))));
    }
}

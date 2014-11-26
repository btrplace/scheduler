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
import org.btrplace.btrpsl.element.*;
import org.btrplace.model.Element;
import org.btrplace.model.view.NamingService;

/**
 * An enumeration of either nodes or virtual machines.
 *
 * @author Fabien Hermenier
 */
public class EnumElement extends BtrPlaceTree {

    private BtrpOperand.Type type;

    private Script script;

    private NamingService namingServiceNodes;

    private NamingService namingServiceVMs;

    /**
     * Make a new tree.
     *
     * @param payload the root token
     * @param v       the script being check
     * @param ty      the type of the elements in the enumeration
     * @param errors  the errors to report
     */
    public EnumElement(Token payload, NamingService srvNodes, NamingService srvVMs, Script v, BtrpOperand.Type ty, ErrorReporter errors) {
        super(payload, errors);
        this.type = ty;
        this.script = v;
        this.namingServiceNodes = srvNodes;
        this.namingServiceVMs = srvVMs;
    }

    /**
     * Expand the enumeration.
     * Elements are not evaluated
     *
     * @return a set of string or an error
     */
    public BtrpOperand expand() {
        String head = getChild(0).getText().substring(0, getChild(0).getText().length() - 1);
        String tail = getChild(getChildCount() - 1).getText().substring(1);
        BtrpSet res = new BtrpSet(1, BtrpOperand.Type.string);

        for (int i = 1; i < getChildCount() - 1; i++) {
            BtrpOperand op = getChild(i).go(this);
            if (op == IgnorableOperand.getInstance()) {
                return op;
            }
            BtrpSet s = (BtrpSet) op;
            for (BtrpOperand o : s.getValues()) {
                //Compose

                String id = head + o.toString() + tail;
                //Remove heading '@' for the nodes
                if (type == BtrpOperand.Type.node) {
                    res.getValues().add(new BtrpString(id));
                } else {
                    res.getValues().add(new BtrpString(script.id() + '.' + id));
                }

            }
        }
        return res;
    }

    @Override
    public BtrpOperand go(BtrPlaceTree parent) {
        String head = getChild(0).getText().substring(0, getChild(0).getText().length() - 1);
        String tail = getChild(getChildCount() - 1).getText().substring(1);
        BtrpSet res;

        switch (type) {
            case node:
                res = new BtrpSet(1, BtrpOperand.Type.node);
                break;
            case VM:
                res = new BtrpSet(1, BtrpOperand.Type.VM);
                break;
            default:
                return ignoreError("Unsupported enumeration type: '" + type + "'");
        }

        for (int i = 1; i < getChildCount() - 1; i++) {
            BtrpOperand op = getChild(i).go(this);
            if (op == IgnorableOperand.getInstance()) {
                return op;
            }
            BtrpSet s = (BtrpSet) op;
            for (BtrpOperand o : s.getValues()) {

                if (o == IgnorableOperand.getInstance()) {
                    return o;
                }

                //Compose
                String id = head + o.toString() + tail;

                if (type == BtrpOperand.Type.node) {
                    //TODO: 'id' does not contains "@" in the scheduler NamingService
                    Element el = namingServiceNodes.resolve(id);
                    //Element el = namingServiceNodes.resolve(id.substring(1));
                    if (el == null) {
                        //Should be fair as each getChild(i) is a range with at least on child. Prevent from a fake token
                        //with no line number
                        Token t = getChild(i).getChild(0).getToken();
                        if (t.getCharPositionInLine() == -1) {
                            t = parent.getToken();
                        }
                        return ignoreError(t, "Unknown node '" + id.substring(1) + "'");
                    }
                    res.getValues().add(new BtrpElement(BtrpOperand.Type.node, id, namingServiceNodes.resolve(id)));
                } else if (type == BtrpOperand.Type.VM) {
                    String fqn = script.id() + '.' + id;
                    Element el = namingServiceVMs.resolve(fqn);
                    Token t = getChild(i).getChild(0).getToken();
                    if (el == null) {
                        return ignoreError(t, "Unknown VM '" + id + "'");
                    }
                    res.getValues().add(new BtrpElement(BtrpOperand.Type.VM, fqn, namingServiceVMs.resolve(fqn)));
                } else {
                    return ignoreError("Unsupported type '" + type + "' in enumeration");
                }
            }
        }
        return res;
    }
}

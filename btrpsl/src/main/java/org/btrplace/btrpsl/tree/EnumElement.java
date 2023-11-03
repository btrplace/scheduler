/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.btrpsl.tree;

import org.antlr.runtime.Token;
import org.btrplace.btrpsl.ErrorReporter;
import org.btrplace.btrpsl.Script;
import org.btrplace.btrpsl.element.BtrpElement;
import org.btrplace.btrpsl.element.BtrpOperand;
import org.btrplace.btrpsl.element.BtrpSet;
import org.btrplace.btrpsl.element.BtrpString;
import org.btrplace.btrpsl.element.IgnorableOperand;
import org.btrplace.model.Element;
import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.model.view.NamingService;

/**
 * An enumeration of either nodes or virtual machines.
 *
 * @author Fabien Hermenier
 */
public class EnumElement extends BtrPlaceTree {

  private final BtrpOperand.Type type;

  private final Script script;

  private final NamingService<Node> namingServiceNodes;

  private final NamingService<VM> namingServiceVMs;

    /**
     * Make a new tree.
     *
     * @param payload the root token
     * @param srvNodes the Naming Service for the nodes
     * @param srvVMs the Naming Service for the VMs
     * @param v       the script being check
     * @param ty      the type of the elements in the enumeration
     * @param errors  the errors to report
     */
    public EnumElement(Token payload, NamingService<Node> srvNodes, NamingService<VM> srvVMs, Script v, BtrpOperand.Type ty, ErrorReporter errors) {
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
        BtrpSet res = new BtrpSet(1, BtrpOperand.Type.STRING);

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
                if (type == BtrpOperand.Type.NODE) {
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
            case NODE:
                res = new BtrpSet(1, BtrpOperand.Type.NODE);
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

                if (type == BtrpOperand.Type.NODE) {
                    //TODO: 'id' does not contains "@" in the scheduler NamingService
                    Element el = namingServiceNodes.resolve(id);
                    if (el == null) {
                        //Should be fair as each getChild(i) is a range with at least on child. Prevent from a fake token
                        //with no line number
                        Token t = getChild(i).getChild(0).getToken();
                        if (t.getCharPositionInLine() == -1) {
                            t = parent.getToken();
                        }
                        return ignoreError(t, "Unknown node '" + id.substring(1) + "'");
                    }
                    res.getValues().add(new BtrpElement(BtrpOperand.Type.NODE, id, namingServiceNodes.resolve(id)));
                } else {
                    String fqn = script.id() + '.' + id;
                    Element el = namingServiceVMs.resolve(fqn);
                    Token t = getChild(i).getChild(0).getToken();
                    if (el == null) {
                        return ignoreError(t, "Unknown VM '" + id + "'");
                    }
                    res.getValues().add(new BtrpElement(BtrpOperand.Type.VM, fqn, namingServiceVMs.resolve(fqn)));
                }
            }
        }
        return res;
    }
}

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

package org.btrplace.btrpsl;

import org.btrplace.btrpsl.element.BtrpElement;
import org.btrplace.btrpsl.element.BtrpOperand;
import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.model.constraint.SatConstraint;

import java.util.*;

/**
 * A btrplace script that contains the declaration of VMs, nodes, and constraints.
 *
 * @author Fabien Hermenier
 */
public class Script {

    private Set<VM> vms;

    private Set<Node> nodes;

    private List<Script> dependencies;

    /**
     * The identifier of the script.
     */
    private String fqn;

    /**
     * the list of placement constraints.
     */
    private Set<SatConstraint> cstrs;

    /**
     * Default file extension for script.
     */
    public static final String EXTENSION = ".btrp";

    /**
     * The list of exported operand.
     */
    private Map<String, BtrpOperand> exported;

    /**
     * The limitations of the exported operands.
     * The value is a namespace identifier. If it ends
     * with a *; the beginning of the namespace as to be used to
     * match.
     */
    private Map<String, Set<String>> exportScopes;

    /**
     * Make a new script with a given identifier.
     */
    public Script() {
        this.dependencies = new ArrayList<>();
        this.cstrs = new HashSet<>();
        this.exported = new HashMap<>();
        this.exportScopes = new HashMap<>();
        this.vms = new HashSet<>();
        this.nodes = new HashSet<>();
    }

    /**
     * Set the fully qualified name of the script.
     *
     * @param n the fully qualified name
     */
    public void setFullyQualifiedName(String n) {
        this.fqn = n;
    }

    /**
     * Get the namespace the script belongs to
     *
     * @return the namespace name or {@code null}
     */
    public String getNamespace() {
        if (fqn.contains(".")) {
            return this.fqn.substring(0, fqn.lastIndexOf('.'));
        }
        return "";
    }

    /**
     * Get the local name of the script.
     *
     * @return a non-empty String
     */
    public String getlocalName() {
        if (fqn.contains(".")) {
            return this.fqn.substring(fqn.lastIndexOf('.') + 1, this.fqn.length());
        }
        return fqn;
    }

    /**
     * Get the unique string identifier of the script.
     *
     * @return a non-empty string
     */
    public String id() {
        return this.fqn;
    }


    /**
     * Get the VMs declared in the script.
     *
     * @return a set of nodes that may be empty
     */
    public Set<VM> getVMs() {
        return vms;
    }

    /**
     * Get the VMs declared in the script.
     *
     * @return a set of nodes that may be empty
     */
    public Set<Node> getNodes() {
        return nodes;
    }

    /**
     * Add a constraint to a script.
     *
     * @param c the constraint
     * @return {@code true} iff the constraint has been added
     */
    public boolean addConstraint(SatConstraint c) {
        return cstrs.add(c);
    }

    /**
     * Get the constraints declared in the script.
     *
     * @return a set of constraints that may be empty
     */
    public Set<SatConstraint> getConstraints() {
        return cstrs;
    }

    /**
     * Add a collection of nodes or elements.
     *
     * @param elems the elements to add
     * @return {@code true} iff at least one element has been added
     */
    public boolean add(Collection<BtrpElement> elems) {
        boolean ret = false;
        for (BtrpElement el : elems) {
            ret |= add(el);
        }
        return ret;
    }

    /**
     * Add a VM or a node to the script.
     *
     * @param el the element to add
     * @return {@code true} if the was was added
     */
    public boolean add(BtrpElement el) {
        switch (el.type()) {
            case VM:
                if (!this.vms.add((VM) el.getElement())) {
                    return false;
                }
                break;
            case node:
                if (!this.nodes.add((Node) el.getElement())) {
                    return false;
                }
                break;
            default:
                return false;
        }
        return true;

    }

    /**
     * Get all the operand a given script can import
     *
     * @param ns the script namespace
     * @return a list of importable operand, may be empty
     */
    public List<BtrpOperand> getImportables(String ns) {
        List<BtrpOperand> importable = new ArrayList<>();
        for (String symbol : getExported()) {
            if (canImport(symbol, ns)) {
                importable.add(getImportable(symbol, ns));
            }
        }
        return importable;
    }

    /**
     * Get the exported operand from its label.
     *
     * @param label     the operand label
     * @param namespace the namespace of the script that ask for this operand.
     * @return the operand if exists or {@code null}
     */
    public BtrpOperand getImportable(String label, String namespace) {
        if (canImport(label, namespace)) {
            return exported.get(label);
        }
        return null;
    }

    /**
     * Get the exported variable.
     * The variable must be importable by anyone.
     *
     * @param label the label that denotes the variable
     * @return {@code null} if the label does not point to a variable or if the variable as some restrictions
     * wrt. its access
     */
    public BtrpOperand getImportable(String label) {
        return getImportable(label, "");
    }

    /**
     * Indicates whether a namespace can import an exported variable or not.
     * To be imported, the label must point to an exported variable, with no import restrictions
     * or with a given namespace compatible with the restrictions.
     *
     * @param label     the label to import
     * @param namespace the namespace of the script asking for the variable
     * @return {@code true} if the variable can be imported. {@code false} otherwise}
     */
    public boolean canImport(String label, String namespace) {
        if (!exported.containsKey(label)) {
            return false;
        }
        Set<String> scopes = exportScopes.get(label);
        for (String scope : scopes) {
            if (scope.equals(namespace)) {
                return true;
            } else if (scope.endsWith("*") && namespace.startsWith(scope.substring(0, scope.length() - 1))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Add an external operand that can be accessed from several given scopes.
     * A scope is a namespace that can ends with a wildcard ('*'). In this situation. The beginning
     * of the scope is considered
     *
     * @param name   the name of the exported operand
     * @param e      the operand to add
     * @param scopes the namespaces of the scripts that can use this variable. {@code null} to allow anyone
     */
    public void addExportable(String name, BtrpOperand e, Set<String> scopes) {
        this.exported.put(name, e);
        this.exportScopes.put(name, scopes);
    }

    /**
     * Get the fully qualified name of a symbol.
     *
     * @param name the symbol name
     * @return the fully qualified symbol name.
     */
    public String fullyQualifiedSymbolName(String name) {
        if (name.equals(SymbolsTable.ME)) {
            return "$".concat(id());
        } else if (name.startsWith("$")) {
            return "$" + id() + '.' + name.substring(1);
        }
        return id() + '.' + name;
    }

    /**
     * Get the set of exported operands label.
     *
     * @return a set of label that may be empty
     */
    public Set<String> getExported() {
        return this.exported.keySet();
    }

    /**
     * Get the direct dependencies of this script.
     *
     * @return the list of dependencies for this script.
     */
    public List<Script> getDependencies() {
        return this.dependencies;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(id()).append("{\n");
        buf.append(" vms: ").append(vms).append("\n");
        buf.append(" nodes: ").append(nodes).append("\n");
        buf.append(" exported: ").append(exported).append("\n");
        buf.append(" constraints:\n");
        for (SatConstraint c : cstrs) {
            buf.append("\t").append(c).append("\n");
        }
        buf.append("}\n");
        return buf.toString();
    }

    /**
     * Textual representation for the dependencies.
     *
     * @return a String containing the dependency tree.
     */
    public String prettyDependencies() {
        StringBuilder b = new StringBuilder();
        b.append(id()).append('\n');
        for (Iterator<Script> ite = dependencies.iterator(); ite.hasNext(); ) {
            Script n = ite.next();
            prettyDependencies(b, !ite.hasNext(), 0, n);
        }
        return b.toString();
    }

    private void prettyDependencies(StringBuilder b, boolean last, int lvl, Script v) {
        for (int i = 0; i < lvl; i++) {
            b.append("   ");
        }
        b.append(last ? "\\" : "|");
        b.append("- ").append(v.id()).append('\n');
        for (Iterator<Script> ite = v.getDependencies().iterator(); ite.hasNext(); ) {
            Script n = ite.next();
            prettyDependencies(b, !ite.hasNext(), lvl + 1, n);
        }
    }
}

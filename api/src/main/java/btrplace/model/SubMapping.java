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

package btrplace.model;

import gnu.trove.set.hash.THashSet;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 * A sub-mapping that is a limited version of a parent mapping.
 * The scope is defined as a collection of nodes.
 * The mapping is considered as immutable.
 *
 * @author Fabien Hermenier
 */
public class SubMapping implements Mapping {

    private Mapping parent;

    private Set<Node> scope;

    private Set<VM> myRunnings = null;

    private Set<VM> mySleepings = null;

    private Set<Node> myOnlines = null;

    private Set<Node> myOfflines = null;

    /**
     * Make a new mapping.
     *
     * @param parent the parent mapping
     * @param scope  the nodes that limit the scope of the new mapping. These nodes must all belong to the original mapping
     */
    public SubMapping(Mapping parent, Collection<Node> scope) {
        this.parent = parent;
        this.scope = new THashSet<>(scope);
    }

    @Override
    public boolean addRunningVM(VM vm, Node node) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addSleepingVM(VM vm, Node node) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addReadyVM(VM vm) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(VM vm) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Node n) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Node> getOnlineNodes() {
        if (myOnlines == null) {
            myOnlines = onlyMyNodes(parent.getOnlineNodes());
        }
        return myOnlines;
    }

    @Override
    public void addOnlineNode(Node node) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addOfflineNode(Node node) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Node> getOfflineNodes() {
        if (myOfflines == null) {
            myOfflines = onlyMyNodes(parent.getOfflineNodes());
        }
        return myOfflines;
    }

    private Set<Node> onlyMyNodes(Set<Node> ns) {
        Set<Node> my = new THashSet<>();
        for (Node n : ns) {
            if (scope.contains(n)) {
                my.add(n);
            }
        }
        return my;
    }

    @Override
    public Set<VM> getRunningVMs() {
        if (myRunnings == null) {
            myRunnings = parent.getRunningVMs(scope);
        }
        return myRunnings;
    }

    @Override
    public Set<VM> getSleepingVMs() {
        if (mySleepings == null) {
            mySleepings = new THashSet<>();
            for (Node n : scope) {
                mySleepings.addAll(parent.getSleepingVMs(n));
            }
        }
        return mySleepings;
    }

    @Override
    public Set<VM> getSleepingVMs(Node n) {
        if (scope.contains(n)) {
            return parent.getSleepingVMs(n);
        }
        return Collections.emptySet();
    }

    @Override
    public Set<VM> getRunningVMs(Node n) {
        if (scope.contains(n)) {
            return parent.getRunningVMs(n);
        }
        return Collections.emptySet();
    }

    @Override
    public Set<VM> getReadyVMs() {
        return parent.getReadyVMs();
    }

    @Override
    public Set<VM> getAllVMs() {
        Set<VM> res = new THashSet<>();
        for (Node n : scope) {
            res.addAll(getRunningVMs());
            res.addAll(getSleepingVMs());
        }
        return res;
    }

    @Override
    public Set<Node> getAllNodes() {
        //We suppose here that every node in the scope belong to the mapping
        return scope;
    }

    @Override
    public Node getVMLocation(VM vm) {
        Node n = parent.getVMLocation(vm);
        if (scope.contains(n)) {
            return n;
        }
        return null;
    }

    @Override
    public Set<VM> getRunningVMs(Collection<Node> ns) {
        Set<VM> res = new THashSet<>();
        for (Node n : ns) {
            if (scope.contains(n)) {
                res.addAll(parent.getRunningVMs(n));
            }
        }
        return res;
    }

    @Override
    public DefaultMapping clone() {
        DefaultMapping c = new DefaultMapping();
        //Keep only the nodes inside the scope
        for (Node n : scope) {
            if (parent.getOnlineNodes().contains(n)) {
                c.addOnlineNode(n);
                for (VM v : parent.getRunningVMs(n)) {
                    c.addRunningVM(v, n);
                }
                for (VM v : parent.getSleepingVMs(n)) {
                    c.addSleepingVM(v, n);
                }
            } else if (parent.getOfflineNodes().contains(n)) {
                c.addOfflineNode(n);
            }
        }
        return c;
    }

    @Override
    public boolean contains(VM vm) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean contains(Node node) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clearNode(Node u) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clearAllVMs() {
        throw new UnsupportedOperationException();
    }
}

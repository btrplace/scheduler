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

package btrplace.safeplace.spec.term.func;

import btrplace.model.Node;
import btrplace.safeplace.spec.type.NodeStateType;
import btrplace.safeplace.spec.type.NodeType;
import btrplace.safeplace.spec.type.Type;
import btrplace.safeplace.verification.spec.SpecModel;

import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class NodeState extends Function<NodeStateType.Type> {

    @Override
    public NodeStateType type() {
        return NodeStateType.getInstance();
    }

    @Override
    public NodeStateType.Type eval(SpecModel mo, List<Object> args) {

        Node n = (Node) args.get(0);
        if (n == null) {
            return null;
        }
        return mo.getMapping().state(n);
        /*if (mo.getMapping().isOffline(n)) {
            return NodeStateType.Type.offline;
        } else if (mo.getMapping().isOnline(n)) {
            return NodeStateType.Type.online;
        } else {
            return null;
        } */
    }

    @Override
    public String id() {
        return "nodeState";
    }

    @Override
    public Type[] signature() {
        return new Type[]{NodeType.getInstance()};
    }
}

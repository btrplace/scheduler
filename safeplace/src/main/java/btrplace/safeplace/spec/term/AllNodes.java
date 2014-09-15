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

package btrplace.safeplace.spec.term;

import btrplace.model.Node;
import btrplace.safeplace.spec.type.NodeType;
import btrplace.safeplace.verification.spec.SpecModel;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class AllNodes extends Primitive {

    private static Random rnd = new Random();

    public AllNodes() {
        super("nodes", NodeType.getInstance());
    }

    @Override
    public Set<Node> eval(SpecModel m) {
        return m.getMapping().nodes();
    }

    @Override
    public Node pickIn(SpecModel mo) {
        int n = rnd.nextInt(mo.getMapping().nodes().size());
        Iterator<Node> it = mo.getMapping().nodes().iterator();

        while (n > 0) {
            it.next();
            n--;
        }
        return it.next();
    }

    @Override
    public Set<Node> pickIncluded(SpecModel mo) {
        Set<Node> s = new HashSet<>();
        for (Node v : mo.getMapping().nodes()) {
            if (rnd.nextBoolean()) {
                s.add(v);
            }
        }
        return s;
    }

    @Override
    public boolean isConstant() {
        return true;
    }

}

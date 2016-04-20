/*
 * Copyright (c) 2016 University Nice Sophia Antipolis
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

package org.btrplace.scheduler.choco.constraint.mttr;

import org.btrplace.model.Node;
import org.btrplace.scheduler.choco.ReconfigurationProblem;
import org.btrplace.scheduler.choco.view.CShareableResource;

import java.util.Comparator;

/**
 * Created by fhermeni on 17/02/2015.
 */
public class CShareableResourceComparator implements Comparator<Node> {

    private ReconfigurationProblem rp;

    private CShareableResource rc;

    private int ordering;

    public CShareableResourceComparator(ReconfigurationProblem rp, CShareableResource rc, boolean asc) {
        this.rp = rp;
        this.rc = rc;
        ordering = asc ? 1 : -1;
    }
    @Override
    public int compare(Node n1, Node n2) {
        int i = rp.getNode(n1);
        int j = rp.getNode(n2);
        return ordering * (rc.getPhysicalUsage(i).getLB() - rc.getPhysicalUsage(j).getLB());
    }
}

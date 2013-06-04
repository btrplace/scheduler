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

import java.util.ArrayList;
import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class Util {

    public static List<VM> newVMs(Model mo, int nb) {
        List<VM> vms = new ArrayList<>(nb);
        for (int i = 0; i < nb; i++) {
            vms.add(mo.newVM());
        }
        return vms;
    }

    public static List<VM> newVMs(int nb) {
        List<VM> vms = new ArrayList<>(nb);
        for (int i = 0; i < nb; i++) {
            vms.add(new VM(i));
        }
        return vms;
    }

    public static List<Node> newNodes(Model mo, int nb) {
        List<Node> ns = new ArrayList<>(nb);
        for (int i = 0; i < nb; i++) {
            ns.add(mo.newNode());
        }
        return ns;
    }

    public static List<Node> newNodes(int nb) {
        List<Node> ns = new ArrayList<>(nb);
        for (int i = 0; i < nb; i++) {
            ns.add(new Node(i));
        }
        return ns;
    }

}

/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.model;

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

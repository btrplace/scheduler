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

package org.btrplace.fuzzer.generator;

import org.btrplace.model.DefaultModel;
import org.btrplace.model.Model;
import org.btrplace.model.Node;
import org.btrplace.model.VM;

import java.util.Collection;
import java.util.Random;

/**
 * Created by fhermeni on 07/09/2015.
 */
public class ModelGenerator implements ModelParams {

    private ModelParams ps;

    public ModelGenerator() {
        ps = new DefaultModelParams();
    }


    @Override
    public ModelGenerator vms(int n) {
        ps.vms(n);
        return this;
    }

    @Override
    public ModelGenerator nodes(int n) {
        ps.nodes(n);
        return this;
    }

    @Override
    public ModelGenerator with(ModelViewFuzzer f) {
        ps.with(f);
        return this;
    }

    @Override
    public int vms() {
        return ps.vms();
    }

    @Override
    public int nodes() {
        return ps.nodes();
    }

    public Model build() {
        Random rnd = new Random();
        Model mo = new DefaultModel();
        for (int i = 0; i < ps.nodes(); i++) {
            Node n = mo.newNode();

            //if (rnd.nextBoolean()) {
                mo.getMapping().addOnlineNode(n);
           /* } else {
                mo.getMapping().addOfflineNode(n);
            }*/
        }

        for (int i = 0; i < ps.vms(); i++) {
            VM v = mo.newVM();
            /*switch (rnd.nextInt(3)) {
                case 0:
                    mo.getMapping().addReadyVM(v);
                    break;
                case 1:
                    mo.getMapping().addSleepingVM(v, oneOf(rnd, mo.getMapping().getOnlineNodes()));
                    break;
                case 2:*/
                    mo.getMapping().addRunningVM(v, oneOf(rnd, mo.getMapping().getOnlineNodes()));
/*                    break;
            }*/
        }
        return mo;
    }

    public ModelGenerator setParams(ModelParams ps) {
        this.ps = ps;
        return this;
    }

    private Node oneOf(Random rnd, Collection<Node> nodes) {

        int cnt = rnd.nextInt(nodes.size()) + 1;

        for (Node n : nodes) {
            cnt--;
            if (cnt == 0) {
                return n;
            }
        }
        return null; //should not occur
    }
}

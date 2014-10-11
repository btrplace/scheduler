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

package org.btrplace.model;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Unit tests for {@link SynchronizedElementBuilder}.
 *
 * @author Fabien Hermenier
 */
public class SynchronizedElementBuilderTest {

    @Test
    public void testVMRegistration() {
        DefaultElementBuilder de = new DefaultElementBuilder();
        ElementBuilder eb = new SynchronizedElementBuilder(de);
        VM v = eb.newVM();
        VM vX = eb.newVM();
        Assert.assertNotEquals(v, vX);
        Assert.assertTrue(eb.contains(v));
        Assert.assertNull(eb.newVM(v.id()));

        int nextId = v.id() + 1000;
        VM v2 = eb.newVM(nextId);
        Assert.assertTrue(eb.contains(v2));
    }

    @Test
    public void testNodeRegistration() {
        ElementBuilder eb = new SynchronizedElementBuilder(new DefaultElementBuilder());
        Node n = eb.newNode();
        Assert.assertTrue(eb.contains(n));
        Assert.assertNull(eb.newNode(n.id()));

        int nextId = n.id() + 1000;
        Node n2 = eb.newNode(nextId);
        Assert.assertTrue(eb.contains(n2));
    }

    @Test
    public void testMultipleIDDemand() {
        final ElementBuilder eb = new SynchronizedElementBuilder(new DefaultElementBuilder());
        int nbThreads = 10;
        final int nbAllocs = 1000;
        final int[] used = new int[nbThreads * nbAllocs];
        Thread[] ths = new Thread[nbThreads];
        for (int i = 0; i < nbThreads; i++) {
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int x = 0; x < nbAllocs; x++) {
                        VM v = eb.newVM();
                        used[v.id()]++;
                    }
                }
            });
            ths[i] = t;
            t.start();
        }
        try {
            for (int i = 0; i < nbThreads; i++) {
                ths[i].join();
            }
        } catch (InterruptedException ex) {
            Assert.fail(ex.getMessage(), ex);
        }
        for (int i = 0; i < used.length; i++) {
            if (used[i] != 1) {
                Assert.fail("ID '" + i + "' used " + used[i] + " times");
            }
        }
    }
}

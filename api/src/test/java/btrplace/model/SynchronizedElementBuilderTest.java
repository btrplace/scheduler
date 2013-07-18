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

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;

/**
 * Unit tests for {@link SynchronizedElementBuilder}.
 *
 * @author Fabien Hermenier
 */
public class SynchronizedElementBuilderTest {

    @Test
    public void testVMRegistration() {
        ElementBuilder eb = new SynchronizedElementBuilder();
        VM v = eb.newVM();
        VM vX = eb.newVM();
        Assert.assertNotEquals(v, vX);
        Assert.assertTrue(eb.getVMs().contains(v));
        Assert.assertNull(eb.newVM(v.id()));

        int nextId = v.id() + 1000;
        VM v2 = eb.newVM(nextId);
        Assert.assertTrue(eb.getVMs().contains(v2));
    }

    @Test
    public void testNodeRegistration() {
        ElementBuilder eb = new SynchronizedElementBuilder();
        Node n = eb.newNode();
        Assert.assertTrue(eb.getNodes().contains(n));
        Assert.assertNull(eb.newNode(n.id()));

        int nextId = n.id() + 1000;
        Node n2 = eb.newNode(nextId);
        Assert.assertTrue(eb.getNodes().contains(n2));
    }

    @Test
    public void testInstantiation() {
        ElementBuilder eb = new SynchronizedElementBuilder();
        Assert.assertEquals(eb.getVMs().size(), 0);
        Assert.assertEquals(eb.getNodes().size(), 0);

        eb = new SynchronizedElementBuilder(Arrays.asList(new VM(1), new VM(2), new VM(3)), Arrays.asList(new Node(1), new Node(0)));
        Assert.assertEquals(eb.getVMs().size(), 3);
        Assert.assertEquals(eb.getNodes().size(), 2);
    }

    @Test
    public void testMultipleIDDemand() {
        final ElementBuilder eb = new SynchronizedElementBuilder();
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

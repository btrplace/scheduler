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

package org.btrplace.plan.event;

import org.btrplace.model.*;
import org.btrplace.model.view.ModelView;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link SubstitutedVMEvent}.
 *
 * @author Fabien Hermenier
 */
public class SubstitutedVMEventTest {

    static Model mo = new DefaultModel();
    static List<Node> ns = Util.newNodes(mo, 10);
    static List<VM> vms = Util.newVMs(mo, 10);
    static SubstitutedVMEvent s = new SubstitutedVMEvent(vms.get(0), vms.get(1));

    @Test
    public void testInstantiation() {
        Assert.assertEquals(s.getVM(), vms.get(0));
        Assert.assertEquals(s.getNewVM(), vms.get(1));
        Assert.assertFalse(s.toString().contains("null"));
    }

    @Test
    public void testVisit() {
        ActionVisitor visitor = mock(ActionVisitor.class);
        s.visit(visitor);
        verify(visitor).visit(s);
    }

    @Test
    public void testApply() {
        Model mo = new DefaultModel();
        Mapping map = mo.getMapping();
        map.addOnlineNode(ns.get(0));
        map.addOnlineNode(ns.get(1));
        map.addReadyVM(vms.get(0));
        map.addReadyVM(vms.get(2));
        ModelView v = mock(ModelView.class);
        mo.attach(v);
        Assert.assertTrue(s.apply(mo));
        verify(v).substituteVM(vms.get(0), vms.get(1));
    }
}

/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.plan.event;

import org.btrplace.model.DefaultModel;
import org.btrplace.model.Mapping;
import org.btrplace.model.Model;
import org.btrplace.model.Node;
import org.btrplace.model.Util;
import org.btrplace.model.VM;
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

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

package btrplace.plan.event;

import btrplace.model.DefaultModel;
import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.view.ModelView;
import btrplace.test.PremadeElements;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link SubstitutedVMEvent}.
 *
 * @author Fabien Hermenier
 */
public class SubstitutedVMEventTest implements PremadeElements {

    static SubstitutedVMEvent s = new SubstitutedVMEvent(vm1, vm2);

    @Test
    public void testInstantiation() {
        Assert.assertEquals(s.getVM(), vm1);
        Assert.assertEquals(s.getNewint(), vm2);
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
        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addReadyVM(vm1);
        map.addReadyVM(vm3);
        ModelView v = mock(ModelView.class);
        mo.attach(v);
        Assert.assertTrue(s.apply(mo));
        verify(v).substitute(vm1, vm2);
    }
}

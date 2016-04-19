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

package org.btrplace.plan;

import org.btrplace.model.DefaultModel;
import org.btrplace.model.Model;
import org.btrplace.model.constraint.SatConstraint;
import org.btrplace.plan.event.Action;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Basic unit tests for {@link ReconfigurationPlanCheckerException}.
 *
 * @author Fabien Hermenier
 */
public class ReconfigurationPlanCheckerExceptionTest {

    @Test
    public void testInstantiationWithModel() {
        SatConstraint c = Mockito.mock(SatConstraint.class);
        Model m = new DefaultModel();
        ReconfigurationPlanCheckerException ex = new ReconfigurationPlanCheckerException(c, m, true);
        Assert.assertEquals(ex.getModel(), m);
        Assert.assertEquals(ex.isOrigin(), true);
        Assert.assertNull(ex.getAction());

        ex = new ReconfigurationPlanCheckerException(c, m, false);
        Assert.assertEquals(ex.isOrigin(), false);
        Assert.assertEquals(ex.getConstraint(), c);
        Assert.assertFalse(ex.toString().contains("null"));
    }

    @Test
    public void testInstantiationWithAction() {
        SatConstraint c = Mockito.mock(SatConstraint.class);
        Action a = Mockito.mock(Action.class);
        ReconfigurationPlanCheckerException ex = new ReconfigurationPlanCheckerException(c, a);
        Assert.assertEquals(ex.getAction(), a);
        Assert.assertNull(ex.getModel());
        Assert.assertFalse(ex.toString().contains("null"));
    }
}

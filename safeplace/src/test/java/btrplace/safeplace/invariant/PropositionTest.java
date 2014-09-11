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

package btrplace.safeplace.invariant;

import btrplace.safeplace.spec.prop.Proposition;
import btrplace.safeplace.verification.spec.SpecModel;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Fabien Hermenier
 */
public class PropositionTest {

    @Test
    public void testTrue() {
        Proposition t = Proposition.True;
        Assert.assertEquals(t.not(), Proposition.False);
        Assert.assertEquals(t.eval(new SpecModel()), Boolean.TRUE);
        Assert.assertEquals(t.toString(), "true");
    }

    @Test
    public void testFalse() {
        Proposition t = Proposition.False;
        Assert.assertEquals(t.toString(), "false");
        Assert.assertEquals(t.not(), Proposition.True);
        Assert.assertEquals(t.eval(new SpecModel()), Boolean.FALSE);
    }
}

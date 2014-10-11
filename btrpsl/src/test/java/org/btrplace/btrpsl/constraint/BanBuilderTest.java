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

package org.btrplace.btrpsl.constraint;

import org.btrplace.btrpsl.ScriptBuilder;
import org.btrplace.btrpsl.ScriptBuilderException;
import org.btrplace.model.DefaultModel;
import org.btrplace.model.VM;
import org.btrplace.model.constraint.Ban;
import org.btrplace.model.constraint.SatConstraint;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.Set;

/**
 * Unit tests for {@link BanBuilder}.
 *
 * @author Fabien Hermenier
 */
@Test
public class BanBuilderTest {

    @DataProvider(name = "badBans")
    public Object[][] getBadSignatures() {
        return new String[][]{
                new String[]{"ban(@N1,@N[1..10]);"},
                new String[]{"ban({VM1},VM[1..5]);"},
                new String[]{"ban({VM1},@N[1..10],@N1);"},
                new String[]{"ban({VM1},@N[1..10],VM1);"},
                new String[]{"ban({VM1},@N[1..10],@N1);"},
                new String[]{"ban({VM1},{@N[1..5], @N[6..10]});"},
                new String[]{"ban({},@N[1..5]);"},
                new String[]{"ban(VM1,{});"},
                new String[]{"ban({},{});"},
        };
    }

    @Test(dataProvider = "badBans", expectedExceptions = {ScriptBuilderException.class})
    public void testBadSignatures(String str) throws ScriptBuilderException {
        ScriptBuilder b = new ScriptBuilder(new DefaultModel());
        try {
            b.build("namespace test; VM[1..10] : tiny;\n@N[1..10] : defaultNode;" + str);
        } catch (ScriptBuilderException ex) {
            System.out.println(ex.getMessage());
            throw ex;
        }
    }

    @DataProvider(name = "goodBans")
    public Object[][] getGoodSignatures() {
        return new Object[][]{
                new Object[]{">>ban(VM1,{@N1});", 1, 1, false},
                new Object[]{"ban({VM1},@N1);", 1, 1, true},
                new Object[]{">>ban(VM1,@N[1..10]);", 1, 10, false},
                new Object[]{"ban({VM1,VM2},@N[1..10]);", 2, 10, true},
        };
    }

    @Test(dataProvider = "goodBans")
    public void testGoodSignatures(String str, int nbVMs, int nbNodes, boolean c) throws Exception {
        ScriptBuilder b = new ScriptBuilder(new DefaultModel());
        Set<SatConstraint> cstrs = b.build("namespace test; VM[1..10] : tiny;\n@N[1..10] : defaultNode;\n" + str).getConstraints();
        Assert.assertEquals(cstrs.size(), nbVMs);
        Set<VM> vms = new HashSet<>();
        for (SatConstraint s : cstrs) {
            Assert.assertTrue(s instanceof Ban);
            Assert.assertEquals(s.getInvolvedVMs().size(), 1);
            Assert.assertTrue(vms.addAll(s.getInvolvedVMs()));
            Assert.assertEquals(s.getInvolvedNodes().size(), nbNodes);
            Assert.assertEquals(s.isContinuous(), c);
        }
    }
}

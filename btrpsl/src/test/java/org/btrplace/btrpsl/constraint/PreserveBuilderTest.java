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
import org.btrplace.model.constraint.Preserve;
import org.btrplace.model.constraint.SatConstraint;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.Set;

/**
 * Unit tests for {@link PreserveBuilder}.
 *
 * @author Fabien Hermenier
 */
@Test
public class PreserveBuilderTest {

    @DataProvider(name = "badPreserves")
    public Object[][] getBadSignatures() {
        return new String[][]{
                new String[]{"preserve({VM1,VM2},\"foo\", -1);"},
                new String[]{"preserve({VM1,VM2},\"foo\", 1.2);"},
                new String[]{"preserve(\"foo\",-1);"},
                new String[]{"preserve({},5);"},
                new String[]{"preserve(VM[1,3,5]);"},
                new String[]{"preserve(VM[1,3,5,15],\"foo\");"},
                //new String[]{"preserve(5);"},
        };
    }

    @Test(dataProvider = "badPreserves", expectedExceptions = {ScriptBuilderException.class})
    public void testBadSignatures(String str) throws ScriptBuilderException {
        ScriptBuilder b = new ScriptBuilder(new DefaultModel());
        try {
            b.build("namespace test; VM[1..10] : tiny;\n@N[1..20] : defaultNode;\n" + str);
        } catch (ScriptBuilderException ex) {
            System.out.println(str + " " + ex.getMessage());
            throw ex;
        }
    }

    @DataProvider(name = "goodPreserves")
    public Object[][] getGoodSignatures() {
        return new Object[][]{
                new Object[]{">>preserve(VM1,\"foo\", 3);", 1, "foo", 3},
                new Object[]{"preserve(VM[1..4],\"bar\", 7);", 4, "bar", 7},
        };
    }

    @Test(dataProvider = "goodPreserves")
    public void testGoodSignatures(String str, int nbVMs, String rcId, int a) throws Exception {
        ScriptBuilder b = new ScriptBuilder(new DefaultModel());
        Set<SatConstraint> cstrs = b.build("namespace test; VM[1..10] : tiny;\n@N[1..20] : defaultNode;\n" + str).getConstraints();
        Assert.assertEquals(cstrs.size(), nbVMs);
        Set<VM> vms = new HashSet<>();
        for (SatConstraint x : cstrs) {
            Assert.assertTrue(x instanceof Preserve);
            Assert.assertTrue(vms.addAll(x.getInvolvedVMs()));
            Assert.assertEquals(x.isContinuous(), false);
            Assert.assertEquals(((Preserve) x).getResource(), rcId);
            Assert.assertEquals(((Preserve) x).getAmount(), a);

        }
    }
}

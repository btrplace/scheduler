/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.btrpsl.constraint;

import org.btrplace.btrpsl.ScriptBuilder;
import org.btrplace.btrpsl.ScriptBuilderException;
import org.btrplace.model.DefaultModel;
import org.btrplace.model.VM;
import org.btrplace.model.constraint.Root;
import org.btrplace.model.constraint.SatConstraint;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.Set;

/**
 * Unit tests for {@link org.btrplace.btrpsl.constraint.RootBuilder}.
 *
 * @author Fabien Hermenier
 */
@Test
public class RootBuilderTest {

    @DataProvider(name = "badRoots")
    public Object[][] getBadSignatures() {
        return new String[][]{
                new String[]{"root({VM1,VM2},@N1);"},
                new String[]{"root({});"},
                new String[]{"root(@N[1..10]);"},
                new String[]{"root(VMa);"},
                new String[]{"root();"},
                new String[]{">>root({VM1});"}, //root is necessarily continuous
        };
    }

    @Test(dataProvider = "badRoots", expectedExceptions = {ScriptBuilderException.class})
    public void testBadSignatures(String str) throws ScriptBuilderException {
        ScriptBuilder b = new ScriptBuilder(new DefaultModel());
        try {
            b.build("namespace test; VM[1..10] : tiny;\n" + str);
        } catch (ScriptBuilderException ex) {
            System.out.println(str + " " + ex.getMessage());
            throw ex;
        }
    }

    @DataProvider(name = "goodRoots")
    public Object[][] getGoodSignatures() {
        return new Object[][]{
                new Object[]{"root({VM1});", 1},
                new Object[]{"root(VM1);", 1},
                new Object[]{"root(VM[1..5]);", 5},
        };
    }

    @Test(dataProvider = "goodRoots")
    public void testGoodSignatures(String str, int nbVMs) throws Exception {
        ScriptBuilder b = new ScriptBuilder(new DefaultModel());
        Set<SatConstraint> cstrs = b.build("namespace test; VM[1..10] : tiny;\n" + str).getConstraints();
        Assert.assertEquals(cstrs.size(), nbVMs);
        Set<VM> vms = new HashSet<>();
        for (SatConstraint x : cstrs) {
            Assert.assertTrue(x instanceof Root);
            Assert.assertTrue(vms.addAll(x.getInvolvedVMs()));
            Assert.assertEquals(x.isContinuous(), true);
        }
    }
}

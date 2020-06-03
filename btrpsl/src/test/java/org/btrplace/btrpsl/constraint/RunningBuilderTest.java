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
import org.btrplace.model.constraint.Running;
import org.btrplace.model.constraint.SatConstraint;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.Set;

/**
 * Unit tests for {@link RunningBuilder}.
 *
 * @author Fabien Hermenier
 */
@Test
public class RunningBuilderTest {

    @DataProvider(name = "badRunnings")
    public Object[][] getBadSignatures() {
        return new String[][]{
                new String[]{">>running({});"},
                new String[]{">>running({@N1});"},
                new String[]{">>running({VM[1..5]});"},
        };
    }

    @Test(dataProvider = "badRunnings", expectedExceptions = {ScriptBuilderException.class})
    public void testBadSignatures(String str) throws ScriptBuilderException {
        ScriptBuilder b = new ScriptBuilder(new DefaultModel());
        try {
            b.build("namespace test; VM[1..10] : tiny;\n@N[1..20] : defaultNode;" + str);
        } catch (ScriptBuilderException ex) {
            System.out.println(str + " " + ex.getMessage());
            throw ex;
        }
    }

    @DataProvider(name = "goodRunnings")
    public Object[][] getGoodSignatures() {
        return new Object[][]{
                new Object[]{">>running(VM1);", 1, false},
                new Object[]{">>running(VM[1..10]);", 10, false}
        };
    }

    @Test(dataProvider = "goodRunnings")
    public void testGoodSignatures(String str, int nbVMs, boolean c) throws Exception {
        ScriptBuilder b = new ScriptBuilder(new DefaultModel());
        Set<SatConstraint> cstrs = b.build("namespace test; VM[1..10] : tiny;\n@N[1..20] : defaultNode;" + str).getConstraints();
        Assert.assertEquals(cstrs.size(), nbVMs);
        Set<VM> vms = new HashSet<>();
        for (SatConstraint x : cstrs) {
            Assert.assertTrue(x instanceof Running);
            Assert.assertTrue(vms.addAll(x.getInvolvedVMs()));
            Assert.assertEquals(x.isContinuous(), c);
        }
    }
}

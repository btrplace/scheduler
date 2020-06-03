/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.btrpsl.constraint;

import org.btrplace.btrpsl.ScriptBuilder;
import org.btrplace.btrpsl.ScriptBuilderException;
import org.btrplace.model.DefaultModel;
import org.btrplace.model.constraint.SplitAmong;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit tests for {@link SplitAmongBuilder}.
 *
 * @author Fabien Hermenier
 */
@Test
public class SplitAmongBuilderTest {

    @DataProvider(name = "badSplitAmongs")
    public Object[][] getBadSignatures() {
        return new String[][]{
                new String[]{">>splitAmong({VM1},{VM2},{VM3});"},
                new String[]{"splitAmong({{VM1}}, {{}});"},
                new String[]{"splitAmong({{}},{@N[1..2],@N[3..5]});"},
                new String[]{"splitAmong({@N[1..5],@N[6..10]},{@VM[1..5],VM[6..10]});"},
        };
    }

    @Test(dataProvider = "badSplitAmongs", expectedExceptions = {ScriptBuilderException.class})
    public void testBadSignatures(String str) throws ScriptBuilderException {
        ScriptBuilder b = new ScriptBuilder(new DefaultModel());
        try {
            b.build("namespace test; VM[1..10] : tiny;\n@N[1..20] : defaultNode;\n" + str);
        } catch (ScriptBuilderException ex) {
            System.out.println(str + " " + ex.getMessage());
            throw ex;
        }
    }

    @DataProvider(name = "goodSplitAmongs")
    public Object[][] getGoodSignatures() {
        return new Object[][]{
                new Object[]{"splitAmong({VM[1..5],VM[6..10]},{@N[1..5],@N[6..10],@N[11..20]});", 2, 10, 3, 20},
                new Object[]{">>splitAmong({VM[1..5],VM[6..10]},{@N[1..5],@N[6..10],@N[11..20]});", 2, 10, 3, 20},
        };
    }

    @Test(dataProvider = "goodSplitAmongs")
    public void testGoodSignatures(String str, int nbVGrp, int nbVMs, int nbPGrp, int nbNodes) throws Exception {
        ScriptBuilder b = new ScriptBuilder(new DefaultModel());
        SplitAmong x = (SplitAmong) b.build("namespace test; VM[1..10] : tiny;\n@N[1..20] : defaultNode;\n" + str).getConstraints().iterator().next();
        Assert.assertEquals(x.getGroupsOfVMs().size(), nbVGrp);
        Assert.assertEquals(x.getInvolvedVMs().size(), nbVMs);
        Assert.assertEquals(x.getGroupsOfNodes().size(), nbPGrp);
        Assert.assertEquals(x.getInvolvedNodes().size(), nbNodes);
        Assert.assertEquals(x.isContinuous(), !str.startsWith(">>"));
    }
}

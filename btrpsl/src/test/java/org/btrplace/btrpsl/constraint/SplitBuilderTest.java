/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.btrpsl.constraint;

import org.btrplace.btrpsl.ScriptBuilder;
import org.btrplace.btrpsl.ScriptBuilderException;
import org.btrplace.model.DefaultModel;
import org.btrplace.model.constraint.Split;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit tests for SplitBuilder.
 *
 * @author Fabien Hermenier
 */
@Test
public class SplitBuilderTest {

    @DataProvider(name = "badSplits")
    public Object[][] getBadSignatures() {
        return new String[][]{
                new String[]{"split({VM1},{VM2},{VM3});"},
                new String[]{"split({VM1},{});"},
                new String[]{"split({},{VM1});"},
                new String[]{"split(@N[1..5],@VM[1..5]);"},
                new String[]{">>split(VM[1..5],@N[1..5]);"},
                new String[]{"split({VM[1..5]},{VM1});"},
                new String[]{"split(VM[1..5],{{VM1}});"},
        };
    }

    @Test(dataProvider = "badSplits", expectedExceptions = {ScriptBuilderException.class})
    public void testBadSignatures(String str) throws ScriptBuilderException {
        ScriptBuilder b = new ScriptBuilder(new DefaultModel());
        try {
            b.build("namespace test; VM[1..10] : tiny;\n@N[1..20] : defaultNode;\n" + str);
        } catch (ScriptBuilderException ex) {
            System.err.println(str + " " + ex.getMessage());
            System.err.flush();
            throw ex;
        }
    }

    @DataProvider(name = "goodSplits")
    public Object[][] getGoodSignatures() {
        return new Object[][]{
                new Object[]{">>split({{VM1},{VM2}});", 1, 1},
                new Object[]{"split({{VM1},{VM2}});", 1, 1},
                new Object[]{">>split({VM[1..5] - {VM2},{VM2}});", 4, 1},
        };
    }

    @Test(dataProvider = "goodSplits")
    public void testGoodSignatures(String str, int nbVMs1, int nbVMs2) throws Exception {
        ScriptBuilder b = new ScriptBuilder(new DefaultModel());
        Split x = (Split) b.build("namespace test; VM[1..10] : tiny;\n@N[1..20] : defaultNode;\n" + str).getConstraints().iterator().next();
        Assert.assertEquals(x.getInvolvedVMs().size(), nbVMs2 + nbVMs1);
        Assert.assertEquals(x.isContinuous(), !str.startsWith(">>"));
    }
}

/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.btrpsl.constraint;

import org.btrplace.btrpsl.ScriptBuilder;
import org.btrplace.btrpsl.ScriptBuilderException;
import org.btrplace.model.DefaultModel;
import org.btrplace.model.constraint.Spread;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit tests for {@link SpreadBuilder}.
 *
 * @author Fabien Hermenier
 */
@Test
public class SpreadBuilderTest {

    @DataProvider(name = "badSpreads")
    public Object[][] getBadSignatures() {
        return new String[][]{
                new String[]{"spread({VM1,VM2},{VM3});"},
                new String[]{"spread({});"},
                new String[]{"spread(@N[1..10]);"},
                new String[]{"spread(VMa);"},
                new String[]{"spread();"},
        };
    }

    @Test(dataProvider = "badSpreads", expectedExceptions = {ScriptBuilderException.class})
    public void testBadSignatures(String str) throws ScriptBuilderException {
        ScriptBuilder b = new ScriptBuilder(new DefaultModel());
        try {
            b.build("namespace test; VM[1..10] : tiny;\n" + str);
        } catch (ScriptBuilderException ex) {
            System.out.println(str + " " + ex.getMessage());
            throw ex;
        }
    }

    @DataProvider(name = "goodSpreads")
    public Object[][] getGoodSignatures() {
        return new Object[][]{
                new Object[]{">>spread({VM1});", 1},
                new Object[]{"spread(VM1);", 1},
                new Object[]{">>spread(VM[1..5]);", 5},
        };
    }

    @Test(dataProvider = "goodSpreads")
    public void testGoodSignatures(String str, int nbVMs) throws Exception {
        ScriptBuilder b = new ScriptBuilder(new DefaultModel());
        Spread x = (Spread) b.build("namespace test; VM[1..10] : tiny;\n" + str).getConstraints().iterator().next();
        Assert.assertEquals(x.getInvolvedVMs().size(), nbVMs);
        Assert.assertEquals(x.isContinuous(), !str.startsWith(">>"));
    }
}

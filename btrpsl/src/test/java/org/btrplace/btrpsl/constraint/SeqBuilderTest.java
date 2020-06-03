/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.btrpsl.constraint;

import org.btrplace.btrpsl.ScriptBuilder;
import org.btrplace.btrpsl.ScriptBuilderException;
import org.btrplace.model.DefaultModel;
import org.btrplace.model.constraint.Seq;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit tests for {@link SeqBuilder}.
 *
 * @author Fabien Hermenier
 */
public class SeqBuilderTest {

    @DataProvider(name = "badseqs")
    public Object[][] getBadSignatures() {
        return new String[][]{
                new String[]{"seq({VM1,VM2},{VM3});"},
                new String[]{"seq({});"},
                new String[]{"seq(@N[1..10]);"},
                new String[]{"seq(VMa);"},
                new String[]{"seq();"},
                new String[]{">>seq(VM[1..5]);"}, //No discrete restriction
        };
    }

    @Test(dataProvider = "badseqs", expectedExceptions = {ScriptBuilderException.class})
    public void testBadSignatures(String str) throws ScriptBuilderException {
        ScriptBuilder b = new ScriptBuilder(new DefaultModel());
        try {
            b.build("namespace test; VM[1..10] : tiny;\n" + str);
        } catch (ScriptBuilderException ex) {
            System.out.println(str + " " + ex.getMessage());
            throw ex;
        }
    }

    @DataProvider(name = "goodseqs")
    public Object[][] getGoodSignatures() {
        return new Object[][]{
                new Object[]{"seq({VM1});", 1},
                new Object[]{"seq(VM1);", 1},
                new Object[]{"seq(VM[1..5]);", 5},
        };
    }

    @Test(dataProvider = "goodseqs")
    public void testGoodSignatures(String str, int nbVMs) throws Exception {
        ScriptBuilder b = new ScriptBuilder(new DefaultModel());
        Seq x = (Seq) b.build("namespace test; VM[1..10] : tiny;\n" + str).getConstraints().iterator().next();
        Assert.assertEquals(x.getInvolvedVMs().size(), nbVMs);
        Assert.assertEquals(x.isContinuous(), true);
    }
}

/*
 * Copyright  2021 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.btrpsl.constraint;

import org.btrplace.btrpsl.ScriptBuilder;
import org.btrplace.btrpsl.ScriptBuilderException;
import org.btrplace.model.DefaultModel;
import org.btrplace.model.constraint.RunningCapacity;
import org.btrplace.model.constraint.SatConstraint;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Set;

/**
 * Unit tests for {@link RunningCapacity}.
 *
 * @author Fabien Hermenier
 */
@Test
public class RunningCapacityBuilderTest {

    @DataProvider(name = "badCapacities")
    public Object[][] getBadSignatures() {
        return new String[][]{
                new String[]{"runningCapacity({@N1,@N2},-1);"},
                new String[]{"runningCapacity({@N1,@N2},1.2);"},
                new String[]{">>runningCapacity({},5);"},
                new String[]{"runningCapacity(@N[1,3,5]);"},
                new String[]{">>runningCapacity(@N[1,3,5,15]);"},
                new String[]{"runningCapacity(VM[1..3],3);"},
                new String[]{"runningCapacity(5);"},
        };
    }

    @Test(dataProvider = "badCapacities", expectedExceptions = {ScriptBuilderException.class})
    public void testBadSignatures(String str) throws ScriptBuilderException {
        ScriptBuilder b = new ScriptBuilder(new DefaultModel());
        try {
            b.build("namespace test; VM[1..10] : tiny;\n@N[1..20] : defaultNode;\n" + str);
        } catch (ScriptBuilderException ex) {
            System.out.println(str + " " + ex.getMessage());
            throw ex;
        }
    }

    @DataProvider(name = "goodCapacities")
    public Object[][] getGoodSignatures() {
        return new Object[][]{
                new Object[]{">>runningCapacity(@N1,3);", 1, 3, false},
                new Object[]{">>runningCapacity(@N[1..3],7-5%2);", 3, 6, false},
        };
    }

    @Test(dataProvider = "goodCapacities")
    public void testGoodSignatures(String str, int nbNodes, int capa, boolean c) throws Exception {
        ScriptBuilder b = new ScriptBuilder(new DefaultModel());
        Set<SatConstraint> cstrs = b.build("namespace test; VM[1..10] : tiny;\n@N[1..20] : defaultNode;\n" + str).getConstraints();
        Assert.assertEquals(cstrs.size(), 1);
        RunningCapacity x = (RunningCapacity) cstrs.iterator().next();
        Assert.assertEquals(x.getInvolvedNodes().size(), nbNodes);
        Assert.assertEquals(x.getAmount(), capa);
        Assert.assertEquals(x.isContinuous(), c);
    }
}

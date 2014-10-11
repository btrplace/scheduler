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

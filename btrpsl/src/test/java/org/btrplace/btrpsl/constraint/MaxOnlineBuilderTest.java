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
import org.btrplace.model.constraint.MaxOnline;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit tests for {@link MaxOnlineBuilder}.
 *
 * @author Fabien Hermenier
 */
public class MaxOnlineBuilderTest {

    @DataProvider(name = "badMaxOnline")
    public Object[][] getBadSignatures() {
        return new String[][]{
                new String[]{"maxOnline({VM1,VM2},{VM3});"},
                new String[]{"maxOnline({});"},
                new String[]{"maxOnline(@N[1..10]);"},
                new String[]{"maxOnline({@N2}, 2.5);"},
                new String[]{"maxOnline({@N2}, -2);"},
                new String[]{"maxOnline();"},
        };
    }

    @Test(dataProvider = "badMaxOnline", expectedExceptions = {ScriptBuilderException.class})
    public void testBadSignatures(String str) throws ScriptBuilderException {
        ScriptBuilder b = new ScriptBuilder(new DefaultModel());
        try {
            b.build("namespace test; VM[1..10] : tiny;\n" + str);
        } catch (ScriptBuilderException ex) {
            System.out.println(str + " " + ex.getMessage());
            throw ex;
        }
    }

    @DataProvider(name = "goodMaxOnline")
    public Object[][] getGoodSignatures() {
        return new Object[][]{
                new Object[]{">>maxOnline(@N1, 5);", 1},
                new Object[]{"maxOnline({@N1}, 10);", 1},
                new Object[]{">>maxOnline(@N[1..5], 3);", 5},
        };
    }

    @Test(dataProvider = "goodMaxOnline")
    public void testGoodSignatures(String str, int nbNodes) throws Exception {
        ScriptBuilder b = new ScriptBuilder(new DefaultModel());
        MaxOnline x = (MaxOnline) b.build("namespace test; VM[1..10] : tiny;\n@N[1..10] : mock;" + str).getConstraints().iterator().next();
        Assert.assertEquals(x.getInvolvedNodes().size(), nbNodes);
        Assert.assertEquals(x.isContinuous(), !str.startsWith(">>"));
    }
}

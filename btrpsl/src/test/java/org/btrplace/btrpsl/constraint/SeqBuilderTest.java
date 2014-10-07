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

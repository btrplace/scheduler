/*
 * Copyright (c) 2013 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package btrplace.btrpsl.constraint;

import btrplace.btrpsl.ScriptBuilder;
import btrplace.btrpsl.ScriptBuilderException;
import btrplace.model.DefaultModel;
import btrplace.model.constraint.SequentialVMTransitions;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit tests for {@link SequentialVMTransitionsBuilder}.
 *
 * @author Fabien Hermenier
 */
public class SequentialVMTransitionsBuilderTest {

    @DataProvider(name = "badSequences")
    public Object[][] getBadSignatures() {
        return new String[][]{
                new String[]{"sequence({VM1,VM2},{VM3});"},
                new String[]{"sequence({});"},
                new String[]{"sequence(@N[1..10]);"},
                new String[]{"sequence(VMa);"},
                new String[]{"sequence();"},
                new String[]{">>sequence(VM[1..5]);"}, //No discrete restriction
        };
    }

    @Test(dataProvider = "badSequences", expectedExceptions = {ScriptBuilderException.class})
    public void testBadSignatures(String str) throws ScriptBuilderException {
        ScriptBuilder b = new ScriptBuilder(new DefaultModel());
        try {
            b.build("namespace test; VM[1..10] : tiny;\n" + str);
        } catch (ScriptBuilderException ex) {
            System.out.println(str + " " + ex.getMessage());
            throw ex;
        }
    }

    @DataProvider(name = "goodSequences")
    public Object[][] getGoodSignatures() {
        return new Object[][]{
                new Object[]{"sequence({VM1});", 1},
                new Object[]{"sequence(VM1);", 1},
                new Object[]{"sequence(VM[1..5]);", 5},
        };
    }

    @Test(dataProvider = "goodSequences")
    public void testGoodSignatures(String str, int nbVMs) throws Exception {
        ScriptBuilder b = new ScriptBuilder(new DefaultModel());
        SequentialVMTransitions x = (SequentialVMTransitions) b.build("namespace test; VM[1..10] : tiny;\n" + str).getConstraints().iterator().next();
        Assert.assertEquals(x.getInvolvedVMs().size(), nbVMs);
        Assert.assertEquals(x.isContinuous(), true);
    }
}

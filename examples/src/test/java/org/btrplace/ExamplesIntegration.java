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

package org.btrplace;

import org.btrplace.examples.*;
import org.btrplace.examples.migration.AdvancedMigScheduling;
import org.btrplace.examples.migration.NetworkAndMigrations;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Launch the examples and check for their termination.
 *
 * @author Fabien Hermenier
 */
public class ExamplesIntegration {

    @Test
    public void testGettingStarted() throws Exception {
        Example ex = new GettingStarted();
        Assert.assertTrue(ex.run(), "Example " + ex.toString() + " failed");
    }

    @Test
    public void testSolvingTuning() throws Exception {
        Example ex = new SolverTuning();
        Assert.assertTrue(ex.run(), "Example " + ex.toString() + " failed");
    }

    @Test
    public void testModelCustomization() throws Exception {
        Example ex = new ModelCustomization();
        Assert.assertTrue(ex.run(), "Example " + ex.toString() + " failed");
    }

    @Test
    public void testDecommissioning() throws Exception {
        Example ex = new Decommissionning();
        Assert.assertTrue(ex.run(), "Example " + ex.toString() + " failed");
    }

    @Test
    public void testNetworkAndMigrations() throws Exception {
        Example ex = new NetworkAndMigrations();
        Assert.assertTrue(ex.run(), "Example " + ex.toString() + " failed");
    }

    @Test
    public void testAdvancedMigScheduling() throws Exception {
        Example ex = new AdvancedMigScheduling();
        Assert.assertTrue(ex.run(), "Example " + ex.toString() + " failed");
    }
}

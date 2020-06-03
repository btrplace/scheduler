/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.examples;

import org.btrplace.examples.migration.AdvancedMigScheduling;
import org.btrplace.examples.migration.NetworkAndMigrations;
import org.testng.annotations.Test;

/**
 * Launch the examples and check for their termination.
 *
 * @author Fabien Hermenier
 */
public class ExamplesIntegration {

    @Test
    public void testGettingStarted() {
        Example ex = new GettingStarted();
        ex.run();
    }

    @Test
    public void testSolvingTuning() {
        Example ex = new SolverTuning();
        ex.run();
    }

    @Test
    public void testModelCustomization() {
        Example ex = new ModelCustomization();
        ex.run();
    }

    @Test
    public void testDecommissioning() {
        Example ex = new Decommissionning();
        ex.run();
    }

    @Test
    public void testNetworkAndMigrations() {
        Example ex = new NetworkAndMigrations();
        ex.run();
    }

    @Test
    public void testAdvancedMigScheduling() {
        Example ex = new AdvancedMigScheduling();
        ex.run();
    }
}

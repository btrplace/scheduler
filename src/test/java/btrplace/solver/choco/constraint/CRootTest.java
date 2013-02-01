/*
 * Copyright (c) 2012 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
 *
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

package btrplace.solver.choco.constraint;

import btrplace.model.DefaultModel;
import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.SatConstraint;
import btrplace.model.constraint.Online;
import btrplace.model.constraint.Root;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoReconfigurationAlgorithm;
import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithm;
import btrplace.solver.choco.MappingBuilder;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for {@link CRoot}.
 *
 * @author Fabien Hermenier
 */
public class CRootTest extends ConstraintTestMaterial {

    @Test
    public void testBasic() throws SolverException {
        Mapping map = new MappingBuilder().on(n1, n2).run(n1, vm1, vm2).ready(vm3).build();

        Model mo = new DefaultModel(map);

        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.repair(false);
        cra.labelVariables(true);
        Root r1 = new Root(map.getAllVMs());
        Online n = new Online(map.getAllNodes());
        List<SatConstraint> l = new ArrayList<SatConstraint>();
        l.add(r1);
        l.add(n);
        ReconfigurationPlan p = cra.solve(mo, l);
        Assert.assertNotNull(p);
        Model res = p.getResult();
        Assert.assertEquals(n1, res.getMapping().getVMLocation(vm1));
    }
}

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

package btrplace.safeplace.verification;

import btrplace.model.Model;
import btrplace.plan.ReconfigurationPlan;
import btrplace.safeplace.Constraint;
import btrplace.safeplace.spec.term.Constant;

import java.util.List;

/**
 * @author Fabien Hermenier
 */
public interface Verifier {

    CheckerResult verify(Constraint c, List<Constant> params, Model dst, Model src);

    CheckerResult verify(Constraint c, List<Constant> params, ReconfigurationPlan p);
}

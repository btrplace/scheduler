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

package org.btrplace.plan;

import org.btrplace.model.Model;
import org.btrplace.model.VM;
import org.btrplace.plan.event.Action;
import org.btrplace.plan.event.ActionVisitor;

/**
 * A fake action for test purposes.
 *
 * @author Fabien Hermenier
 */
public class MockAction extends Action {

    public int count = 0;

    public VM u;

    public MockAction(VM u, int st, int ed) {
        super(st, ed);
        this.u = u;
    }

    @Override
    public boolean applyAction(Model i) {
        count++;
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) {
            return false;
        }
        MockAction that = (MockAction) o;

        return count == that.count && u.equals(that.u);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + count;
        result = 31 * result + u.hashCode();
        return result;
    }

    @Override
    public String pretty() {
        return "pretty(" + u + ", start= " + getStart() + ", end=" + getEnd() + ")";
    }

    @Override
    public Object visit(ActionVisitor v) {
        count++;
        return true;
    }
}

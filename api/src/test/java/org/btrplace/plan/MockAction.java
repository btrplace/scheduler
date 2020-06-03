/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
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

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

package btrplace.solver.choco.view;

import btrplace.model.VM;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ReconfigurationProblem;

import java.util.*;

/**
 * @author Fabien Hermenier
 */
public class SolverViewsManager {

    private ReconfigurationProblem rp;

    private List<ChocoView> workflow;

    private Map<String, ChocoView> views;

    public SolverViewsManager(ReconfigurationProblem p) throws SolverException {
        this.rp = p;
        workflow = new ArrayList<>();
        this.views = new HashMap<>();
    }


    public void build(List<SolverViewBuilder> vs) throws SolverException {
        Set<String> done = new HashSet<>();
        List<SolverViewBuilder> remaining = new ArrayList<>(vs);

        while (!remaining.isEmpty()) {
            ListIterator<SolverViewBuilder> ite = remaining.listIterator();
            boolean blocked = true;
            while (ite.hasNext()) {
                SolverViewBuilder s = ite.next();
                if (done.containsAll(s.getDependencies())) {
                    ChocoView v = s.build(rp);
                    workflow.add(v);
                    views.put(v.getIdentifier(), v);
                    ite.remove();
                    done.add(s.getKey());
                    blocked = false;
                }
            }
            if (blocked) {
                throw new SolverException(rp.getSourceModel(), "Cyclic dependencies among the following views: " + remaining);
            }
        }
    }

    public boolean beforeSolve() throws SolverException {
        ListIterator<ChocoView> l = workflow.listIterator(workflow.size());
        while (l.hasPrevious()) {
            ChocoView v = l.previous();
            if (!v.beforeSolve(rp)) {
                return false;
            }
        }
        return true;
    }

    public boolean insertActions(ReconfigurationPlan p) throws SolverException {
        for (ChocoView v : workflow) {
            v.insertActions(rp, p);
        }
        return true;
    }

    public void cloneVM(VM vm, VM newVM) {
        for (ChocoView v : workflow) {
            v.cloneVM(vm, newVM);
        }
    }

    public boolean add(ChocoView v) {
        if (views.put(v.getIdentifier(), v) == null) {
            workflow.add(v);
            return true;
        }
        return false;
    }

    public Set<String> getKeys() {
        return views.keySet();
    }

    public ChocoView get(String id) {
        return views.get(id);
    }
}

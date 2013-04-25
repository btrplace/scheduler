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

package btrplace.solver.choco;

import btrplace.model.Model;
import btrplace.model.SatConstraint;
import btrplace.model.constraint.Killed;
import btrplace.model.constraint.Ready;
import btrplace.model.constraint.Running;
import btrplace.model.constraint.Sleeping;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.ReconfigurationPlanApplier;
import btrplace.plan.ReconfigurationPlanValidator;
import btrplace.solver.SolverException;
import btrplace.solver.choco.constraint.SatConstraintMapper;
import btrplace.solver.choco.objective.minMTTR.MinMTTR;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.common.logging.Verbosity;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solution;
import choco.kernel.solver.search.measure.IMeasures;

import java.util.*;

/**
 * Default implementation of {@link ChocoReconfigurationAlgorithm}.
 *
 * @author Fabien Hermenier
 */
public class DefaultChocoReconfigurationAlgorithm implements ChocoReconfigurationAlgorithm {

    private ModelViewMapper viewMapper;

    private SatConstraintMapper cstrMapper;

    private boolean optimize = false;

    /**
     * No time limit by default.
     */
    private int timeLimit = 0;

    private boolean repair = false;

    private boolean useLabels = false;

    private ReconfigurationProblem rp;

    private Collection<SatConstraint> cstrs;

    private DurationEvaluators durationEvaluators;

    private ReconfigurationObjective obj;

    private int maxEnd = DefaultReconfigurationProblem.DEFAULT_MAX_TIME;

    private long coreRPDuration;

    private long speRPDuration;

    /**
     * Make a new algorithm.
     */
    public DefaultChocoReconfigurationAlgorithm() {

        cstrMapper = new SatConstraintMapper();
        durationEvaluators = new DurationEvaluators();
        viewMapper = new ModelViewMapper();

        //Default objective
        obj = new MinMTTR();
    }

    @Override
    public void doOptimize(boolean b) {
        this.optimize = b;
    }

    @Override
    public boolean doOptimize() {
        return this.optimize;
    }

    @Override
    public void setTimeLimit(int t) {
        timeLimit = t;
    }

    @Override
    public int getTimeLimit() {
        return timeLimit;
    }

    @Override
    public void repair(boolean b) {
        repair = b;
    }

    @Override
    public boolean repair() {
        return repair;
    }


    @Override
    public void labelVariables(boolean b) {
        useLabels = b;
    }

    @Override
    public boolean areVariablesLabelled() {
        return useLabels;
    }

    @Override
    public ReconfigurationPlan solve(Model i, Collection<SatConstraint> cstrs) throws SolverException {
        rp = null;
        this.cstrs = cstrs;
        coreRPDuration = -System.currentTimeMillis();
        //Build the RP. As VM state management is not possible
        //We extract VM-state related constraints first.
        //For other constraint, we just create the right choco constraint
        Set<UUID> toRun = new HashSet<UUID>();
        Set<UUID> toForge = new HashSet<UUID>();
        Set<UUID> toKill = new HashSet<UUID>();
        Set<UUID> toSleep = new HashSet<UUID>();

        List<ChocoSatConstraint> cConstraints = new ArrayList<ChocoSatConstraint>();
        for (SatConstraint cstr : cstrs) {
            if (cstr instanceof Running) {
                toRun.addAll(cstr.getInvolvedVMs());
            } else if (cstr instanceof Sleeping) {
                toSleep.addAll(cstr.getInvolvedVMs());
            } else if (cstr instanceof Ready) {
                toForge.addAll(cstr.getInvolvedVMs());
            } else if (cstr instanceof Killed) {
                toKill.addAll(cstr.getInvolvedVMs());
            }

            ChocoSatConstraintBuilder ccstrb = cstrMapper.getBuilder(cstr.getClass());
            if (ccstrb == null) {
                throw new SolverException(i, "Unable to map constraint '" + cstr.getClass().getSimpleName() + "'");
            }
            ChocoSatConstraint ccstr = ccstrb.build(cstr);
            if (ccstr == null) {
                throw new SolverException(i, "Error while mapping the constraint '"
                        + cstr.getClass().getSimpleName() + "'");
            }
            cConstraints.add(ccstr);
        }

        //Make the core-RP
        DefaultReconfigurationProblemBuilder rpb = new DefaultReconfigurationProblemBuilder(i)
                .setNextVMsStates(toForge, toRun, toSleep, toKill)
                .setViewMapper(viewMapper)
                .setDurationEvaluatators(durationEvaluators);
        if (repair) {
            Set<UUID> toManage = new HashSet<UUID>();
            for (ChocoSatConstraint cstr : cConstraints) {
                toManage.addAll(cstr.getMisPlacedVMs(i));
            }
            toManage.addAll(obj.getMisPlacedVMs(i));
            rpb.setManageableVMs(toManage);
        }
        if (useLabels) {
            rpb.labelVariables();
        }
        rp = rpb.build();

        //Set the maximum duration
        try {
            rp.getEnd().setSup(maxEnd);
        } catch (ContradictionException e) {
            rp.getLogger().error("Unable to restrict the maximum plan duration to {}", maxEnd);
            return null;
        }
        coreRPDuration += System.currentTimeMillis();

        //Customize with the constraints
        speRPDuration = -System.currentTimeMillis();
        for (ChocoSatConstraint ccstr : cConstraints) {
            if (!ccstr.inject(rp)) {
                return null;
            }
        }

        //The objective
        obj.inject(rp);
        speRPDuration += System.currentTimeMillis();
        rp.getLogger().debug("{} ms to build the core-RP + {} ms to tune it", coreRPDuration, speRPDuration);

        rp.getLogger().debug("{} nodes; {} VMs; {} constraints", rp.getNodes().length, rp.getVMs().length, cstrs.size());
        rp.getLogger().debug("optimize: {}; timeLimit: {}; manageableVMs: {}", optimize, getTimeLimit(), rp.getManageableVMs().size());

        ReconfigurationPlan p = rp.solve(timeLimit, optimize);
        if (p != null) {
            //assert checkSatisfaction(p, cstrs);
            assert checkSatisfaction2(p, cstrs);
            return p;
        } else {
            return null;
        }
    }

    private boolean checkSatisfaction(ReconfigurationPlan p, Collection<SatConstraint> cstrs) {
        Model res = p.getResult();
        if (res == null) {
            rp.getLogger().error("Applying the following plan does not conclude to a model:\n{}", p);
            return false;
        }
        for (SatConstraint cstr : cstrs) {
            if (cstr.isContinuous() && !cstr.isSatisfied(p).equals(SatConstraint.Sat.SATISFIED)) {
                rp.getLogger().error("The following plan does not satisfy {}:\n{}", cstr.toString(), p);
                return false;
            } else if (!cstr.isContinuous() && !cstr.isSatisfied(res).equals(SatConstraint.Sat.SATISFIED)) {
                rp.getLogger().error("The following model does not satisfy {}:\n{}", cstr.toString(), res);
                return false;
            }

        }
        return true;
    }

    private boolean checkSatisfaction2(ReconfigurationPlan p, Collection<SatConstraint> cstrs) {
        System.err.println(p);
        ReconfigurationPlanApplier applier = p.getReconfigurationApplier();
        List<ReconfigurationPlanValidator> validators = new ArrayList<>();
        for (SatConstraint cstr : cstrs) {
            validators.add(cstr.getValidator());
        }
        for (ReconfigurationPlanValidator v : validators) {
            applier.addValidator(v);
        }
        Model mo = applier.apply(p);
        if (mo == null) {
            return false;
        }
        for (ReconfigurationPlanValidator v : validators) {
            if (!v.acceptResultingModel(mo)) {
                return false;
            }
            applier.removeValidator(v);
        }
        return true;
    }

    @Override
    public SatConstraintMapper getSatConstraintMapper() {
        return cstrMapper;
    }

    @Override
    public DurationEvaluators getDurationEvaluators() {
        return durationEvaluators;
    }

    @Override
    public ReconfigurationObjective getObjective() {
        return obj;
    }

    @Override
    public void setObjective(ReconfigurationObjective o) {
        obj = o;
    }

    @Override
    public SolvingStatistics getSolvingStatistics() {
        if (rp == null) {
            return new SolvingStatistics(0, 0, 0, optimize, getTimeLimit(), 0, 0, 0, 0, false, 0, 0);
        }
        SolvingStatistics st = new SolvingStatistics(
                rp.getNodes().length,
                rp.getVMs().length,
                cstrs.size(),
                optimize,
                getTimeLimit(),
                rp.getManageableVMs().size(),
                rp.getSolver().getTimeCount(),
                rp.getSolver().getNodeCount(),
                rp.getSolver().getBackTrackCount(),
                rp.getSolver().isEncounteredLimit(),
                coreRPDuration,
                speRPDuration);

        for (Solution s : rp.getSolver().getSearchStrategy().getStoredSolutions()) {
            IMeasures m = s.getMeasures();
            SolutionStatistics sol;
            if (m.getObjectiveValue() != null) {
                sol = new SolutionStatistics(m.getNodeCount(),
                        m.getBackTrackCount(),
                        m.getTimeCount(),
                        m.getObjectiveValue().intValue());
            } else {
                sol = new SolutionStatistics(m.getNodeCount(),
                        m.getBackTrackCount(),
                        m.getTimeCount());
            }
            st.addSolution(sol);

        }
        return st;
    }

    @Override
    public void setMaxEnd(int end) {
        this.maxEnd = end;
    }

    @Override
    public int getMaxEnd() {
        return this.maxEnd;
    }

    @Override
    public ModelViewMapper getViewMapper() {
        return viewMapper;
    }

    @Override
    public void setViewMapper(ModelViewMapper m) {
        viewMapper = m;
    }

    @Override
    public void setVerbosity(int lvl) {
        if (lvl <= 0) {
            ChocoLogging.setVerbosity(Verbosity.SILENT);
            labelVariables(false);
        } else {
            labelVariables(true);
            ChocoLogging.setVerbosity(Verbosity.SOLUTION);
            if (lvl == 2) {
                ChocoLogging.setVerbosity(Verbosity.SEARCH);
                ChocoLogging.setLoggingMaxDepth(Integer.MAX_VALUE);
            } else if (lvl > 2) {
                ChocoLogging.setVerbosity(Verbosity.FINEST);
            }
        }
    }
}

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

package btrplace.solver.choco.actionModel;

/**
 * A visitor for each possible {@link btrplace.solver.choco.ActionModel}.
 *
 * @author Fabien Hermenier
 */
public interface ActionModelVisitor {

    /**
     * Visit a {@link BootVMModel}.
     *
     * @param a the action to visit
     */
    void visit(BootVMModel a);

    /**
     * Visit a {@link ShutdownVMModel}.
     *
     * @param a the action to visit
     */
    void visit(ShutdownVMModel a);

    /**
     * Visit a {@link BootableNodeModel}.
     *
     * @param a the action to visit
     */
    void visit(BootableNodeModel a);

    /**
     * Visit a {@link ShutdownableNodeModel}.
     *
     * @param a the action to visit
     */
    void visit(ShutdownableNodeModel a);

    /**
     * Visit a {@link RelocatableVMModel}.
     *
     * @param a the action to visit
     */
    void visit(RelocatableVMModel a);

    /**
     * Visit a {@link ResumeVMModel}.
     *
     * @param a the action to visit
     */
    void visit(ResumeVMModel a);

    /**
     * Visit a {@link SuspendVMModel}.
     *
     * @param a the action to visit
     */
    void visit(SuspendVMModel a);

    /**
     * Visit a {@link ForgeVMModel}.
     *
     * @param a the action to visit
     */
    void visit(ForgeVMModel a);

    /**
     * Visit a {@link StayRunningVMModel}.
     *
     * @param a the action to visit
     */
    void visit(StayRunningVMModel a);

    /**
     * Visit a {@link StayAwayVMModel}.
     *
     * @param a the action to visit
     */
    void visit(StayAwayVMModel a);

    /**
     * Visit a {@link KillVMActionModel}.
     *
     * @param a the action to visit
     */
    void visit(KillVMActionModel a);

}

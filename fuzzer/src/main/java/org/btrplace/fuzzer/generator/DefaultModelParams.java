/*
 * Copyright (c) 2016 University Nice Sophia Antipolis
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

package org.btrplace.fuzzer.generator;

/**
 * @author Fabien Hermenier
 */
public class DefaultModelParams implements ModelParams {

    private int nbVMs = 3;

    private int nbNodes = 3;

    @Override
    public int vms() {
        return nbVMs;
    }

    @Override
    public int nodes() {
        return nbNodes;
    }

    @Override
    public ModelParams vms(int nb) {
        nbVMs = nb;
        return this;
    }

    @Override
    public ModelParams nodes(int nb) {
        nbNodes = nb;
        return this;
    }
}

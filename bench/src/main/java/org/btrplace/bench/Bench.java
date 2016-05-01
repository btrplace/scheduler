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

package org.btrplace.bench;

import org.btrplace.scheduler.choco.ChocoScheduler;
import org.btrplace.scheduler.choco.DefaultChocoScheduler;
import org.btrplace.scheduler.choco.Parameters;
import org.btrplace.scheduler.choco.runner.SolutionStatistics;
import org.btrplace.scheduler.choco.runner.SolvingStatistics;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class Bench {

    public static void main(String[] args) throws IOException {
        Options opts = new Options();

        // Parse the cmdline arguments
        CmdLineParser cli = new CmdLineParser(opts);
        try {
            cli.getProperties().withUsageWidth(80);
            cli.parseArgument(args);
        } catch (CmdLineException ex) {
            System.err.println(ex.getMessage());
            cli.printUsage(System.err);
            System.exit(1);
        }

        Parameters ps = opts.parameters();
        ChocoScheduler s = new DefaultChocoScheduler().setParameters(ps);

        File output = opts.output();
        int nb = 0;
        List<LabelledInstance> instances = opts.instances();
        for (LabelledInstance i : instances) {
            s.solve(i);
            if (opts.showProgress()) {
                System.out.println("------ " + nb + "/" + instances.size() + ": " + i.label + " ------");
                System.out.println(s.getStatistics());
            }
            store(i, s.getStatistics(), output);
        }

    }

    private static void store(LabelledInstance i, SolvingStatistics stats, File output) {
        for (SolutionStatistics sol : stats.getSolutions()) {
            //id,nbNodes,nbVMs,nbManagedVMs,nbConstraints,int_vars,cstr,bool_vars,cstrs,core,spe,dur,search_nodes,backtracks,opt
        }
    }
}

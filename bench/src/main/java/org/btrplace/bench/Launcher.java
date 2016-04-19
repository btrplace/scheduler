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

package org.btrplace.bench;

import org.btrplace.json.model.InstanceConverter;
import org.btrplace.model.Instance;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.ChocoScheduler;
import org.btrplace.scheduler.choco.DefaultChocoScheduler;
import org.btrplace.scheduler.choco.runner.SolvingStatistics;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

/**
 * Created by vkherbac on 16/09/14.
 */
public class Launcher {

    // Define options list
    @Option(name = "-r", aliases = "--repair", usage = "Enable the 'repair' feature")
    private boolean repair;
    @Option(name = "-m", aliases = "--optimize", usage = "Enable the 'optimize' feature")
    private boolean optimize;
    @Option(name = "-t", aliases = "--timeout", usage = "Set a timeout (in sec)")
    private int timeout = 0; //5min by default
    @Option(required = true, name = "-i", aliases = "--input-json", usage = "the json instance file to read (can be a .gz)")
    private String src;
    @Option(required = true, name = "-o", aliases = "--output", usage = "Output to this file")
    private String dst;

    public static void main(String[] args) throws IOException {
        new Launcher().parseArgs(args);
    }

    public void parseArgs(String[] args) throws IOException {

        // Parse the cmdline arguments
        CmdLineParser cmdParser = new CmdLineParser(this);
        cmdParser.getProperties().withUsageWidth(80);
        try {
            cmdParser.parseArgument(args);
            if (timeout < 0)
                throw new IllegalArgumentException("Timeout can not be < 0 !");
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            System.err.println("benchLauncher [-r] [-m] [-t n_sec] -i file_name -o file_name");
            cmdParser.printUsage(System.err);
            System.err.println();
            return;
        }

        launch(repair, optimize, timeout, src, dst);
    }

    public static void launch(boolean repair, boolean optimize, int timeout, String src, String dst) throws IOException {

        // Create and customize a scheduler
        ChocoScheduler cra = new DefaultChocoScheduler();
        ReconfigurationPlan plan = null;

        // Manage options behaviors
        cra.doRepair(repair);
        cra.doOptimize(optimize);
        cra.setTimeLimit(timeout);

        Instance i = InstanceConverter.quickFromJSON(src);
        // Try to solve
        try {
            // For debug purpose
            cra.setVerbosity(2);
            plan = cra.solve(i.getModel(), i.getSatConstraints());
            if (plan == null) {
                throw new RuntimeException("No solution !");
            }
        } finally {
            System.out.println(cra.getStatistics());
        }

        // Save stats to a CSV file
        createCSV(dst, plan, cra);

        //Save the plan
        savePlan(stripExtension(dst) + ".plan", plan);
    }

    public static void savePlan(String fileName, ReconfigurationPlan plan) throws IOException {
        // Write the plan in a specific file
        try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), StandardCharsets.UTF_8))) {
            out.append(plan.toString());
        }
    }

    public static void createCSV(String fileName, ReconfigurationPlan plan, ChocoScheduler cra) throws IOException, SchedulerException {

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), StandardCharsets.UTF_8))) {
            SolvingStatistics stats = cra.getStatistics();

            // Set header
            if (plan != null) {
                writer.append("planDuration;planSize;planActionsSize;");
            }
            if (stats != null) {
                writer.append("craStart;craNbSolutions;");
                if (!stats.getSolutions().isEmpty()) {
                    writer.append("craSolutionTime;");
                }
                writer.append("craCoreRPBuildDuration;")
                        .append("craSpeRPDuration;")
                        .append("craSolvingDuration;")
                        .append("craNbBacktracks;")
                        .append("craNbConstraints;")
                        .append("craNbManagedVMs;")
                        .append("craNbNodes;")
                        .append("craNbSearchNodes;")
                        .append("craNbVMs")
                        .append('\n');
            }

            // Store values
            if (plan != null) {
                writer.append(String.valueOf(plan.getDuration()))
                        .append(';')
                        .append(String.valueOf(plan.getSize()))
                        .append(';')
                        .append(String.valueOf(plan.getActions().size()))
                        .append(';');
            }
            if (stats != null) {
                writer.append(String.valueOf(stats.getStart()))
                        .append(';')
                        .append(String.valueOf(stats.getSolutions().size()));
                if (!stats.getSolutions().isEmpty()) {
                    writer.append(String.valueOf(stats.getSolutions().get(0).getTime()))
                            .append(';');
                }
                writer.append(String.valueOf(stats.getCoreRPBuildDuration()))
                        .append(';')
                        .append(String.valueOf(stats.getSpeRPDuration()))
                        .append(';')
                        .append(String.valueOf(stats.getSolvingDuration()))
                        .append(';')
                        .append(String.valueOf(stats.getNbBacktracks()))
                        .append(';')
                        .append(String.valueOf(stats.getNbConstraints()))
                        .append(';')
                        .append(String.valueOf(stats.getNbManagedVMs()))
                        .append(';')
                        .append(String.valueOf(stats.getNbNodes()))
                        .append(';')
                        .append(String.valueOf(stats.getNbSearchNodes()))
                        .append(';')
                        .append(String.valueOf(stats.getNbVMs()))
                        .append('\n');
            }
        }
    }

    public static String stripExtension(final String s) {
        return s != null && s.lastIndexOf(".") > 0 ? s.substring(0, s.lastIndexOf(".")) : s;
    }
}

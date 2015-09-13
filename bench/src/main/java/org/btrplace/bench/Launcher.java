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

import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.btrplace.json.JSONConverterException;
import org.btrplace.json.model.InstanceConverter;
import org.btrplace.model.Attributes;
import org.btrplace.model.Instance;
import org.btrplace.model.VM;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.plan.event.MigrateVM;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.ChocoScheduler;
import org.btrplace.scheduler.choco.DefaultChocoScheduler;
import org.btrplace.scheduler.choco.duration.DurationEvaluators;
import org.btrplace.scheduler.choco.duration.LinearToAResourceActionDuration;
import org.btrplace.scheduler.choco.runner.SolvingStatistics;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.*;
import java.util.zip.GZIPInputStream;

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

    public void parseArgs(String[] args) {

        // Parse the cmdline arguments
        CmdLineParser cmdParser = new CmdLineParser(this);
        cmdParser.setUsageWidth(80);
        try {
            cmdParser.parseArgument(args);
            if (timeout < 0)
                throw new CmdLineException("Timeout can not be < 0 !");
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            System.err.println("benchLauncher [-r] [-m] [-t n_sec] -i file_name -o file_name");
            cmdParser.printUsage(System.err);
            System.err.println();
            return;
        }

        launch(repair, optimize, timeout, src, dst);
    }

    public static void launch(boolean repair, boolean optimize, int timeout, String src, String dst) {

        // Create and customize a scheduler
        ChocoScheduler cra = new DefaultChocoScheduler();
        ReconfigurationPlan plan = null;

        // Manage options behaviors
        if (repair) {
            cra.doRepair(true);
        } else {
            cra.doRepair(false);
        }
        if (optimize) {
            cra.doOptimize(true);
        } else {
            cra.doOptimize(false);
        }
        if (timeout > 0) {
            cra.setTimeLimit(timeout);
        }

        // Read the input JSON instance
        JSONParser parser = new JSONParser(JSONParser.DEFAULT_PERMISSIVE_MODE);
        Object obj = null;
        try {
            // Check for gzip extension
            if (src.endsWith(".gz")) {
                obj = parser.parse(new InputStreamReader(new GZIPInputStream(new FileInputStream(src))));
            } else {
                obj = parser.parse(new FileReader(src));
            }

        } catch (ParseException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        JSONObject o = (JSONObject) obj;

        // Convert the json object to an instance
        InstanceConverter conv = new InstanceConverter();
        Instance i = null;
        try {
            i = conv.fromJSON(o);
        } catch (JSONConverterException e) {
            e.printStackTrace();
        }

        //Set custom actions durations
        setAttributes(i, cra.getDurationEvaluators());

        // Try to solve
        try {
            // For debug purpose
            cra.setVerbosity(2);
            plan = cra.solve(i.getModel(), i.getSatConstraints());
            if (plan == null) {
                System.err.println("No solution !");
                throw new RuntimeException();
            }
        } catch (SchedulerException e) {
            e.printStackTrace();
        } finally {
            try {
                System.out.println(cra.getStatistics());
            } catch (SchedulerException ex) {
                ex.printStackTrace();
            }
        }

        // Save stats to a CSV file
        try {
            createCSV(dst, plan, cra);
        } catch (IOException | SchedulerException e) {
            e.printStackTrace();
        }

        //Save the plan
        if (plan != null) {
            try {
                savePlan(stripExtension(dst) + ".plan", plan);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void setAttributes(Instance i, DurationEvaluators dev) {

        Attributes attrs = i.getModel().getAttributes();
        for (VM vm : i.getModel().getMapping().getAllVMs()) {
            // Hypervisor
            //attrs.put(vm, "template", "kvm");

            // Can be re-instantiated
            attrs.put(vm, "clone", true);

            // Migration duration: Memory/100
            dev.register(MigrateVM.class, new LinearToAResourceActionDuration<VM>("memory", 0.01));

            // Actions
            attrs.put(vm, "forge", 3);
            attrs.put(vm, "kill", 2);
            //attrs.put(vm, "boot", 5);
            //attrs.put(vm, "shutdown", 2);
            //attrs.put(vm, "suspend", 4);
            //attrs.put(vm, "resume", 5);
            //attrs.put(vm, "allocate", 5);
        }
        /*for (Node n : i.getModel().getMapping().getAllNodes()) {
            attrs.put(n, "boot", 6);
            attrs.put(n, "shutdown", 6);
        }*/
    }

    public static void savePlan(String fileName, ReconfigurationPlan plan) throws IOException {
        // Write the plan in a specific file
        FileWriter writerPlan = new FileWriter(fileName);
        writerPlan.append(plan.toString());
        writerPlan.flush();
        writerPlan.close();
    }

    public static void createCSV(String fileName, ReconfigurationPlan plan, ChocoScheduler cra) throws IOException, SchedulerException {

        FileWriter writer = new FileWriter(fileName);
        SolvingStatistics stats = cra.getStatistics();

        // Set header
        if (plan != null) {
            writer.append("planDuration;planSize;planActionsSize;");
        }
        if (stats != null) {
            writer.append("craStart;craNbSolutions;");
            if (stats.getSolutions().size() > 0) {
                writer.append("craSolutionTime;");
            }
            writer.append("craCoreRPBuildDuration;" +
                            "craSpeRPDuration;" +
                            "craSolvingDuration;" +
                            "craNbBacktracks;" +
                            "craNbConstraints;" +
                            "craNbManagedVMs;" +
                            "craNbNodes;" +
                            "craNbSearchNodes;" +
                            "craNbVMs" + '\n'
            );
        }

        // Store values
        if (plan != null) {
            writer.append(String.valueOf(plan.getDuration()) + ';' +
                            String.valueOf(plan.getSize()) + ';' +
                            String.valueOf(plan.getActions().size()) + ';'
            );
        }
        if (stats != null) {
            writer.append(String.valueOf(stats.getStart()) + ';' +
                            String.valueOf(stats.getSolutions().size()) + ';'
            );
            if (stats.getSolutions().size() > 0) {
                writer.append(String.valueOf(stats.getSolutions().get(0).getTime()) + ';');
            }
            writer.append(String.valueOf(stats.getCoreRPBuildDuration()) + ';' +
                            String.valueOf(stats.getSpeRPDuration()) + ';' +
                            String.valueOf(stats.getSolvingDuration()) + ';' +
                            String.valueOf(stats.getNbBacktracks()) + ';' +
                            String.valueOf(stats.getNbConstraints()) + ';' +
                            String.valueOf(stats.getNbManagedVMs()) + ';' +
                            String.valueOf(stats.getNbNodes()) + ';' +
                            String.valueOf(stats.getNbSearchNodes()) + ';' +
                            String.valueOf(stats.getNbVMs()) + '\n'
            );
        }

        // Close the file
        writer.flush();
        writer.close();
    }

    public static String stripExtension(final String s) {
        return s != null && s.lastIndexOf(".") > 0 ? s.substring(0, s.lastIndexOf(".")) : s;
    }
}

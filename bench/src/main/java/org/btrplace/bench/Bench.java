/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.bench;

import org.btrplace.json.JSON;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.UnstatableProblemException;
import org.btrplace.scheduler.choco.ChocoScheduler;
import org.btrplace.scheduler.choco.DefaultChocoScheduler;
import org.btrplace.scheduler.choco.Parameters;
import org.btrplace.scheduler.choco.runner.SolvingStatistics;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Iterator;
import java.util.UUID;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

/**
 * Simple benching tool.
 * @author Fabien Hermenier
 */
@SuppressWarnings("squid:S106")
public class Bench {

    /**
     * The CSV file that will contains the output statistics.
     */
    public static final String SCHEDULER_STATS = "scheduler.csv";

    private static Options opts;
    private Bench() {
    }

    /**
     * Launcher
     *
     * @param args the CLI arguments
     * @throws IOException
     */
    @SuppressWarnings("squid:S1166")
    public static void main(String[] args) throws IOException {
        opts = new Options();

        // Parse the cmdline arguments
        CmdLineParser cli = new CmdLineParser(opts);
        try {
            cli.getProperties().withUsageWidth(80);
            cli.parseArgument(args);
        } catch (@SuppressWarnings("unused") CmdLineException ex) {
            cli.printUsage(System.err);
            System.exit(1);
        }

        Runtime runtime = Runtime.getRuntime();
        int mb = 1024 * 1024;
        out(1, "Total Memory: %d%n", runtime.totalMemory() / mb);
        out(1, "Max Memory: %d%n", runtime.maxMemory() / mb);

        Parameters ps = opts.parameters();

        Iterator<LabelledInstance> ite = opts.instances().iterator();
        while (ite.hasNext()) {
            LabelledInstance i = ite.next();
            solve(i, ps);
        }
    }

    private static void solve(LabelledInstance i, Parameters ps) throws IOException {
        ChocoScheduler s = new DefaultChocoScheduler().setParameters(ps);
        String status = "OK";
        try {
            s.solve(i);
        } catch (@SuppressWarnings("unused") UnstatableProblemException ex) {
            status = "TO";
        } catch (@SuppressWarnings("unused") SchedulerException ex) {
            status = "FAIL";
        }
        if (opts.single()) {
            out(0, "%s%n", s.getStatistics());
        } else {
            SolvingStatistics stats = s.getStatistics();
            if (stats.getSolutions().isEmpty()) {
                status = "KO*";
            } else {
                status = "OK";
                if (stats.completed()) {
                    status += "*";
                }
            }
            if (opts.verbosity() == 0) {
                out(0, "%s: %s%n", i.label, status);
            } else {
                out(1, "----- %s -----%n", i.label);
                out(1, "%s%n", stats);
                out(1, "%n");
            }
            File output = opts.output();
            store(i, stats, output);
        }
    }

    private static void out(int lvl, String fmt, Object... args) {
        if (opts.verbosity() >= lvl) {
            System.out.printf(fmt, args);
        }
    }
    private static void store(LabelledInstance i, SolvingStatistics stats, File base) throws IOException {
        Files.createDirectories(base.toPath());
        //Stats about the solving process
        Path p = Paths.get(base.getAbsolutePath(), SCHEDULER_STATS);
        UUID id = uniqueFile(base);
        StringBuilder line = new StringBuilder(id.toString()).append(";").append(i.label).append(";").append(stats.toCSV());
        Files.write(p, Collections.singletonList(line), UTF_8, CREATE, APPEND);
        ReconfigurationPlan best = stats.lastSolution();

        //The resulting plan
        if (best != null) {
            File f = toFile(base, id);
            JSON.write(best, f);
        }
    }

    private static File toFile(File root, UUID id) {
        return new File(root.getAbsolutePath() + File.separator + id.toString() + ".gz");
    }

    private static UUID uniqueFile(File base) {
        UUID u;
        File f;
        do {
            u = UUID.randomUUID();
            f = toFile(base, u);
        } while (f.exists());
        return u;
    }
}

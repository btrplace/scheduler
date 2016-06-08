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

import org.btrplace.json.JSON;
import org.btrplace.scheduler.choco.DefaultParameters;
import org.btrplace.scheduler.choco.Parameters;
import org.kohsuke.args4j.Option;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * CLI options to indicate instances and the solver tuning.
 *
 * @author Fabien Hermenier
 */
public class Options {

    @Option(name = "-r", aliases = "--repair", usage = "Enable the 'repair' feature")
    private boolean repair;

    @Option(name = "-m", aliases = "--optimize", usage = "Enable the 'optimize' feature")
    private boolean optimize;

    @Option(name = "-t", aliases = "--timeout", usage = "Set a timeout (in sec)")
    private int timeout = 0;

    @Option(name = "-i", aliases = "--instance", usage = "An instance  ('.json' or '.json.gz')", forbids = {"-l"})
    private String instance;

    @Option(name = "-l", aliases = "--list", usage = "a list of instance files (one path per line)", forbids = {"-i"})
    private String instances;

    @Option(name = "-o", aliases = "--output", usage = "Output folder where the CSV and the plans are stored", depends = {"-l"})
    private String output = "./";

    @Option(name = "-v", usage = "Set the verbosity level. With '-i' it controls the solver verbosity. With '-l' the bench progress")
    private int verbosity = 0;

    @Option(name = "-n", usage = "The numbers of repetitions per instance.")
    private int count = 1;

    /**
     * Get the parameters from the options.
     *
     * @return the resulting parameters
     */
    public Parameters parameters() {
        Parameters ps = new DefaultParameters()
                .setTimeLimit(timeout)
                .doRepair(repair)
                .doOptimize(optimize);

        if (single()) {
            ps.setVerbosity(verbosity);
        }
        return ps;
    }

    /**
     * Get the verbosity
     *
     * @return the verbosity level
     */
    public int verbosity() {
        return verbosity;
    }

    /**
     * Check if there is only one instance to solver
     *
     * @return {@code true} if there is one instance to solve ontly
     */
    public boolean single() {
        return instance != null;
    }

    /**
     * List all the instances to solve.
     * @return a list of instances
     * @throws IOException if it was not possible to get all the instances
     */
    public List<LabelledInstance> instances() throws IOException {
        List<LabelledInstance> res;
        if (single()) {
            res = Collections.singletonList(instance(new File(instance)));
        } else {
            try (Stream<String> s = Files.lines(Paths.get(instances), StandardCharsets.UTF_8)) {
                res = s.map(x -> instance(new File(x))).collect(Collectors.toList());
            }
        }
        return repeat(res);
    }

    private List<LabelledInstance> repeat(List<LabelledInstance> res) {
        List<LabelledInstance> l = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            l.addAll(res);
        }
        return l;
    }


    /**
     * Get the output directory.
     * @return an existing output directory
     * @throws IOException if the directory does not exists or was not created
     */
    public File output() throws IOException {
        File o = new File(output);
        if (!o.exists() && !o.mkdirs()) {
            throw new IOException("Unable to create output folder '" + output + "'");
        }
        return o;
    }


    /**
     * Make an instance.
     * @param f the file that store the instance
     * @return the parsed instance. The instance label is the file name
     */
    public static LabelledInstance instance(File f) {
        String path = f.getAbsolutePath();
        return new LabelledInstance(path, JSON.readInstance(f));
    }
}

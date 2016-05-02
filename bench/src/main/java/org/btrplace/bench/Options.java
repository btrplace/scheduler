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

import org.btrplace.scheduler.choco.DefaultParameters;
import org.btrplace.scheduler.choco.Parameters;
import org.kohsuke.args4j.Option;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.btrplace.json.model.InstanceConverter.quickFromJSON;

/**
 * @author Fabien Hermenier
 */
public class Options {

    // Define options list
    @Option(name = "-r", aliases = "--repair", usage = "Enable the 'repair' feature")
    private boolean repair;

    @Option(name = "-m", aliases = "--optimize", usage = "Enable the 'optimize' feature")
    private boolean optimize;

    @Option(name = "-t", aliases = "--timeout", usage = "Set a timeout (in sec)")
    private int timeout = 0;

    @Option(required = true, name = "-i", aliases = "--input", usage = "An instance  ('.json' or '.json.gz') or a list of instances (one instance per line)")
    private String input;

    @Option(name = "-o", aliases = "--output", usage = "Output to this file")
    private String output = "./";

    @Option(name = "-p", aliases = "--progress", usage = "Show the progress")
    private int progress = 0; //0 silent, 1: id and status (solved or not), 2: statistics

    @Option(name = "-v", usage = "Set the verbosity level for the scheduler")
    private int verbosity = 0;

    public Parameters parameters() {
        return new DefaultParameters()
                .setTimeLimit(timeout)
                .setVerbosity(verbosity)
                .doRepair(repair)
                .doOptimize(optimize);
    }

    public List<LabelledInstance> instances() throws IOException {
        File f = new File(input);

        if (f.isFile()) {
            if (f.getName().endsWith(".json") || f.getName().endsWith(".gz")) {
                //We assume it is an instance
                return Collections.singletonList(instance(f));
            } else {
                //We assume it is a list of instance files
                return Files.lines(Paths.get(input), StandardCharsets.UTF_8)
                        .map(s -> instance(new File(s)))
                        .collect(Collectors.toList());
            }
        } else if (f.isDirectory()) {
            //We assume all the files in are instances
            File[] files = f.listFiles();
            if (files == null) {
                throw new IllegalArgumentException(input + " should be a folder");
            }
            return Arrays.asList(files).stream().map(Options::instance).collect(Collectors.toList());
        }
        throw new IllegalArgumentException(input + " should be a file or a folder");
    }

    public File output() throws IOException {
        File o = new File(output);
        if (!o.exists() && !o.mkdirs()) {
            throw new IOException("Unable to create output folder '" + output + "'");
        }
        return o;
    }

    public static LabelledInstance instance(File f) {
        String path = f.getPath();
        String lbl = path.substring(0, path.indexOf('.'));
        return new LabelledInstance(lbl, quickFromJSON(new File(path)));
    }

    public int progress() {
        return progress;
    }
}

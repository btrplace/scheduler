/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.json;

import org.btrplace.json.model.InstanceConverter;
import org.btrplace.json.plan.ReconfigurationPlanConverter;
import org.btrplace.model.Instance;
import org.btrplace.plan.ReconfigurationPlan;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Utility class to ease the serialisation and the deserialisation of the main btrplace entities.
 *
 * @author Fabien Hermenier
 */
public class JSON {

    private JSON() {
    }

    private static InputStreamReader makeIn(File f) throws IOException {
        if (f.getName().endsWith(".gz")) {
            return new InputStreamReader(new GZIPInputStream(new FileInputStream(f)), UTF_8);
        }
        return new InputStreamReader(new FileInputStream(f), UTF_8);
    }

    private static OutputStreamWriter makeOut(File f) throws IOException {
        if (f.getName().endsWith(".gz")) {
            return new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(f)), UTF_8);
        }
        return new OutputStreamWriter(new FileOutputStream(f), UTF_8);
    }

    /**
     * Read an instance from a file.
     * A file ending with '.gz' is uncompressed first
     *
     * @param f the file to parse
     * @return the resulting instance
     * @throws IllegalArgumentException if an error occurred while reading the file
     */
    public static Instance readInstance(File f) {
        try (Reader in = makeIn(f)) {
            return readInstance(in);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Read an instance.
     *
     * @param r the stream to read
     * @return the resulting instance
     * @throws IllegalArgumentException if an error occurred while reading the json
     */
    public static Instance readInstance(Reader r) {
        try {
            InstanceConverter c = new InstanceConverter();
            return c.fromJSON(r);
        } catch (JSONConverterException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Write an instance.
     *
     * @param instance the instance to write
     * @param f        the output file. If it ends with '.gz' it will be gzipped
     * @throws IllegalArgumentException if an error occurred while writing the json
     */
    public static void write(Instance instance, File f) {
        try (OutputStreamWriter out = makeOut(f)) {
            write(instance, out);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Write an instance
     *
     * @param instance the instance to write
     * @param a        the stream to write on.
     * @throws IllegalArgumentException if an error occurred while writing the json
     */
    public static void write(Instance instance, Appendable a) {
        try {
            InstanceConverter c = new InstanceConverter();
            c.toJSON(instance).writeJSONString(a);
        } catch (IOException | JSONConverterException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Serialise a instance.
     *
     * @param instance the instance to write
     * @return the JSON string
     * @throws IllegalArgumentException if an error occurred while writing the json
     */
    public static String toString(Instance instance) {
        try {
            InstanceConverter c = new InstanceConverter();
            return c.toJSON(instance).toJSONString();
        } catch (JSONConverterException e) {
            throw new IllegalArgumentException(e);
        }
    }


    /**
     * Read a reconfiguration plan from a file.
     * A file ending with '.gz' is uncompressed first
     *
     * @param f the file to parse
     * @return the resulting plan
     * @throws IllegalArgumentException if an error occurred while reading the file
     */
    public static ReconfigurationPlan readReconfigurationPlan(File f) {
        try (Reader in = makeIn(f)) {
            return readReconfigurationPlan(in);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Read a plan.
     *
     * @param r the stream to read
     * @return the resulting reconfiguration plan
     * @throws IllegalArgumentException if an error occurred while reading the json
     */
    public static ReconfigurationPlan readReconfigurationPlan(Reader r) {
        try {
            ReconfigurationPlanConverter c =
                ReconfigurationPlanConverter.newBundle();
            return c.fromJSON(r);
        } catch (JSONConverterException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Write a reconfiguration plan.
     *
     * @param plan the reconfiguration plan to write
     * @param f    the output file. If it ends with '.gz' it will be gzipped
     * @throws IllegalArgumentException if an error occurred while writing the json
     */
    public static void write(ReconfigurationPlan plan, File f) {
        try (OutputStreamWriter out = makeOut(f)) {
            write(plan, out);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Write a reconfiguration plan.
     *
     * @param plan the plan to write
     * @param a    the stream to write on.
     * @throws IllegalArgumentException if an error occurred while writing the json
     */
    public static void write(ReconfigurationPlan plan, Appendable a) {
        try {
            ReconfigurationPlanConverter c =
                ReconfigurationPlanConverter.newBundle();
            c.toJSON(plan).writeJSONString(a);
        } catch (IOException | JSONConverterException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Serialise a reconfiguration plan.
     *
     * @param plan the plan to write
     * @return the JSON string
     * @throws IllegalArgumentException if an error occurred while writing the json
     */
    public static String toString(ReconfigurationPlan plan) {
        try {
            ReconfigurationPlanConverter c = new ReconfigurationPlanConverter();
            return c.toJSON(plan).toJSONString();
        } catch (JSONConverterException e) {
            throw new IllegalArgumentException(e);
        }
    }
}

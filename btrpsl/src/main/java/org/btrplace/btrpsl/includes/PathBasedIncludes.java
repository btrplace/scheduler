/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.btrpsl.includes;

import org.btrplace.btrpsl.Script;
import org.btrplace.btrpsl.ScriptBuilder;
import org.btrplace.btrpsl.ScriptBuilderException;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * An implementation that loop for the searched script among several folders.
 * Similar to a shell path.
 *
 * @author Fabien Hermenier
 */
public class PathBasedIncludes implements Includes {

  /**
   * The folders to browse.
   */
  private final List<File> paths;

    /**
     * The builder to create the scripts.
     */
    private final ScriptBuilder builder;

  /**
     * Make a new instance that will browse a first folder.
     *
     * @param vBuilder the builder to parse the scripts
     */
    public PathBasedIncludes(ScriptBuilder vBuilder) {
        this.paths = new LinkedList<>();
        this.builder = vBuilder;
    }

    /**
     * Get the script associated to a given identifier by browsing the given paths.
     * The first script having a matching identifier is selected, whatever the parsing process result will be
     *
     * @param name the identifier of the script
     * @return the script if found
     * @throws org.btrplace.btrpsl.ScriptBuilderException if the builder was not able to parse the looked script
     */
    @Override
    public List<Script> getScripts(String name) throws ScriptBuilderException {

        List<Script> scripts = new ArrayList<>();
        if (!name.endsWith(".*")) {
            String toSearch = name.replaceAll("\\.", File.separator) + Script.EXTENSION;
            for (File path : paths) {
                File f = new File(path.getPath() + File.separator + toSearch);
                if (f.exists()) {
                    scripts.add(builder.build(f));
                    break;
                }
            }

        } else {

            //We need to consolidate the errors in allEx and rethrow it at the end if necessary
            ScriptBuilderException allEx = null;
            String base = name.substring(0, name.length() - 2).replaceAll("\\.", File.separator);
            for (File path : paths) {
                File f = new File(path.getPath() + File.separator + base);
                File[] files = f.listFiles();
                if (f.isDirectory() && files != null) {
                    for (File sf : files) {
                        if (sf.getName().endsWith(Script.EXTENSION)) {

                            try {
                                scripts.add(builder.build(sf));
                            } catch (ScriptBuilderException ex) {
                                if (allEx == null) {
                                    allEx = ex;
                                } else {
                                    allEx.getErrorReporter().getErrors().addAll(ex.getErrorReporter().getErrors());
                                }
                            }
                        }
                    }
                }
            }
            if (allEx != null) {
                throw allEx;
            }
        }
        return scripts;
    }

    /**
     * Add a new folder to browse.
     *
     * @param path the path to the folder
     *             {@code path} must be an existing folder
     * @return {@code true} if the path was added
     */
    public boolean addPath(File path) {
        return path.isDirectory() && this.paths.add(path);
    }

    /**
     * Get all the given paths.
     *
     * @return a list of paths that may be empty
     */
    public List<File> getPaths() {
        return this.paths;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        for (Iterator<File> ite = paths.iterator(); ite.hasNext(); ) {
            b.append(ite.next().getPath());
            if (ite.hasNext()) {
                b.append(File.pathSeparatorChar);
            }
        }
        return b.toString();
    }
}

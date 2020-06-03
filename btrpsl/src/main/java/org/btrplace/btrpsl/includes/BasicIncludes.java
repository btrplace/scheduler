/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.btrpsl.includes;

import org.btrplace.btrpsl.Script;
import org.btrplace.btrpsl.ScriptBuilderException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * A basic include mechanism where all the scripts are added manually.
 *
 * @author Fabien Hermenier
 */
public class BasicIncludes implements Includes {
  private final Map<String, Script> hash;

    /**
     * New includes.
     */
    public BasicIncludes() {
        this.hash = new HashMap<>();
    }

    @Override
    public List<Script> getScripts(String name) throws ScriptBuilderException {

        List<Script> scripts = new ArrayList<>();
        if (!name.endsWith(".*")) {
            if (hash.containsKey(name)) {
                scripts.add(hash.get(name));
            }
        } else {
            String base = name.substring(0, name.length() - 2);
            for (Map.Entry<String, Script> e : hash.entrySet()) {
                if (e.getKey().startsWith(base)) {
                    scripts.add(e.getValue());
                }
            }
        }
        return scripts;
    }

    /**
     * Add a script into the set of included scripts.
     *
     * @param script the script to add
     */
    public void add(Script script) {
        this.hash.put(script.id(), script);
    }
}

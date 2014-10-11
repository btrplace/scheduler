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
    private Map<String, Script> hash;

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

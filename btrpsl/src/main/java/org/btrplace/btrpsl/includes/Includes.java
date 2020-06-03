/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.btrpsl.includes;

import org.btrplace.btrpsl.Script;
import org.btrplace.btrpsl.ScriptBuilderException;

import java.util.List;

/**
 * Denotes a library that is used to get the scripts required from the 'use' statement.
 *
 * @author Fabien Hermenier
 */
@FunctionalInterface
public interface Includes {

    /**
     * Get a list of script from an identifier.
     * If the identifier ends with the '.*' wildcard, then any script matching this wildcard will be returned.
     * Otherwise, only the first script matching the identifier will be returned if it exists.
     *
     * @param name the identifier of the script
     * @return A list containing the matched script, may be empty.
     * @throws org.btrplace.btrpsl.ScriptBuilderException if an error occurred while parsing the founded script
     */
    List<Script> getScripts(String name) throws ScriptBuilderException;
}

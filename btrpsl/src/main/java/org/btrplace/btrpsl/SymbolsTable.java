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

package org.btrplace.btrpsl;

import org.btrplace.btrpsl.element.BtrpOperand;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

/**
 * A table of symbols to store variables in a script description.
 * Variables can be declared as immutable. In this context, they can not be modified
 * once declared.
 * In addition, the table can be pushed or popped to simulate a context. Pushing a table
 * create a new context. When it is popped, every variable created after the last push are
 * automatically removed.
 *
 * @author Fabien Hermenier
 */
public class SymbolsTable {

    public static final String ME = "$me";

    /**
     * The declared variables.
     */
    private Map<String, BtrpOperand> type;

    /**
     * The operating level of each variable.
     */
    private Map<String, Integer> level;

    /**
     * The current context of the table.
     * Every immutable variabls will have a negative context.
     */
    private int currentLevel = 0;

    /**
     * Make a new table of symbols for a script.
     */
    public SymbolsTable() {
        type = new Hashtable<>();
        level = new Hashtable<>();
    }

    /**
     * Declare an immutable variable. The variable must not
     * has been already declared.
     *
     * @param label the identifier of the variable
     * @param t     the operand associated to the identifier
     * @return {@code true} if the variable as been declared. {@code false} otherwise
     */
    public boolean declareImmutable(String label, BtrpOperand t) {
        if (isDeclared(label)) {
            return false;
        }
        level.put(label, -1);
        type.put(label, t);
        return true;
    }

    /**
     * Remove a symbol from the table.
     *
     * @param label the symbol to remove
     * @return {@code true} if the symbol was present then removed. {@code false} otherwise
     */
    public boolean remove(String label) {
        if (!isDeclared(label)) {
            return false;
        }
        level.remove(label);
        type.remove(label);
        return true;
    }

    /**
     * Check if one variable is immutable.
     *
     * @param label the identifier of the variable
     * @return {@code true} if the variable is immutable
     */
    public boolean isImmutable(String label) {
        return (isDeclared(label) && level.get(label) < 0);
    }

    /**
     * Declare a new variable.
     * The variable is inserted into the current script.
     *
     * @param label the label of the variable
     * @param t     the content of the variable
     * @return {@code true} if the declaration succeeds, {@code false} otherwise
     */
    public final boolean declare(String label, BtrpOperand t) {
        if (isDeclared(label) && level.get(label) < 0) { //Disallow immutable value
            return false;
        }
        if (!isDeclared(label)) {
            level.put(label, currentLevel);
        }
        type.put(label, t);
        return true;
    }

    /**
     * Get the content associated to a variable label.
     *
     * @param label the label of the variable
     * @return the content of the variable if exists. {@code null} otherwise
     */
    public BtrpOperand getSymbol(String label) {
        return type.get(label);
    }

    /**
     * Check wether a variable is declared.
     *
     * @param label the label of the variable
     * @return {@code true} if the variable is already declared, {@code false} otherwise
     */
    public boolean isDeclared(String label) {
        return type.containsKey(label);
    }

    /**
     * Textual representation of the table of symbol.
     *
     * @return all the variables and their content. One variable per line.
     */
    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        for (Iterator<Map.Entry<String, BtrpOperand>> ite = type.entrySet().iterator(); ite.hasNext(); ) {
            Map.Entry<String, BtrpOperand> e = ite.next();
            b.append(e.getKey());
            b.append(": ");
            b.append(e.getValue());
            if (ite.hasNext()) {
                b.append("\n");
            }
        }
        return b.toString();
    }

    /**
     * Push the table.
     * Every variables in the table are saved
     */
    public void pushTable() {
        currentLevel++;
    }

    /**
     * Pop the table.
     * Every variable created after the last pushTable() are removed from the table. The
     * table can not be poped if it was not at least pushed once earlier.
     *
     * @return {@code true} if the table has been popped successfully. {@code false} otherwise
     */
    public boolean popTable() {
        if (currentLevel > 0) {
            //Remove all the variable having a level equals to currentLevel;
            for (Iterator<Map.Entry<String, Integer>> ite = level.entrySet().iterator(); ite.hasNext(); ) {
                Map.Entry<String, Integer> e = ite.next();
                if (e.getValue() == currentLevel) {
                    ite.remove();
                    level.remove(e.getKey());
                    type.remove(e.getKey());
                }
            }
            currentLevel--;
            return true;
        }
        return false;
    }
}

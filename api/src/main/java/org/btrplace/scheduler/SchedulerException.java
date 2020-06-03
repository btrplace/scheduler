/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler;

import org.btrplace.model.Model;

/**
 * An exception that indicate a programing error in the scheduler.
 *
 * @author Fabien Hermenier
 */
public class SchedulerException extends RuntimeException {

    private final transient Model model;

    /**
     * Make a new exception.
     *
     * @param m   the model that lead to the exception
     * @param msg the error message
     */
    public SchedulerException(Model m, String msg) {
        super(msg);
        model = m;
    }

    /**
     * Make a new exception.
     *
     * @param m   the model that lead to the exception
     * @param msg the error message
     * @param t   the throwable to re-throw
     */
    public SchedulerException(Model m, String msg, Throwable t) {
        super(msg, t);
        model = m;
    }

    /**
     * Get the model at the source of the exception.
     *
     * @return a Model
     */
    public Model getModel() {
        return model;
    }
}

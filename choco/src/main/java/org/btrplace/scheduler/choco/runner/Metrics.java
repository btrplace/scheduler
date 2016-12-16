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

package org.btrplace.scheduler.choco.runner;

import org.btrplace.Copyable;
import org.chocosolver.solver.search.measure.Measures;

import java.util.Objects;

/**
 * Metrics related to the resolution process.
 *
 * @author Fabien Hermenier
 */
public class Metrics implements Copyable<Metrics> {

    private long readingTimeCount;

    private long timeCount;

    private long nodes;

    private long backtracks;

    private long fails;

    private long restarts;

    /**
     * New empty metrics.
     * Every value are zeroed
     */
    public Metrics() {
        //values are zeroed
    }

    /**
     * Build the metrics from Choco measures.
     *
     * @param m the measures
     */
    public Metrics(Measures m) {
        timeCount = (long) (m.getTimeCountInNanoSeconds() / 1E6d);
        readingTimeCount = (long) (m.getReadingTimeCountInNanoSeconds() / 1E6d);
        nodes = m.getNodeCount();
        backtracks = m.getBackTrackCount();
        fails = m.getFailCount();
        restarts = m.getRestartCount();
    }

    /**
     * New metrics. Duration are expressed in milliseconds.
     *
     * @param readingTimeCount the time to read the model
     * @param timeCount        the solving duration
     * @param nodes            the number of opened search nodes
     * @param backtracks       the number of backtracks
     * @param fails            the number of failures
     * @param restarts         the number of restarts
     */
    public Metrics(long readingTimeCount, long timeCount, long nodes, long backtracks, long fails, long restarts) {
        this.timeCount = timeCount;
        this.readingTimeCount = readingTimeCount;
        this.nodes = nodes;
        this.backtracks = backtracks;
        this.fails = fails;
        this.restarts = restarts;
    }

    @Override
    public Metrics copy() {
        return new Metrics(readingTimeCount, timeCount, nodes, backtracks, fails, restarts);
    }

    /**
     * Add metrics.
     * All the metrics are aggregated to the current instance
     *
     * @param m the metrics to add
     */
    public void add(Metrics m) {
        timeCount += m.timeCount;
        readingTimeCount += m.readingTimeCount;
        nodes += m.nodes;
        backtracks += m.backtracks;
        fails += m.fails;
        restarts += m.restarts;
    }

    @Override
    public String toString() {
        float sec = 1f * (timeCount / 1000);
        return String.format("at %dms, %d Nodes (%,.1f n/s), %d Backtracks, %d Fails, %d Restarts",
                timeCount,
                nodes,
                1.0f * nodes / sec,
                backtracks,
                fails,
                restarts
        );
    }

    /**
     * Returns the time to read the model.
     *
     * @return a duration in milliseconds
     */
    public long readingTimeCount() {
        return readingTimeCount;
    }

    /**
     * Returns the time to solve.
     *
     * @return a duration in milliseconds
     */
    public long timeCount() {
        return timeCount;
    }

    /**
     * Returns the number of opened search nodes.
     *
     * @return a number
     */
    public long nodes() {
        return nodes;
    }

    /**
     * Returns the number of backtracks.
     *
     * @return a number
     */
    public long backtracks() {
        return backtracks;
    }

    /**
     * Returns the number of failures.
     *
     * @return a number
     */
    public long fails() {
        return fails;
    }

    /**
     * Returns the number of restarts.
     *
     * @return a number
     */
    public long restarts() {
        return restarts;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Metrics metrics = (Metrics) o;
        return readingTimeCount == metrics.readingTimeCount &&
                timeCount == metrics.timeCount &&
                nodes == metrics.nodes &&
                backtracks == metrics.backtracks &&
                fails == metrics.fails &&
                restarts == metrics.restarts;
    }

    @Override
    public int hashCode() {
        return Objects.hash(readingTimeCount, timeCount, nodes, backtracks, fails, restarts);
    }
}

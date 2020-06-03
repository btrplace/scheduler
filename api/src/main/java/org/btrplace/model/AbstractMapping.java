/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.model;

import java.util.Objects;

/**
 * An helper abstract class for standards equals() and hashcode() methods
 * as it is not possible to make them as default in Mapping.
 *
 * @author Fabien Hermenier
 */
public abstract class AbstractMapping implements Mapping {

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Mapping)) {
            return false;
        }

        Mapping that = (Mapping) o;

        if (!getOnlineNodes().equals(that.getOnlineNodes())
                || !getOfflineNodes().equals(that.getOfflineNodes())
                || !getReadyVMs().equals(that.getReadyVMs())) {
            return false;
        }

        for (Node n : getOnlineNodes()) {
            if (!getRunningVMs(n).equals(that.getRunningVMs(n))
                    || !getSleepingVMs(n).equals(that.getSleepingVMs(n))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(getOfflineNodes(), getReadyVMs(), getOnlineNodes());
        for (Node n : getOnlineNodes()) {
            result += Objects.hash(n, getRunningVMs(n), getSleepingVMs(n));
        }
        return result;
    }
}

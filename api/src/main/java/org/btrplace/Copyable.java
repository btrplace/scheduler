package org.btrplace;

/**
 * Copyable is a safer alternative to clone.
 *
 * @author Fabien Hermenier
 */
@FunctionalInterface
public interface Copyable<T> {

    /**
     * Make a deep copy of the object.
     *
     * @return a deep copy
     */
    T copy();
}

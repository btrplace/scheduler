package org.btrplace.safeplace.fuzzer;

import org.btrplace.safeplace.Constraint;

/**
 * Created by fhermeni on 25/07/2015.
 */
public interface Fuzzer {

    TestCase fuzz(String lbl, Constraint c);

    Fuzzer clone();
}

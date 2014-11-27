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

package org.btrplace.safeplace.fuzzer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class TransitionTable {

    private List<String> dstLabels;

    private List<String> srcLabels;

    private List<double[]> proba;

    public TransitionTable(Reader in) throws IOException {
        proba = new ArrayList<>();
        makeTable(in);
    }

    private void makeTable(Reader st) throws IOException {
        srcLabels = new ArrayList<>();
        try (BufferedReader r = new BufferedReader(st)) {
            String[] lbls = r.readLine().split("\\t"); //split \t
            dstLabels = new ArrayList<>();
            for (int i = 1; i < lbls.length; i++) {
                dstLabels.add(lbls[i]);
            }

            String l = r.readLine();
            while (l != null) {
                String[] toks = l.split("\\t");
                l = r.readLine();
                srcLabels.add(toks[0]);
                double[] probs = new double[dstLabels.size()];
                if (toks.length != dstLabels.size() + 1) {
                    throw new IOException("Missing numbers for transition from state " + toks[0]);
                }
                for (int i = 0; i < dstLabels.size(); i++) {
                    probs[i] = Double.parseDouble(toks[i + 1]);
                }
                proba.add(probs);
            }
        }
    }

    public String getInitState(double d) {
        double total = 0;
        for (int i = 0; i < srcLabels.size(); i++) {
            total += proba.get(i)[0];
            if (d < total) {
                return srcLabels.get(i);
            }
        }
        throw new RuntimeException("Probabilities exceed 1.0 for initial state: " + d);
    }

    public String getDstState(String src, double d) {
        int j = srcLabels.indexOf(src);
        double total = 0;
        for (int i = 1; i < proba.get(j).length; i++) {
            total += proba.get(j)[i];
            if (d < total) {
                return dstLabels.get(i);
            }
        }
        throw new RuntimeException("Probabilities exceed 1.0");
    }

    private double getProba(String src) {
        int i = srcLabels.indexOf(src);
        return proba.get(i)[0];
    }

    private double getProba(String src, String dst) {
        int i = srcLabels.indexOf(src);
        int j = dstLabels.indexOf(dst);
        return proba.get(i)[j];
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        for (String l : dstLabels) {
            b.append("\t").append(l);
        }
        b.append("\n");
        for (int i = 0; i < srcLabels.size(); i++) {
            b.append(srcLabels.get(i));
            for (double d : proba.get(i)) {
                b.append("\t").append(d);
            }
            b.append("\n");
        }
        return b.toString();
    }
}

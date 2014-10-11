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

package org.btrplace.bench;

import java.io.IOException;

/**
 * Created by vkherbac on 16/09/14.
 */
public class LauncherTest {

    public void test() throws IOException {

        Launcher.main(new String[]{
                "--repair",
                "--timeout", "500",
                "-i", "src/test/resources/wkld-tdsc/nr/r3/p5000/c66/1.gz",
                "-o", "src/test/resources/nr-r3-p5000-c66-1.csv"
        });
        System.err.flush();
        System.out.flush();
    }
}

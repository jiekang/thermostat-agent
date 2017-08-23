/*
 * Copyright 2012-2017 Red Hat, Inc.
 *
 * This file is part of Thermostat.
 *
 * Thermostat is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 2, or (at your
 * option) any later version.
 *
 * Thermostat is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Thermostat; see the file COPYING.  If not see
 * <http://www.gnu.org/licenses/>.
 *
 * Linking this code with other modules is making a combined work
 * based on this code.  Thus, the terms and conditions of the GNU
 * General Public License cover the whole combination.
 *
 * As a special exception, the copyright holders of this code give
 * you permission to link this code with independent modules to
 * produce an executable, regardless of the license terms of these
 * independent modules, and to copy and distribute the resulting
 * executable under terms of your choice, provided that you also
 * meet, for each linked independent module, the terms and conditions
 * of the license of that module.  An independent module is a module
 * which is not derived from or based on this code.  If you modify
 * this code, you may extend this exception to your version of the
 * library, but you are not obligated to do so.  If you do not wish
 * to do so, delete this exception statement from your version.
 */

package com.redhat.thermostat.common.portability;

import com.redhat.thermostat.shared.config.OS;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * A wrapper over POSIX's sysconf.
 * <p>
 * Implementation notes: uses {@code getconf(1)}
 */
public class SysConf {

    private SysConf() {
        /* do not initialize */
    }

    public static long getClockTicksPerSecond() {
        return OS.IS_UNIX ? getPosixClockTicksPerSecond() : getWindowsClockTicksPerSecond();
    }

    private static long getWindowsClockTicksPerSecond() {
        return PortableHostFactory.getInstance().getClockTicksPerSecond();
    }

    public static long getPosixClockTicksPerSecond() {
        String ticks = sysConf("CLK_TCK");
        try {
            return Long.valueOf(ticks);
        } catch (NumberFormatException nfe) {
            return 0;
        }
    }

    public static long getPageSize() {
        try {
            return Long.valueOf(sysConf("PAGESIZE"));
        } catch (NumberFormatException nfe) {
            return 0;
        }
    }

    private static String sysConf(String arg) {
        try {
            Process process = Runtime.getRuntime().exec(new String[] { "getconf", arg });
            int result = process.waitFor();
            if (result != 0) {
                return null;
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                return reader.readLine();
            }
        } catch (IOException | InterruptedException e) {
            return null;
        }
    }
}


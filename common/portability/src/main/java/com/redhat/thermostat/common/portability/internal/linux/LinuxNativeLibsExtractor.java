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

package com.redhat.thermostat.common.portability.internal.linux;

import com.redhat.thermostat.common.utils.LoggingUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LinuxNativeLibsExtractor {

    private static final Logger LOGGER
            = LoggingUtils.getLogger(LinuxNativeLibsExtractor.class);

    public static String[] getNativeLibs(int pid) {
        return getNativeLibsFromReader(new File(String.format("/proc/%d/maps", pid)));
    }

    // for testing purposes only
    static String[] getNativeLibs(File testFile) {
        return getNativeLibsFromReader(testFile);
    }

    private static String[] getNativeLibsFromReader(File nativeLibFile) {
        final String soGrep = ".+\\.so.*";
        Set<String> result = new HashSet<>();
        try (BufferedReader br
                     = new BufferedReader(new FileReader(nativeLibFile))) {
            String next = br.readLine();
            while (next != null) {
                next = next.trim();
                if (next.matches(soGrep)) {
                    String candidate = next.substring(next.lastIndexOf(' ') + 1);
                    result.add(candidate);
                }
                next = br.readLine();
            }
            return result.toArray(new String[0]);
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Unable to retrieve native libraries.");
            LOGGER.log(Level.INFO, ex.getMessage());
            return new String[0];
        }
    }
}

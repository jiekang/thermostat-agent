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

package com.redhat.thermostat.jvm.overview.agent.internal.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.HashSet;
import org.junit.Before;
import org.junit.Test;

public class VmLinuxNativeLibsExtractorTest {

    private VmLinuxNativeLibsExtractor extractor;

    private static final Integer VM_PID = 0;

    private final File THREE_LIBS = getFileFromTestSources("native_lib_three_libs");
    private final File NO_LIBS = getFileFromTestSources("native_lib_no_libs");
    private final File EMPTY = getFileFromTestSources("native_lib_empty");

    private File getFileFromTestSources(String path) {
        path = '/' + path;
        return new File(decodeFilePath(this.getClass().getResource(path)));
    }

    private String decodeFilePath(URL url) {
        try {
            return URLDecoder.decode(url.getFile(), "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            throw new AssertionError(ex);
        }
    }

    @Before
    public void setup() {
        extractor = new VmLinuxNativeLibsExtractor(VM_PID);
    }

    @Test
    public void threeLibs() {
        HashSet<String> expectedLibs = new HashSet<>(Arrays.asList("/usr/foo/libhello.so",
                "/usr/bar/libworld.so.0.18.0", "/tmp/libnew.so.so"));

        // Cannot compare arrays directly since the implementation internally gathers libs
        // to a Set, which might yield a different ordering of items in the collection in the end
        assertEquals(expectedLibs, new HashSet<>(Arrays.asList(extractor.getNativeLibs(THREE_LIBS))));
    }

    @Test
    public void noLibs() {
        String[] result = extractor.getNativeLibs(NO_LIBS);
        int numExpectedLibs = 0;

        assertNotNull(result);
        assertEquals(result.length, numExpectedLibs);

    }

    @Test
    public void empty() {
        String[] result = extractor.getNativeLibs(EMPTY);
        int numExpectedLibs = 0;

        assertNotNull(result);
        assertEquals(result.length, numExpectedLibs);

    }

}

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

package com.redhat.thermostat.vm.gc.agent.params;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertTrue;

public class CollectorTest {

    private static final String COMMON_NAME = "COMMON_NAME";
    private static final Set<String> DISTINCT_COLLECTOR_NAMES = new HashSet<>(Arrays.asList("COLLECTOR1", "COLLECTOR2"));
    private static final JavaVersionRange JAVA_VERSION = new JavaVersionRange(new JavaVersionRange.VersionPoints(1, 8, 0, 45));
    private static final Set<GcParam> GC_PARAMS = new HashSet<GcParam>() {{
        add(new GcParam("-XXflag", "Description", new JavaVersionRange(new JavaVersionRange.VersionPoints(1, 9, 0, 10))));
    }};
    private static final String REFERENCE_URL = "http://example.com";
    private static final CollectorInfo COLLECTOR_INFO = new CollectorInfo(JAVA_VERSION, COMMON_NAME, DISTINCT_COLLECTOR_NAMES, REFERENCE_URL);

    @Test
    public void testGetters() {
        Collector collector = new Collector(COLLECTOR_INFO, GC_PARAMS);
        assertTrue(COLLECTOR_INFO.toString(), COLLECTOR_INFO.equals(collector.getCollectorInfo()));
        assertTrue(GC_PARAMS.toString(), GC_PARAMS.equals(collector.getGcParams()));
    }

    @Test(expected = NullPointerException.class)
    public void testNullCollectorInfoDisallowed() {
        new Collector(null, GC_PARAMS);
    }

    @Test(expected = NullPointerException.class)
    public void testNullGcParamsDisallowed() {
        new Collector(COLLECTOR_INFO, null);
    }
}

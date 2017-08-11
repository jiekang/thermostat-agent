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

package com.redhat.thermostat.host.overview.model;

import static com.redhat.thermostat.testutils.JsonUtils.assertJsonEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class HostInfoTypeAdapterTest {
    
    private HostInfoTypeAdapter adapter;
    
    @Before
    public void setup() {
        adapter = new HostInfoTypeAdapter();
    }

    @Test
    public void testWrite() throws Exception {
        final String expected = "[{\"agentId\":\"myAgent1\",\"hostname\":\"myHost1\","
                + "\"timeStamp\":{\"$numberLong\":\"111\"},"
                + "\"osName\":\"myOS1\",\"osKernel\":\"myKernel1\",\"cpuModel\":\"myCPU1\"," 
                + "\"cpuCount\":4,\"totalMemory\":{\"$numberLong\":\"400000000\"}}," 
                + "{\"agentId\":\"myAgent2\",\"hostname\":\"myHost2\","
                + "\"timeStamp\":{\"$numberLong\":\"222\"},"
                + "\"osName\":\"myOS2\",\"osKernel\":\"myKernel2\",\"cpuModel\":\"myCPU2\",\"cpuCount\":2,"
                + "\"totalMemory\":{\"$numberLong\":\"800000000\"}}]";
        
        HostInfo first = new HostInfo("myAgent1", 111, "myHost1", "myOS1", "myKernel1", "myCPU1", 4, 400000000L);
        HostInfo second = new HostInfo("myAgent2", 222, "myHost2", "myOS2", "myKernel2", "myCPU2", 2, 800000000L);
        List<HostInfo> infos = Arrays.asList(first, second);
        
        String json = adapter.toJson(infos);
        assertJsonEquals(expected, json);
    }

}

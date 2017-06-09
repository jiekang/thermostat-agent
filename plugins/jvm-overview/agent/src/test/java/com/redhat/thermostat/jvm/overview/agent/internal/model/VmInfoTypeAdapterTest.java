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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.redhat.thermostat.jvm.overview.agent.internal.model.VmInfoDAOImpl.VmInfoUpdate;
import com.redhat.thermostat.jvm.overview.agent.internal.model.VmInfoTypeAdapter.VmInfoUpdateTypeAdapter;
import com.redhat.thermostat.jvm.overview.agent.model.VmInfo;
import org.junit.Test;

public class VmInfoTypeAdapterTest {
    
    @Test
    public void testWrite() throws Exception {
        VmInfoTypeAdapter adapter = new VmInfoTypeAdapter();
        final String expected = "[{\"agentId\":\"agent1\",\"vmId\":\"vm1\",\"vmPid\":8000,"
                + "\"startTimeStamp\":{\"$numberLong\":\"50000\"},\"stopTimeStamp\":"
                + "{\"$numberLong\":\"-9223372036854775808\"},\"javaVersion\":\"1.8.0\","
                + "\"javaHome\":\"/path/to/java\",\"mainClass\":\"myClass\",\"javaCommandLine\":\"java myClass\","
                + "\"vmName\":\"myJVM\",\"vmArguments\":\"-Dhello\",\"vmInfo\":\"interesting\","
                + "\"vmVersion\":\"1800\",\"propertiesAsArray\":[{\"key\":\"A\",\"value\":\"B\"},"
                + "{\"key\":\"C\",\"value\":\"D\"}],\"environmentAsArray\":[{\"key\":\"E\",\"value\":\"F\"},"
                + "{\"key\":\"G\",\"value\":\"H\"}],\"loadedNativeLibraries\":[],\"uid\":{\"$numberLong\":\"1234\"},"
                + "\"username\":\"test\"},"
                + "{\"agentId\":\"agent2\",\"vmId\":\"vm2\",\"vmPid\":9000,\"startTimeStamp\":"
                + "{\"$numberLong\":\"100000\"},\"stopTimeStamp\":{\"$numberLong\":\"200000\"},"
                + "\"javaVersion\":\"1.7.0\",\"javaHome\":\"/path/to/jre\",\"mainClass\":\"myOtherClass\","
                + "\"javaCommandLine\":\"otherClass.sh\",\"vmName\":\"myOtherJVM\",\"vmArguments\":\"-Dworld\","
                + "\"vmInfo\":\"info\",\"vmVersion\":\"1700\",\"propertiesAsArray\":[],\"environmentAsArray\":"
                + "[{\"key\":\"A\",\"value\":\"B\"},{\"key\":\"C\",\"value\":\"D\"}],"
                + "\"loadedNativeLibraries\":[\"libhello\",\"libworld\"],\"uid\":{\"$numberLong\":\"5678\"}"
                + ",\"username\":\"user\"}]";
        
        final Map<String, String> props = new HashMap<>();
        props.put("A", "B");
        props.put("C", "D");
        final Map<String, String> env = new HashMap<>();
        env.put("E", "F");
        env.put("G", "H");
        VmInfo first = new VmInfo("agent1", "vm1", 8000, 50000L, Long.MIN_VALUE, "1.8.0", "/path/to/java", "myClass", 
                "java myClass", "myJVM", "interesting", "1800", "-Dhello", props, env, new String[0], 1234L, "test");
        
        final Map<String, String> props2 = new HashMap<>();
        final Map<String, String> env2 = new HashMap<>();
        env2.put("A", "B");
        env2.put("C", "D");
        final String[] libs = { "libhello", "libworld" };
        VmInfo second = new VmInfo("agent2", "vm2", 9000, 100000L, 200000L, "1.7.0", "/path/to/jre", "myOtherClass",
                                   "otherClass.sh", "myOtherJVM", "info", "1700", "-Dworld", props2, env2, libs, 5678L, "user");
        List<VmInfo> infos = Arrays.asList(first, second);
        
        String json = adapter.toJson(infos);
        assertEquals(expected, json);
    }
    
    @Test
    public void testUpdate() throws Exception {
        VmInfoUpdateTypeAdapter adapter = new VmInfoUpdateTypeAdapter();
        final String expected = "{\"set\":{\"stopTimeStamp\":{\"$numberLong\":\"5000\"}}}";
        
        VmInfoUpdate update = new VmInfoUpdate(5000L);
        String json = adapter.toJson(update);
        assertEquals(expected, json);
    }
    
}

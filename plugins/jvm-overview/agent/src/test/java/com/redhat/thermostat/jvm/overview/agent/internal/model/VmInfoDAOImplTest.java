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
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.redhat.thermostat.agent.http.HttpRequestService;
import com.redhat.thermostat.jvm.overview.agent.internal.model.VmInfoDAOImpl.JsonHelper;
import com.redhat.thermostat.jvm.overview.agent.internal.model.VmInfoDAOImpl.VmInfoUpdate;
import com.redhat.thermostat.jvm.overview.agent.model.VmInfo;

public class VmInfoDAOImplTest {

    private static final String URL = "http://localhost:26000/api/v100/vm-info/systems/*/agents/foo-agent";
    private static final String UPDATE_URL = URL + "/jvms/vmId";
    private static final String SOME_JSON = "{\"some\" : \"json\"}";
    private static final String SOME_OTHER_JSON = "{\"some\" : {\"other\" : \"json\"}}";
    
    private VmInfo info;
    private JsonHelper jsonHelper;
    private HttpRequestService httpRequestService;

    @Before
    public void setUp() throws Exception {
        String vmId = "vmId";
        int vmPid = 1;
        long startTime = 2L;
        long stopTime = Long.MIN_VALUE;
        String jVersion = "java 1.0";
        String jHome = "/path/to/jdk/home";
        String mainClass = "Hello.class";
        String commandLine = "World";
        String vmArgs = "-XX=+FastestJITPossible";
        String vmName = "Hotspot";
        String vmInfo = "Some info";
        String vmVersion = "1.0";
        Map<String, String> props = new HashMap<>();
        Map<String, String> env = new HashMap<>();
        String[] libs = new String[0];
        long uid = 2000L;
        String username = "myUser";
        info = new VmInfo("foo-agent", vmId, vmPid, startTime, stopTime, jVersion, jHome,
                mainClass, commandLine, vmName, vmInfo, vmVersion, vmArgs,
                props, env, libs, uid, username);
        
        httpRequestService = mock(HttpRequestService.class);
        jsonHelper = mock(JsonHelper.class);
        when(jsonHelper.toJson(anyListOf(VmInfo.class))).thenReturn(SOME_JSON);
        when(jsonHelper.toJson(any(VmInfoUpdate.class))).thenReturn(SOME_OTHER_JSON);
    }

    @Ignore("Re-enable when proper /jvms endpoint is being used")
    @Test
    public void testPutVmInfo() throws Exception {
        VmInfoDAOImpl dao = new VmInfoDAOImpl(jsonHelper);
        dao.bindHttpRequestService(httpRequestService);
        dao.putVmInfo(info);
        
        verify(jsonHelper).toJson(eq(Arrays.asList(info)));
        verify(httpRequestService).sendHttpRequest(SOME_JSON, URL, HttpRequestService.POST);
    }

    @Ignore("Re-enable when proper /jvms endpoint is being used")
    @Test
    public void testPutVmStoppedTime() throws Exception {
        VmInfoDAOImpl dao = new VmInfoDAOImpl(jsonHelper);
        dao.bindHttpRequestService(httpRequestService);
        
        dao.putVmStoppedTime("foo-agent", "vmId", 3L);

        ArgumentCaptor<VmInfoUpdate> updateCaptor = ArgumentCaptor.forClass(VmInfoUpdate.class);
        verify(jsonHelper).toJson(updateCaptor.capture());
        VmInfoUpdate update = updateCaptor.getValue();
        assertEquals(3L, update.getStoppedTime());
                
        verify(httpRequestService).sendHttpRequest(SOME_OTHER_JSON, UPDATE_URL, HttpRequestService.PUT);
    }

}


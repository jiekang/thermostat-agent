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

package com.redhat.thermostat.storage.internal.dao;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.redhat.thermostat.storage.dao.VmInfoDAO;
import com.redhat.thermostat.storage.internal.StorageCoreConfiguration;
import com.redhat.thermostat.storage.internal.dao.VmInfoDAOImpl.HttpHelper;
import com.redhat.thermostat.storage.internal.dao.VmInfoDAOImpl.JsonHelper;
import com.redhat.thermostat.storage.internal.dao.VmInfoDAOImpl.VmInfoUpdate;
import com.redhat.thermostat.storage.model.VmInfo;

public class VmInfoDAOTest {

    private static final String URL = "http://localhost:26000/api/v100/vm-info/systems/*/agents/foo-agent";
    private static final String UPDATE_URL = URL + "/jvms/vmId";
    private static final String SOME_JSON = "{\"some\" : \"json\"}";
    private static final String SOME_OTHER_JSON = "{\"some\" : {\"other\" : \"json\"}}";
    private static final String CONTENT_TYPE = "application/json";
    
    private VmInfo info;
    private JsonHelper jsonHelper;
    private HttpHelper httpHelper;
    private StringContentProvider contentProvider;
    private Request request;
    private ContentResponse response;
    private StorageCoreConfiguration config;

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
        
        httpHelper = mock(HttpHelper.class);
        contentProvider = mock(StringContentProvider.class);
        when(httpHelper.createContentProvider(anyString())).thenReturn(contentProvider);
        request = mock(Request.class);
        when(httpHelper.newMockRequest(anyString())).thenReturn(request);
        response = mock(ContentResponse.class);
        when(response.getStatus()).thenReturn(HttpStatus.OK_200);
        when(request.send()).thenReturn(response);
        
        jsonHelper = mock(JsonHelper.class);
        when(jsonHelper.toJson(anyListOf(VmInfo.class))).thenReturn(SOME_JSON);
        when(jsonHelper.toJson(any(VmInfoUpdate.class))).thenReturn(SOME_OTHER_JSON);

        config = mock(StorageCoreConfiguration.class);
        when(config.getGatewayURL()).thenReturn(URL);
    }

    @Test
    @Ignore
    public void testPutVmInfo() throws Exception {
        VmInfoDAO dao = new VmInfoDAOImpl(config, httpHelper, jsonHelper);
        dao.putVmInfo(info);
        
        verify(httpHelper).newMockRequest(URL);
        verify(request).method(HttpMethod.POST);
        verify(jsonHelper).toJson(eq(Arrays.asList(info)));
        verify(httpHelper).createContentProvider(SOME_JSON);
        verify(request).content(contentProvider, CONTENT_TYPE);
        verify(request).send();
        verify(response).getStatus();
    }

    @Test
    @Ignore
    public void testPutVmStoppedTime() throws Exception {
        VmInfoDAO dao = new VmInfoDAOImpl(config, httpHelper, jsonHelper);
        dao.putVmStoppedTime("foo-agent", "vmId", 3L);

        verify(httpHelper).newMockRequest(UPDATE_URL);
        verify(request).method(HttpMethod.PUT);
        
        ArgumentCaptor<VmInfoUpdate> updateCaptor = ArgumentCaptor.forClass(VmInfoUpdate.class);
        verify(jsonHelper).toJson(updateCaptor.capture());
        VmInfoUpdate update = updateCaptor.getValue();
        assertEquals(3L, update.getStoppedTime());
                
        verify(httpHelper).createContentProvider(SOME_OTHER_JSON);
        verify(request).content(contentProvider, CONTENT_TYPE);
        verify(request).send();
        verify(response).getStatus();
    }

}


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

package com.redhat.thermostat.host.overview.common.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;

import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;

import com.redhat.thermostat.host.overview.common.HostInfoDAO;
import com.redhat.thermostat.host.overview.common.internal.HostInfoDAOImpl.HttpHelper;
import com.redhat.thermostat.host.overview.common.internal.HostInfoDAOImpl.JsonHelper;
import com.redhat.thermostat.host.overview.common.model.HostInfo;
import com.redhat.thermostat.storage.core.Key;

public class HostInfoDAOTest {

    private static final String URL = "http://localhost:26000/api/v100/host-info/systems/*/agents/foo-agent";
    private static final String SOME_JSON = "{\"some\" : \"json\"}";
    private static final String HOST_NAME = "a host name";
    private static final String OS_NAME = "some os";
    private static final String OS_KERNEL = "some kernel";
    private static final String CPU_MODEL = "some cpu that runs fast";
    private static final int CPU_NUM = -1;
    private static final long MEMORY_TOTAL = 0xCAFEBABEl;
    private static final String CONTENT_TYPE = "application/json";
    
    private HostInfo info;
    private JsonHelper jsonHelper;
    private HttpHelper httpHelper;
    private StringContentProvider contentProvider;
    private Request request;
    private ContentResponse response;
    
    @Before
    public void setup() throws Exception {
        info = new HostInfo("foo-agent", HOST_NAME, OS_NAME, OS_KERNEL, CPU_MODEL, CPU_NUM, MEMORY_TOTAL);
        
        httpHelper = mock(HttpHelper.class);
        contentProvider = mock(StringContentProvider.class);
        when(httpHelper.createContentProvider(anyString())).thenReturn(contentProvider);
        request = mock(Request.class);
        when(httpHelper.newRequest(anyString())).thenReturn(request);
        response = mock(ContentResponse.class);
        when(response.getStatus()).thenReturn(HttpStatus.OK_200);
        when(request.send()).thenReturn(response);
        
        jsonHelper = mock(JsonHelper.class);
        when(jsonHelper.toJson(anyListOf(HostInfo.class))).thenReturn(SOME_JSON);
    }

    @Test
    public void testCategory() {
        assertEquals("host-info", HostInfoDAO.hostInfoCategory.getName());
        Collection<Key<?>> keys = HostInfoDAO.hostInfoCategory.getKeys();
        assertTrue(keys.contains(new Key<>("agentId")));
        assertTrue(keys.contains(new Key<String>("hostname")));
        assertTrue(keys.contains(new Key<String>("osName")));
        assertTrue(keys.contains(new Key<String>("osKernel")));
        assertTrue(keys.contains(new Key<String>("cpuModel")));
        assertTrue(keys.contains(new Key<Integer>("cpuCount")));
        assertTrue(keys.contains(new Key<Long>("totalMemory")));
        assertEquals(7, keys.size());
    }

    @Test
    public void testPutHostInfo() throws Exception {
        HostInfoDAOImpl dao = new HostInfoDAOImpl(httpHelper, jsonHelper);
        dao.activate();
        dao.putHostInfo(info);
        
        verify(httpHelper).newRequest(URL);
        verify(request).method(HttpMethod.POST);
        verify(jsonHelper).toJson(eq(Arrays.asList(info)));
        verify(httpHelper).createContentProvider(SOME_JSON);
        verify(request).content(contentProvider, CONTENT_TYPE);
        verify(request).send();
        verify(response).getStatus();
    }

}


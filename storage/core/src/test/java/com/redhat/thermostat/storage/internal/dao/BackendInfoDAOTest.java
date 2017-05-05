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

import com.redhat.thermostat.storage.core.Category;
import com.redhat.thermostat.storage.core.Key;
import com.redhat.thermostat.storage.dao.BackendInfoDAO;
import com.redhat.thermostat.storage.internal.dao.BackendInfoDAOImpl.HttpHelper;
import com.redhat.thermostat.storage.internal.dao.BackendInfoDAOImpl.JsonHelper;
import com.redhat.thermostat.storage.model.BackendInformation;

public class BackendInfoDAOTest {
    
    private static final String URL = "http://localhost:26000/api/v100/backend-info/systems/*/agents/foo-agent1";
    private static final String REMOVE_URL = URL + "?q=name%3D%3DTest+Backend";
    private static final String SOME_JSON = "{\"some\" : \"json\"}";
    private static final String CONTENT_TYPE = "application/json";

    private BackendInformation backendInfo1;
    private JsonHelper jsonHelper;
    private HttpHelper httpHelper;
    private StringContentProvider contentProvider;
    private Request request;
    private ContentResponse response;

    @Before
    public void setUp() throws Exception {
        backendInfo1 = new BackendInformation("foo-agent1");

        backendInfo1.setName("Test Backend");
        backendInfo1.setDescription("description");
        backendInfo1.setActive(true);
        backendInfo1.setObserveNewJvm(true);
        backendInfo1.setPids(new int[] { -1, 0, 1});
        backendInfo1.setOrderValue(100);

        httpHelper = mock(HttpHelper.class);
        contentProvider = mock(StringContentProvider.class);
        when(httpHelper.createContentProvider(anyString())).thenReturn(contentProvider);
        request = mock(Request.class);
        when(httpHelper.newRequest(anyString())).thenReturn(request);
        response = mock(ContentResponse.class);
        when(response.getStatus()).thenReturn(HttpStatus.OK_200);
        when(request.send()).thenReturn(response);
        
        jsonHelper = mock(JsonHelper.class);
        when(jsonHelper.toJson(anyListOf(BackendInformation.class))).thenReturn(SOME_JSON);
    }
    
    @Test
    public void verifyCategoryName() {
        Category<BackendInformation> c = BackendInfoDAO.CATEGORY;
        assertEquals("backend-info", c.getName());
    }

    @Test
    public void verifyCategoryHasAllKeys() {
        Category<BackendInformation> c = BackendInfoDAO.CATEGORY;
        Collection<Key<?>> keys = c.getKeys();

        assertTrue(keys.contains(Key.AGENT_ID));
        assertTrue(keys.contains(BackendInfoDAO.BACKEND_NAME));
        assertTrue(keys.contains(BackendInfoDAO.BACKEND_DESCRIPTION));
        assertTrue(keys.contains(BackendInfoDAO.IS_ACTIVE));
        assertTrue(keys.contains(BackendInfoDAO.PIDS_TO_MONITOR));
        assertTrue(keys.contains(BackendInfoDAO.SHOULD_MONITOR_NEW_PROCESSES));
        assertTrue(keys.contains(BackendInfoDAO.ORDER_VALUE));
    }

    @Test
    public void verifyAddBackendInformation() throws Exception {
        BackendInfoDAO dao = new BackendInfoDAOImpl(httpHelper, jsonHelper);

        dao.addBackendInformation(backendInfo1);
        
        verify(httpHelper).newRequest(URL);
        verify(request).method(HttpMethod.POST);
        verify(jsonHelper).toJson(eq(Arrays.asList(backendInfo1)));
        verify(httpHelper).createContentProvider(SOME_JSON);
        verify(request).content(contentProvider, CONTENT_TYPE);
        verify(request).send();
        verify(response).getStatus();
    }

    @Test
    public void verifyRemoveBackendInformation() throws Exception {
        BackendInfoDAO dao = new BackendInfoDAOImpl(httpHelper, jsonHelper);
        
        dao.removeBackendInformation(backendInfo1);
        
        verify(httpHelper).newRequest(REMOVE_URL);
        verify(request).method(HttpMethod.DELETE);
        verify(request).send();
        verify(response).getStatus();
    }

}


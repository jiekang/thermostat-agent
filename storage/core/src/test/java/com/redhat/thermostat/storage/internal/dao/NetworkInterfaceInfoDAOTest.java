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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.redhat.thermostat.storage.dao.NetworkInterfaceInfoDAO;
import com.redhat.thermostat.storage.internal.dao.NetworkInterfaceInfoDAOImpl.HttpHelper;
import com.redhat.thermostat.storage.internal.dao.NetworkInterfaceInfoDAOImpl.JsonHelper;
import com.redhat.thermostat.storage.internal.dao.NetworkInterfaceInfoDAOImpl.NetworkInterfaceInfoUpdate;
import com.redhat.thermostat.storage.model.NetworkInterfaceInfo;

public class NetworkInterfaceInfoDAOTest {

    private static final String URL = "http://localhost:26000/api/v100/network-info/systems/*/agents/fooAgent";
    private static final String QUERY_URL = URL + "?q=interfaceName%3D%3Dsome+interface.+maybe+eth0";
    private static final String INTERFACE_NAME = "some interface. maybe eth0";
    private static final String IPV4_ADDR = "256.256.256.256";
    private static final String IPV6_ADDR = "100:100:100::::1";
    private static final String SOME_JSON = "{\"some\" : \"json\"}";
    private static final String SOME_OTHER_JSON = "{\"some\" : {\"other\" : \"json\"}}";
    private static final String EMPTY_JSON = "{}";
    private static final String CONTENT_TYPE = "application/json";

    private NetworkInterfaceInfo info;
    private JsonHelper jsonHelper;
    private HttpHelper httpHelper;
    private StringContentProvider contentProvider;
    private Request request;
    private ContentResponse response;
    
    @Before
    public void setup() throws Exception {
        info = new NetworkInterfaceInfo("fooAgent", INTERFACE_NAME);
        info.setIp4Addr(IPV4_ADDR);
        info.setIp6Addr(IPV6_ADDR);
        
        httpHelper = mock(HttpHelper.class);
        contentProvider = mock(StringContentProvider.class);
        when(httpHelper.createContentProvider(anyString())).thenReturn(contentProvider);
        request = mock(Request.class);
        when(httpHelper.newRequest(anyString())).thenReturn(request);
        response = mock(ContentResponse.class);
        when(response.getStatus()).thenReturn(HttpStatus.OK_200);
        when(request.send()).thenReturn(response);
        
        jsonHelper = mock(JsonHelper.class);
        when(jsonHelper.toJson(anyListOf(NetworkInterfaceInfo.class))).thenReturn(SOME_JSON);
        when(jsonHelper.toJson(any(NetworkInterfaceInfoUpdate.class))).thenReturn(SOME_OTHER_JSON);
        List<NetworkInterfaceInfo> emptyList = Collections.emptyList(); 
        when(jsonHelper.fromJson(EMPTY_JSON)).thenReturn(emptyList);
        when(jsonHelper.fromJson(SOME_JSON)).thenReturn(Arrays.asList(info));
    }

    @Test
    public void testPutNetworkInterfaceInfoAdd() throws Exception {
        NetworkInterfaceInfoDAO dao = new NetworkInterfaceInfoDAOImpl(httpHelper, jsonHelper);
        when(response.getContentAsString()).thenReturn(EMPTY_JSON);
        dao.putNetworkInterfaceInfo(info);
        
        // Check query first
        verify(httpHelper).newRequest(QUERY_URL);
        verify(request).method(HttpMethod.GET);
        verify(jsonHelper).fromJson(EMPTY_JSON);

        // Check data added
        verify(httpHelper).newRequest(URL);
        verify(request).method(HttpMethod.POST);
        verify(jsonHelper).toJson(eq(Arrays.asList(info)));
        verify(httpHelper).createContentProvider(SOME_JSON);
        verify(request).content(contentProvider, CONTENT_TYPE);
        
        verify(request, times(2)).send();
        verify(response, times(2)).getStatus();
    }
    
    @Test
    public void testPutNetworkInterfaceInfoUpdate() throws Exception {
        NetworkInterfaceInfoDAO dao = new NetworkInterfaceInfoDAOImpl(httpHelper, jsonHelper);
        
        when(response.getContentAsString()).thenReturn(SOME_JSON);
        NetworkInterfaceInfo other = new NetworkInterfaceInfo("fooAgent", INTERFACE_NAME);
        other.setIp4Addr("1.2.3.4");
        other.setIp6Addr(IPV6_ADDR);
        when(jsonHelper.fromJson(SOME_JSON)).thenReturn(Arrays.asList(other));
        dao.putNetworkInterfaceInfo(info);
        
        // Check query first
        verify(request).method(HttpMethod.GET);
        verify(jsonHelper).fromJson(SOME_JSON);

        // Check data updated
        verify(httpHelper, times(2)).newRequest(QUERY_URL);
        verify(request).method(HttpMethod.PUT);
        
        ArgumentCaptor<NetworkInterfaceInfoUpdate> updateCaptor = ArgumentCaptor.forClass(NetworkInterfaceInfoUpdate.class);
        verify(jsonHelper).toJson(updateCaptor.capture());
        NetworkInterfaceInfoUpdate update = updateCaptor.getValue();
        assertEquals(IPV4_ADDR, update.getIPv4Addr());
        assertEquals(IPV6_ADDR, update.getIPv6Addr());
        
        verify(httpHelper).createContentProvider(SOME_OTHER_JSON);
        verify(request).content(contentProvider, CONTENT_TYPE);
        
        verify(request, times(2)).send();
        verify(response, times(2)).getStatus();
    }

    @Test
    public void testPutNetworkInterfaceInfoNoUpdate() throws Exception {
        NetworkInterfaceInfoDAO dao = new NetworkInterfaceInfoDAOImpl(httpHelper, jsonHelper);
        
        when(response.getContentAsString()).thenReturn(SOME_JSON);
        dao.putNetworkInterfaceInfo(info);
        
        // Check query first
        verify(httpHelper).newRequest(QUERY_URL);
        verify(request).method(HttpMethod.GET);
        verify(jsonHelper).fromJson(SOME_JSON);
        verify(request).send();
        verify(response).getStatus();

        // Check no update sent
        verify(request, never()).method(HttpMethod.PUT);
        verify(jsonHelper, never()).toJson(any(NetworkInterfaceInfoUpdate.class));
        verify(httpHelper, never()).createContentProvider(SOME_OTHER_JSON);
        verify(request, never()).content(contentProvider, CONTENT_TYPE);
    }
}


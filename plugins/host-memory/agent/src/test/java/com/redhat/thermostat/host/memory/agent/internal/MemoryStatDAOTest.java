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

package com.redhat.thermostat.host.memory.agent.internal;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.redhat.thermostat.agent.http.HttpRequestService;
import com.redhat.thermostat.common.Clock;
import com.redhat.thermostat.common.SystemClock;
import com.redhat.thermostat.common.config.experimental.ConfigurationInfoSource;
import com.redhat.thermostat.common.plugin.SystemID;
import com.redhat.thermostat.common.plugin.PluginConfiguration;

import com.redhat.thermostat.host.memory.model.MemoryStat;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;

import org.mockito.Matchers;

public class MemoryStatDAOTest {

    private static final String URL = "http://localhost:26000/api/system-memory/0.0.1";
    private static final String SOME_JSON = "{\"some\" : \"json\"}";
    private static final String CONTENT_TYPE = "application/json";
    private static final String HOST_NAME = "somehostname";

    private static final String URL_PROP = "gatewayURL";

    private MemoryStat info;
    private MemoryStatDAOImpl.JsonHelper jsonHelper;
    private ConfigurationInfoSource cfiSource;
    private MemoryStatDAOImpl.ConfigurationCreator configCreator;
    private SystemID idservice;
    private HttpRequestService httpRequestService;

    @Before
    public void setup() throws Exception {
        Clock clock = new SystemClock();
        this.info = new MemoryStat("foo-agent", clock.getRealTimeMillis(), 0, 0, 0, 0, 0, 0, 0);

        this.jsonHelper = mock(MemoryStatDAOImpl.JsonHelper.class);
        when(jsonHelper.toJson(anyListOf(MemoryStat.class))).thenReturn(SOME_JSON);

        Request request = mock(Request.class);
        HttpClient httpClient = mock(HttpClient.class);
        when(httpClient.newRequest(anyString())).thenReturn(request);
        ContentResponse response = mock(ContentResponse.class);
        when(response.getStatus()).thenReturn(HttpStatus.OK_200);
        when(request.send()).thenReturn(response);

        cfiSource = mock(ConfigurationInfoSource.class);
        Map<String, String> map = new HashMap<>();
        map.put(URL_PROP, URL);
        when(cfiSource.getConfiguration(anyString(), anyString())).thenReturn(map);

        httpRequestService = mock(HttpRequestService.class);
        ContentResponse contentResponse = mock(ContentResponse.class);
        when(httpRequestService.sendHttpRequest(anyString(), anyString(), any(HttpMethod.class))).thenReturn(contentResponse);
        when(contentResponse.getStatus()).thenReturn(HttpStatus.OK_200);

        configCreator = mock(MemoryStatDAOImpl.ConfigurationCreator.class);
        when(configCreator.create(eq(cfiSource))).thenReturn(new PluginConfiguration(cfiSource, MemoryStatDAOImpl.PLUGIN_ID));

        idservice = mock(SystemID.class);
        when(idservice.getSystemID()).thenReturn(HOST_NAME);
    }

    @Test
    public void testPut() throws Exception {

        MemoryStatDAOImpl dao = new MemoryStatDAOImpl(jsonHelper, configCreator);
        dao.bindSystemID(idservice);
        dao.bindConfigurationInfoSource(cfiSource);
        dao.bindHttpRequestService(httpRequestService);
        dao.activate();
        dao.put(info);

        verify(httpRequestService, times(1)).sendHttpRequest(SOME_JSON, URL + "/systems/" + HOST_NAME, HttpMethod.POST);
    }
}


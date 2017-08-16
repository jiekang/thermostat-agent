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

package com.redhat.thermostat.vm.memory.agent.internal.models;

import static com.redhat.thermostat.vm.memory.agent.internal.models.VmTlabStatDAOImpl.HttpHelper;
import static com.redhat.thermostat.vm.memory.agent.internal.models.VmTlabStatDAOImpl.JsonHelper;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.Arrays;

import com.redhat.thermostat.common.config.experimental.ConfigurationInfoSource;
import com.redhat.thermostat.common.plugin.PluginConfiguration;
import com.redhat.thermostat.vm.memory.agent.model.VmTlabStat;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class VmTlabStatDAOTest {

    private static final String JSON = "{\"this\":\"is\",\"test\":\"JSON\"}";
    private static final String VM_ID = "0xcafe";
    private static final String AGENT_ID = "agent";
    private static final String CONTENT_TYPE = "application/json";
    private static final URI GATEWAY_URI = URI.create("http://example.com/jvm-memory/0.0.2/");
    
    private HttpClient httpClient;
    private HttpHelper httpHelper;
    private JsonHelper jsonHelper;
    private StringContentProvider contentProvider;
    private Request request;
    private ContentResponse response;
    private PluginConfiguration config;

    @Before
    public void setUp() throws Exception {
        httpClient = mock(HttpClient.class);
        request = mock(Request.class);
        when(httpClient.newRequest(anyString())).thenReturn(request);
        response = mock(ContentResponse.class);
        when(response.getStatus()).thenReturn(HttpStatus.OK_200);
        when(request.send()).thenReturn(response);

        httpHelper = mock(HttpHelper.class);
        contentProvider = mock(StringContentProvider.class);
        when(httpHelper.createContentProvider(anyString())).thenReturn(contentProvider);
        jsonHelper = mock(JsonHelper.class);
        when(jsonHelper.toJson(anyListOf(VmTlabStat.class))).thenReturn(JSON);
        
        config = mock(PluginConfiguration.class);
        when(config.getGatewayURL()).thenReturn(GATEWAY_URI);
    }

    @Test
    public void testActivation() throws Exception {
        PluginConfiguration pluginConfig = mock(PluginConfiguration.class);
        when(pluginConfig.getGatewayURL()).thenReturn(GATEWAY_URI);
        VmTlabStatDAOImpl.ConfigurationCreator configCreator = mock(VmTlabStatDAOImpl.ConfigurationCreator.class);
        ConfigurationInfoSource configInfoSource = mock(ConfigurationInfoSource.class);
        when(configCreator.create(configInfoSource)).thenReturn(pluginConfig);

        VmTlabStatDAOImpl dao = new VmTlabStatDAOImpl(httpClient, httpHelper, jsonHelper, configCreator, configInfoSource);
        dao.activate();

        verify(pluginConfig, times(1)).getGatewayURL();
        verify(httpHelper, times(1)).startClient(httpClient);
    }

    @Test
    @Ignore
    public void verifyPutStat() throws Exception {
//      TODO: Remove @Ignore when web-gateway service for TLAB stats is available
//      See VmTlabStatDAOImpl.putStat()

        VmTlabStat stat = new VmTlabStat();
        stat.setAgentId(AGENT_ID);
        stat.setJvmId(VM_ID);
        stat.setTimeStamp(1000l);
        stat.setTotalAllocatingThreads(10l);
        stat.setTotalAllocations(1342l);
        stat.setTotalRefills(58l);
        stat.setMaxRefills(90l);
        stat.setTotalSlowAllocations(343l);
        stat.setMaxSlowAllocations(989l);
        stat.setTotalGcWaste(788l);
        stat.setMaxGcWaste(992l);
        stat.setTotalSlowWaste(899l);
        stat.setMaxSlowWaste(634l);
        stat.setTotalFastWaste(678l);
        stat.setMaxFastWaste(333l);

        VmTlabStatDAOImpl.ConfigurationCreator configurationCreator = new VmTlabStatDAOImpl.ConfigurationCreator();
        ConfigurationInfoSource configInfoSource = mock(ConfigurationInfoSource.class);
        VmTlabStatDAO dao = new VmTlabStatDAOImpl(httpClient, httpHelper, jsonHelper, configurationCreator, configInfoSource);
        dao.putStat(stat);

        verify(httpClient).newRequest(GATEWAY_URI);
        verify(request).method(HttpMethod.POST);
        verify(jsonHelper).toJson(Arrays.asList(stat));
        verify(httpHelper).createContentProvider(JSON);
        verify(request).content(contentProvider, CONTENT_TYPE);
        verify(request).send();
        verify(response).getStatus();
    }
}

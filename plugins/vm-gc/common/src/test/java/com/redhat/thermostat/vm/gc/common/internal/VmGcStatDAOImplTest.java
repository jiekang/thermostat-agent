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

package com.redhat.thermostat.vm.gc.common.internal;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;

import com.redhat.thermostat.agent.http.HttpRequestService;
import com.redhat.thermostat.common.config.experimental.ConfigurationInfoSource;
import com.redhat.thermostat.common.plugins.PluginConfiguration;
import com.redhat.thermostat.vm.gc.common.internal.VmGcStatDAOImpl.ConfigurationCreator;
import com.redhat.thermostat.vm.gc.common.internal.VmGcStatDAOImpl.JsonHelper;
import com.redhat.thermostat.vm.gc.common.model.VmGcStat;

public class VmGcStatDAOImplTest {

    private static final String AGENT_ID = "some-agent";
    private static final String JSON = "{\"this\":\"is\",\"also\":\"JSON\"}";
    private static final String GATEWAY_URL = "http://example.com/jvm-gc";
    
    private VmGcStat stat;
    private JsonHelper jsonHelper;
    private VmGcStatDAOImpl dao;

    private HttpRequestService httpRequestService;

    @Before
    public void setup() throws Exception {
        stat = new VmGcStat();
        stat.setAgentId(AGENT_ID);
        stat.setTimeStamp(1234l);
        stat.setWallTime(4000l);
        stat.setRunCount(1000l);
        stat.setVmId("Vm-1");
        stat.setCollectorName("Collector");

        jsonHelper = mock(JsonHelper.class);
        when(jsonHelper.toJson(anyListOf(VmGcStat.class))).thenReturn(JSON);

        ConfigurationInfoSource source = mock(ConfigurationInfoSource.class);
        PluginConfiguration config = mock(PluginConfiguration.class);
        when(config.getGatewayURL()).thenReturn(GATEWAY_URL);
        ConfigurationCreator creator = mock(ConfigurationCreator.class);
        when(creator.create(source)).thenReturn(config);

        httpRequestService = mock(HttpRequestService.class);
        ContentResponse contentResponse = mock(ContentResponse.class);
        when(httpRequestService.sendHttpRequest(anyString(), anyString(), any(HttpMethod.class))).thenReturn(contentResponse);
        when(contentResponse.getStatus()).thenReturn(HttpStatus.OK_200);

        dao = new VmGcStatDAOImpl(jsonHelper, creator, source);
        dao.bindHttpRequestService(httpRequestService);
    }

    @Test
    public void verifyAddVmGcStat() throws Exception {
        dao.activate();
        dao.putVmGcStat(stat);

        verify(jsonHelper).toJson(eq(Arrays.asList(stat)));

        verify(httpRequestService.sendHttpRequest(JSON, GATEWAY_URL, HttpMethod.POST), times(1));
    }

}


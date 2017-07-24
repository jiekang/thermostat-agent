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

package com.redhat.thermostat.host.network.internal;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.ArrayList;

import org.eclipse.jetty.http.HttpMethod;
import org.junit.Before;
import org.junit.Test;

import com.redhat.thermostat.agent.http.HttpRequestService;
import com.redhat.thermostat.common.config.experimental.ConfigurationInfoSource;
import com.redhat.thermostat.common.plugin.PluginConfiguration;
import com.redhat.thermostat.common.plugin.SystemID;
import com.redhat.thermostat.host.network.internal.NetworkInfoListDAOImpl.ConfigurationCreator;
import com.redhat.thermostat.host.network.internal.NetworkInfoListDAOImpl.JsonHelper;
import com.redhat.thermostat.host.network.model.NetworkInfoList;
import com.redhat.thermostat.host.network.model.NetworkInterfaceInfo;

public class NetworkInfoListDAOTest {

    private static final URI GATEWAY_URI = URI.create("http://localhost:26000/api/v100/network-info/");
    private static final String INTERFACE_NAME = "some interface. maybe eth0";
    private static final long TIMESTAMP = 333;
    private static final String IPV4_ADDR = "256.256.256.256";
    private static final String IPV6_ADDR = "100:100:100::::1";
    private static final String SOME_JSON = "{\"some\" : \"json\"}";
    private static final String HOST_NAME = "somehostname";
    private static final String AGENT_ID = "xxx some agent";

    private JsonHelper jsonHelper;

    private ConfigurationInfoSource cfiSource;
    private NetworkInfoListDAOImpl.ConfigurationCreator configCreator;

    private SystemID idservice;
    private HttpRequestService httpRequestService;

    @Before
    public void setup() throws Exception {
        NetworkInterfaceInfo info = new NetworkInterfaceInfo(INTERFACE_NAME);
        info.setIp4Addr(IPV4_ADDR);
        info.setIp6Addr(IPV6_ADDR);
        
        jsonHelper = mock(JsonHelper.class);
        when(jsonHelper.toJson(any(NetworkInfoList.class))).thenReturn(SOME_JSON);

        cfiSource = mock(ConfigurationInfoSource.class);
        configCreator = mock(ConfigurationCreator.class);
        PluginConfiguration pluginConfig = mock(PluginConfiguration.class);
        when(pluginConfig.getGatewayURL()).thenReturn(GATEWAY_URI);
        when(configCreator.create(cfiSource)).thenReturn(pluginConfig);
        
        httpRequestService = mock(HttpRequestService.class);
        idservice = mock(SystemID.class);
        when(idservice.getSystemID()).thenReturn(HOST_NAME);
    }

    @Test
    public void testPut() throws Exception {

        NetworkInfoListDAOImpl dao = new NetworkInfoListDAOImpl(jsonHelper, configCreator);
        dao.bindSystemID(idservice);
        dao.bindConfigurationInfoSource(cfiSource);
        dao.bindHttpRequestService(httpRequestService);
        dao.activate();

        NetworkInfoList obj = new NetworkInfoList(AGENT_ID, TIMESTAMP, new ArrayList<NetworkInterfaceInfo>());
        dao.put(obj);

        verify(httpRequestService, times(1)).sendHttpRequest(SOME_JSON, GATEWAY_URI.resolve("systems/" + HOST_NAME), HttpMethod.POST.asString());
    }
}


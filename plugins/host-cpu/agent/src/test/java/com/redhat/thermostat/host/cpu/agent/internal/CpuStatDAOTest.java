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

package com.redhat.thermostat.host.cpu.agent.internal;

import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.redhat.thermostat.agent.http.HttpRequestService;
import com.redhat.thermostat.common.Clock;
import com.redhat.thermostat.common.SystemClock;
import com.redhat.thermostat.common.config.experimental.ConfigurationInfoSource;
import com.redhat.thermostat.common.plugin.PluginConfiguration;
import com.redhat.thermostat.common.plugin.SystemID;
import com.redhat.thermostat.host.cpu.model.CpuStat;

public class CpuStatDAOTest {

    private static final String URL = "http://localhost:26000/api/system-cpu/0.0.1";
    private static final String SOME_JSON = "{\"some\" : \"json\"}";
    private static final double times[] = { 33., 44, };
    private static final String HOST_NAME = "somehostname";

    private static final String URL_PROP = "gatewayURL";

    private CpuStat info;
    private CpuStatDAOImpl.JsonHelper jsonHelper;
    private ConfigurationInfoSource cfiSource;
    private CpuStatDAOImpl.ConfigurationCreator configCreator;

    private SystemID idservice;
    private HttpRequestService httpRequestService;

    @Before
    public void setup() throws Exception {
        Clock clock = new SystemClock();
        info = new CpuStat("foo-agent", clock.getRealTimeMillis(), times);

        jsonHelper = mock(CpuStatDAOImpl.JsonHelper.class);
        when(jsonHelper.toJson(anyListOf(CpuStat.class))).thenReturn(SOME_JSON);

        cfiSource = mock(ConfigurationInfoSource.class);
        Map<String,String> map = new HashMap<>();
        map.put(URL_PROP, URL);
        when(cfiSource.getConfiguration(anyString(),anyString())).thenReturn(map);

        configCreator = mock(CpuStatDAOImpl.ConfigurationCreator.class);
        when(configCreator.create(eq(cfiSource))).thenReturn(new PluginConfiguration(cfiSource, CpuStatDAOImpl.PLUGIN_ID));

        httpRequestService = mock(HttpRequestService.class);
        idservice = mock(SystemID.class);
        when(idservice.getSystemID()).thenReturn(HOST_NAME);
    }

    @Test
    public void testPut() throws Exception {

        CpuStatDAOImpl dao = new CpuStatDAOImpl(jsonHelper, configCreator);
        dao.bindSystemID(idservice);
        dao.bindConfigurationInfoSource(cfiSource);
        dao.bindHttpRequestService(httpRequestService);
        dao.activate();
        dao.put(info);

        verify(httpRequestService, times(1)).sendHttpRequest(SOME_JSON, URL + "/systems/" + HOST_NAME, HttpRequestService.POST);
    }

}

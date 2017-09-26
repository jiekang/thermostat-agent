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

package com.redhat.thermostat.vm.io.agent.internal;


import com.redhat.thermostat.vm.io.model.VmIoStat;
import com.redhat.thermostat.vm.io.agent.internal.VmIoStatDAOImpl.ConfigurationCreator;
import com.redhat.thermostat.vm.io.agent.internal.VmIoStatDAOImpl.JsonHelper;

import com.redhat.thermostat.agent.http.HttpRequestService;
import com.redhat.thermostat.common.config.experimental.ConfigurationInfoSource;
import com.redhat.thermostat.common.plugin.PluginConfiguration;
import com.redhat.thermostat.common.plugin.SystemID;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;

import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class VmIoStatDAOImplTest {

    private static final long SOME_TIMESTAMP = 1234;
    private static final String SOME_SYSTEM_ID = "somesystemid";
    private static final String SOME_VM_ID = "321";
    private static final long SOME_CHARACTERS_READ = 123456;
    private static final long SOME_CHARACTERS_WRITTEN = 67798;
    private static final long SOME_READ_SYSCALLS = 123456;
    private static final long SOME_WRITE_SYSCALLS = 67798;

    private static final String AGENT_ID = "some-agent";
    private static final String JSON = "{\"this\":\"is\",\"also\":\"JSON\"}";

    private static final URI GATEWAY_URI = URI.create("http://example.com/jvm-io/");

    private VmIoStat ioStat;
    private JsonHelper jsonHelper;
    private VmIoStatDAOImpl dao;
    private HttpRequestService httpRequestService;

    @Before
    public void setUp() throws IOException {
        this.ioStat = new VmIoStat("foo-agent", SOME_VM_ID, SOME_TIMESTAMP,
                SOME_CHARACTERS_READ, SOME_CHARACTERS_WRITTEN,
                SOME_READ_SYSCALLS, SOME_WRITE_SYSCALLS);

        jsonHelper = mock(JsonHelper.class);
        when(jsonHelper.toJson(anyListOf(VmIoStat.class))).thenReturn(JSON);

        ConfigurationInfoSource source = mock(ConfigurationInfoSource.class);
        PluginConfiguration config = mock(PluginConfiguration.class);
        when(config.getGatewayURL()).thenReturn(GATEWAY_URI);
        ConfigurationCreator creator = mock(ConfigurationCreator.class);
        when(creator.create(source)).thenReturn(config);

        httpRequestService = mock(HttpRequestService.class);
        dao = new VmIoStatDAOImpl(jsonHelper, creator);
        dao.bindHttpRequestService(httpRequestService);
        dao.bindConfigurationInfoSource(source);
    }

    @Test
    public void verifyPut() throws Exception {
        SystemID id = mock(SystemID.class);
        when(id.getSystemID()).thenReturn(SOME_SYSTEM_ID);
        dao.bindSystemID(id);
        dao.activate();
        dao.put(ioStat);

        verify(jsonHelper).toJson(eq(Arrays.asList(ioStat)));
        verify(httpRequestService).sendHttpRequest(JSON, GATEWAY_URI.resolve("systems/" + SOME_SYSTEM_ID + "/jvms/" + SOME_VM_ID), HttpRequestService.Method.POST);
    }


}

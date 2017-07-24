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

import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.redhat.thermostat.agent.http.HttpRequestService;
import com.redhat.thermostat.common.config.experimental.ConfigurationInfoSource;
import com.redhat.thermostat.common.plugin.PluginConfiguration;
import com.redhat.thermostat.vm.memory.agent.internal.models.VmMemoryStatDAOImpl.JsonHelper;
import com.redhat.thermostat.vm.memory.agent.model.VmMemoryStat;
import com.redhat.thermostat.vm.memory.agent.model.VmMemoryStat.Generation;
import com.redhat.thermostat.vm.memory.agent.model.VmMemoryStat.Space;

public class VmMemoryStatDAOImplTest {

    private static final String JSON = "{\"this\":\"is\",\"test\":\"JSON\"}";
    private static final URI GATEWAY_URI = URI.create("http://example.com/jvm-memory/0.0.2/");
    
    private JsonHelper jsonHelper;
    private PluginConfiguration config;
    VmMemoryStatDAOImpl.ConfigurationCreator creator;
    ConfigurationInfoSource source;
    private HttpRequestService httpRequestService;

    @Before
    public void setUp() throws Exception {
        jsonHelper = mock(JsonHelper.class);
        when(jsonHelper.toJson(anyListOf(VmMemoryStat.class))).thenReturn(JSON);
        
        config = mock(PluginConfiguration.class);
        when(config.getGatewayURL()).thenReturn(GATEWAY_URI);

        source = mock(ConfigurationInfoSource.class);

        creator = mock(VmMemoryStatDAOImpl.ConfigurationCreator.class);
        when(creator.create(source)).thenReturn(config);
        httpRequestService = mock(HttpRequestService.class);
    }

    @Test
    public void testPutVmMemoryStat() throws Exception {
        List<Generation> generations = new ArrayList<Generation>();

        int i = 0;
        for (String genName: new String[] { "new", "old", "perm" }) {
            Generation gen = new Generation();
            gen.setName(genName);
            gen.setCollector(gen.getName());
            generations.add(gen);
            List<Space> spaces = new ArrayList<Space>();
            String[] spaceNames = null;
            if (genName.equals("new")) {
                spaceNames = new String[] { "eden", "s0", "s1" };
            } else if (genName.equals("old")) {
                spaceNames = new String[] { "old" };
            } else {
                spaceNames = new String[] { "perm" };
            }
            for (String spaceName: spaceNames) {
                Space space = new Space();
                space.setName(spaceName);
                space.setIndex(0);
                space.setUsed(i++);
                space.setCapacity(i++);
                space.setMaxCapacity(i++);
                spaces.add(space);
            }
            gen.setSpaces(spaces.toArray(new Space[spaces.size()]));
        }
        VmMemoryStat stat = new VmMemoryStat("foo-agent", 1, "vmId", generations.toArray(new Generation[generations.size()]),
                2, 3, 4, 5);

        VmMemoryStatDAOImpl dao = new VmMemoryStatDAOImpl(jsonHelper, creator, source);
        dao.bindHttpRequestService(httpRequestService);
        dao.activate();
        
        dao.putVmMemoryStat(stat);

        verify(jsonHelper).toJson(Arrays.asList(stat));
        verify(httpRequestService).sendHttpRequest(JSON, GATEWAY_URI, HttpRequestService.POST);
    }
    
}


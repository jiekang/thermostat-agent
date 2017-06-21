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

package com.redhat.thermostat.agent.internal;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.redhat.thermostat.storage.model.BackendInformation;

public class BackendInformationTypeAdapterTest {
    
    private BackendInformationTypeAdapter adapter;
    
    @Before
    public void setup() {
        adapter = new BackendInformationTypeAdapter();
    }

    @Test
    public void testWrite() throws Exception {
        final String expected = "[{\"agentId\":\"agent1\",\"name\":\"First Backend\",\"description\":\"Gathers something\","
                + "\"observeNewJvm\":true,\"pids\":[8000,9000],\"active\":true,\"orderValue\":280},"
                + "{\"agentId\":\"agent2\",\"name\":\"Second Backend\",\"description\":\"Gathers something else\","
                + "\"observeNewJvm\":false,\"pids\":[],\"active\":false,\"orderValue\":200}]";
        
        BackendInformation first = createBackendInformation("agent1", "First Backend", "Gathers something", true, 
                new int[] { 8000, 9000 }, true, 280);
        BackendInformation second = createBackendInformation("agent2", "Second Backend", "Gathers something else", false, 
                new int[0], false, 200);
        List<BackendInformation> infos = Arrays.asList(first, second);
        
        String json = adapter.toJson(infos);
        assertEquals(expected, json);
    }
    
    private BackendInformation createBackendInformation(String agentId, String name, String desc, 
            boolean observeNewJvm, int[] pids, boolean active, int orderValue) {
        BackendInformation info = new BackendInformation(agentId);
        info.setName(name);
        info.setDescription(desc);
        info.setObserveNewJvm(observeNewJvm);
        info.setPids(pids);
        info.setActive(active);
        info.setOrderValue(orderValue);
        return info;
    }

}

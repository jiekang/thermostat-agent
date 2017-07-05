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

import com.redhat.thermostat.agent.internal.AgentInfoDAOImpl.AgentInformationUpdate;
import com.redhat.thermostat.agent.internal.AgentInformationTypeAdapter.AgentInformationUpdateTypeAdapter;
import org.junit.Test;

import com.redhat.thermostat.storage.model.AgentInformation;

public class AgentInformationTypeAdapterTest {
    
    @Test
    public void testWrite() throws Exception {
        AgentInformationTypeAdapter adapter = new AgentInformationTypeAdapter();
        final String expected = "[{\"agentId\":\"agent1\",\"startTime\":{\"$numberLong\":\"4000\"},"
                + "\"stopTime\":{\"$numberLong\":\"6000\"},\"alive\":false},"
                + "{\"agentId\":\"agent2\",\"startTime\":{\"$numberLong\":\"5000\"},\"stopTime\":{\"$numberLong\":\"0\"},"
                + "\"alive\":true}]";
        
        AgentInformation first = createAgentInformation("agent1", 4000L, 6000L, false);
        AgentInformation second = createAgentInformation("agent2", 5000L, 0L, true);
        List<AgentInformation> infos = Arrays.asList(first, second);
        
        String json = adapter.toJson(infos);
        assertEquals(expected, json);
    }
    
    @Test
    public void testUpdate() throws Exception {
        AgentInformationUpdateTypeAdapter adapter = new AgentInformationUpdateTypeAdapter();
        final String expected = "{\"set\":{\"startTime\":{\"$numberLong\":\"5000\"},"
                + "\"stopTime\":{\"$numberLong\":\"7000\"},\"alive\":false}}";
        
        AgentInformation info = createAgentInformation("agent2", 5000L, 7000L, false);
        AgentInformationUpdate update = new AgentInformationUpdate(info);
        
        String json = adapter.toJson(update);
        assertEquals(expected, json);
    }
    
    private AgentInformation createAgentInformation(String agentId, long startTime, long stopTime, boolean alive) {
        AgentInformation info = new AgentInformation(agentId);
        info.setStartTime(startTime);
        info.setStopTime(stopTime);
        info.setAlive(alive);
        return info;
    }

}

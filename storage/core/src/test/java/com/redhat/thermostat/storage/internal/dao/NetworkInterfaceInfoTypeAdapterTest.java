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
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.redhat.thermostat.storage.internal.dao.NetworkInterfaceInfoDAOImpl.NetworkInterfaceInfoUpdate;
import com.redhat.thermostat.storage.internal.dao.NetworkInterfaceInfoTypeAdapter.NetworkInterfaceInfoUpdateTypeAdapter;
import com.redhat.thermostat.storage.model.NetworkInterfaceInfo;

public class NetworkInterfaceInfoTypeAdapterTest {
    
    @Test
    public void testWrite() throws Exception {
        NetworkInterfaceInfoTypeAdapter adapter = new NetworkInterfaceInfoTypeAdapter();
        final String expected = "[{\"agentId\":\"agent1\",\"interfaceName\":\"lo\",\"ip4Addr\":\"127.0.0.1\","
                + "\"ip6Addr\":\"0:0:0:0:0:0:0:1%lo\"},{\"agentId\":\"agent2\",\"interfaceName\":\"if1\","
                + "\"ip4Addr\":\"1.2.3.4\",\"ip6Addr\":\"1:2:3:4:5:6:7:8%if1\"}]";
        
        NetworkInterfaceInfo first = createNetworkInterfaceInfo("agent1", "lo", "127.0.0.1", "0:0:0:0:0:0:0:1%lo");
        NetworkInterfaceInfo second = createNetworkInterfaceInfo("agent2", "if1", "1.2.3.4", "1:2:3:4:5:6:7:8%if1");
        List<NetworkInterfaceInfo> infos = Arrays.asList(first, second);
        
        String json = adapter.toJson(infos);
        assertEquals(expected, json);
    }
    
    @Test
    public void testRead() throws Exception {
        NetworkInterfaceInfoTypeAdapter adapter = new NetworkInterfaceInfoTypeAdapter();
        final String json = "{\"response\" : [{\"agentId\" : \"agent1\", \"interfaceName\" : \"lo\", "
                + "\"ip4Addr\" : \"127.0.0.1\", \"ip6Addr\" : \"0:0:0:0:0:0:0:1%lo\"}, "
                + "{\"agentId\" : \"agent2\", \"interfaceName\" : \"if1\", "
                + "\"ip4Addr\" : \"1.2.3.4\", \"ip6Addr\" : null}], "
                + "\"time\" : \"500000000\"}";
        
        List<NetworkInterfaceInfo> infos = adapter.fromJson(json);
        assertEquals(2, infos.size());
        
        NetworkInterfaceInfo first = infos.get(0);
        assertEquals("agent1", first.getAgentId());
        assertEquals("lo", first.getInterfaceName());
        assertEquals("127.0.0.1", first.getIp4Addr());
        assertEquals("0:0:0:0:0:0:0:1%lo", first.getIp6Addr());
        
        NetworkInterfaceInfo second = infos.get(1);
        assertEquals("agent2", second.getAgentId());
        assertEquals("if1", second.getInterfaceName());
        assertEquals("1.2.3.4", second.getIp4Addr());
        assertNull(second.getIp6Addr());
    }
    
    @Test
    public void testUpdate() throws Exception {
        NetworkInterfaceInfoUpdateTypeAdapter adapter = new NetworkInterfaceInfoUpdateTypeAdapter();
        final String expected = "{\"set\":{\"ip4Addr\":\"1.2.3.4\",\"ip6Addr\":\"1:2:3:4:5:6:7:8%if0\"}}";
        
        NetworkInterfaceInfoUpdate update = new NetworkInterfaceInfoUpdate("1.2.3.4", "1:2:3:4:5:6:7:8%if0");
        
        String json = adapter.toJson(update);
        assertEquals(expected, json);
    }
    
    private NetworkInterfaceInfo createNetworkInterfaceInfo(String agentId, String iFace, String ip4Addr, 
            String ip6Addr) {
        NetworkInterfaceInfo info = new NetworkInterfaceInfo(agentId, iFace);
        info.setIp4Addr(ip4Addr);
        info.setIp6Addr(ip6Addr);
        return info;
    }

}

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

package com.redhat.thermostat.host.network.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class NetworkInterfaceInfoTypeAdapterTest {
    
    @Test
    public void testWrite() throws Exception {
        NetworkInterfaceInfoTypeAdapter adapter = new NetworkInterfaceInfoTypeAdapter();
        final String expected = "[{\"interfaceName\":\"lo\",\"ip4Addr\":\"127.0.0.1\","
                + "\"ip6Addr\":\"0:0:0:0:0:0:0:1%lo\"},{\"interfaceName\":\"if1\","
                + "\"ip4Addr\":\"1.2.3.4\",\"ip6Addr\":\"1:2:3:4:5:6:7:8%if1\"}]";
        
        NetworkInterfaceInfo first = createNetworkInterfaceInfo("lo", "127.0.0.1", "0:0:0:0:0:0:0:1%lo");
        NetworkInterfaceInfo second = createNetworkInterfaceInfo("if1", "1.2.3.4", "1:2:3:4:5:6:7:8%if1");
        List<NetworkInterfaceInfo> infos = Arrays.asList(first, second);
        
        String json = adapter.toJson(infos);
        assertEquals(expected, json);
    }
    
    @Test
    public void testRead() throws Exception {
        NetworkInterfaceInfoTypeAdapter adapter = new NetworkInterfaceInfoTypeAdapter();
        final String json = "{\"response\" : [{\"interfaceName\" : \"lo\", "
                + "\"ip4Addr\" : \"127.0.0.1\", \"ip6Addr\" : \"0:0:0:0:0:0:0:1%lo\"}, "
                + "{\"interfaceName\" : \"if1\", "
                + "\"ip4Addr\" : \"1.2.3.4\", \"ip6Addr\" : null}], "
                + "\"time\" : \"500000000\"}";
        
        List<NetworkInterfaceInfo> infos = adapter.fromJson(json);
        assertEquals(2, infos.size());
        
        NetworkInterfaceInfo first = infos.get(0);
        assertEquals("lo", first.getInterfaceName());
        assertEquals("127.0.0.1", first.getIp4Addr());
        assertEquals("0:0:0:0:0:0:0:1%lo", first.getIp6Addr());
        
        NetworkInterfaceInfo second = infos.get(1);
        assertEquals("if1", second.getInterfaceName());
        assertEquals("1.2.3.4", second.getIp4Addr());
        assertNull(second.getIp6Addr());
    }
    
    private NetworkInterfaceInfo createNetworkInterfaceInfo(String iFace, String ip4Addr, String ip6Addr) {
        NetworkInterfaceInfo info = new NetworkInterfaceInfo(iFace);
        info.setIp4Addr(ip4Addr);
        info.setIp6Addr(ip6Addr);
        info.setDisplayName(null);
        return info;
    }

}

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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

public class NetworkInfoListTest {

    private NetworkInfoList info;

    private static final String WRITER_ID = "some-agent-id";
    private static final long TIMESTAMP = 333;

    @Before
    public void setup() {
        info = createInfo("if1", "1.2.3.4", "1:2:3:4:5:6:7:8");
    }

    @Test
    public void testEquals() {
        NetworkInfoList other = createInfo( "if1", "1.2.3.4", "1:2:3:4:5:6:7:8");
        assertTrue(info.equals(other));
    }

    @Test
    public void testEqualsWrongInterface() {
        NetworkInfoList other = createInfo("if2", "1.2.3.4", "1:2:3:4:5:6:7:8");
        assertFalse(info.equals(other));
    }

    @Test
    public void testEqualsWrongIpV4() {
        NetworkInfoList other = createInfo("if1", "1.2.3.5", "1:2:3:4:5:6:7:8");
        assertFalse(info.equals(other));
    }

    @Test
    public void testEqualsWrongIpV6() {
        NetworkInfoList other = createInfo("if1", "1.2.3.4", "1:2:3:4:5:6:7:9");
        assertFalse(info.equals(other));
    }

    @Test
    public void testHashCode() {
        NetworkInfoList other = createInfo("if1", "1.2.3.4", "1:2:3:4:5:6:7:8");
        assertEquals(info.hashCode(), other.hashCode());
    }

    @Test
    public void testHashCodeWrongInterface() {
        NetworkInfoList other = createInfo("if2", "1.2.3.4", "1:2:3:4:5:6:7:8");
        assertFalse(info.hashCode() == other.hashCode());
    }

    @Test
    public void testHashCodeWrongIpV4() {
        NetworkInfoList other = createInfo("if1", "1.2.3.5", "1:2:3:4:5:6:7:8");
        assertFalse(info.hashCode() == other.hashCode());
    }

    @Test
    public void testHashCodeWrongIpV6() {
        NetworkInfoList other = createInfo("if1", "1.2.3.4", "1:2:3:4:5:6:7:9");
        assertFalse(info.hashCode() == other.hashCode());
    }

    private NetworkInfoList createInfo(String iFace, String ip4Addr, String ip6Addr) {
        NetworkInterfaceInfo info = new NetworkInterfaceInfo(iFace);
        info.setIp4Addr(ip4Addr);
        info.setIp6Addr(ip6Addr);
        ArrayList<NetworkInterfaceInfo> list = new ArrayList<NetworkInterfaceInfo>(1);
        list.add(info);
        NetworkInfoList ilist = new NetworkInfoList(WRITER_ID, TIMESTAMP, list);
        return ilist;
    }
}

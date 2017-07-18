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

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.redhat.thermostat.common.utils.LoggingUtils;
import com.redhat.thermostat.host.network.model.NetworkInterfaceInfo;

class NetworkInfoBuilder {

    private static final Logger logger = LoggingUtils.getLogger(NetworkInfoBuilder.class);

    private void addInterfaces(List<NetworkInterfaceInfo> infos, List<NetworkInterface> ifaceList, boolean addAll, boolean addUnconnected) {
        for (NetworkInterface iface : ifaceList) {
            NetworkInterfaceInfo info = new NetworkInterfaceInfo(iface.getName());
            List<InetAddress> addrList = Collections.list(iface.getInetAddresses());
            final boolean want = addAll || (addrList.isEmpty() == addUnconnected);
            for (InetAddress addr : addrList) {
                if (addr instanceof Inet4Address) {
                    info.setIp4Addr(addr.getHostAddress());
                } else if (addr instanceof Inet6Address) {
                    info.setIp6Addr(addr.getHostAddress());
                }
            }
            if (want) {
                info.setDisplayName(iface.getDisplayName());
                infos.add(info);
            }
        }
    }

    List<NetworkInterfaceInfo> build() {
        final List<NetworkInterfaceInfo> infos = new ArrayList<>();
        try {
            final Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
            final List<NetworkInterface> ifaceList = Collections.list(ifaces);
            // list connected interfaces first (there is a lot of noise on windows)
            addInterfaces(infos, ifaceList, false, false);
            addInterfaces(infos, ifaceList, false, true);

        } catch (SocketException e) {
            logger.log(Level.WARNING, "error enumerating network interfaces");
        }
        return infos;
    }

}


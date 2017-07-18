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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

public class NetworkInterfaceInfoTypeAdapter extends TypeAdapter<List<NetworkInterfaceInfo>> {

    private static final String INTERFACE_NAME = "interfaceName";
    private static final String DISPLAY_NAME = "displayName";
    private static final String IP4_ADDR = "ip4Addr";
    private static final String IP6_ADDR = "ip6Addr";
    private static final String RESPONSE_ROOT = "response";
    private static final String SERVER_TIME = "time";

    @Override
    public void write(JsonWriter out, List<NetworkInterfaceInfo> value) throws IOException {

        // Request is an array of NetworkInterfaceInfo objects
        out.beginArray();
        
        for (NetworkInterfaceInfo info : value) {
            writeNetworkInterfaceInfo(out, info);
        }
        
        out.endArray();
    }

    private void writeNetworkInterfaceInfo(JsonWriter out, NetworkInterfaceInfo info) throws IOException {
        out.beginObject();
        
        // Write each field of NetworkInterfaceInfo as part of a JSON object
        out.name(INTERFACE_NAME);
        out.value(info.getInterfaceName());

        if (info.getDisplayName() != null) {
            out.name(DISPLAY_NAME);
            out.value(info.getDisplayName());
        }
        out.name(IP4_ADDR);
        out.value(info.getIp4Addr());
        out.name(IP6_ADDR);
        out.value(info.getIp6Addr());
        
        out.endObject();
    }

    @Override
    public List<NetworkInterfaceInfo> read(JsonReader in) throws IOException {
        List<NetworkInterfaceInfo> infos = null;
        
        try {
            // Parse root object
            in.beginObject();
            while (in.hasNext()) {
                String name = in.nextName();
                switch (name) {
                case RESPONSE_ROOT:
                    infos = readList(in);
                    break;
                case SERVER_TIME:
                    in.nextString();
                    break;
                default:
                    throw new IOException("Unexpected JSON name in gateway response: '" + name + "'");
                }
            }
            in.endObject();
        } catch (IllegalStateException e) {
            throw new IOException("Reading JSON response from web gateway failed", e);
        }
        
        return infos;
    }
    
    List<NetworkInterfaceInfo> readList(JsonReader in) throws IOException {
        List<NetworkInterfaceInfo> infos = new ArrayList<>();
        
        // Parse array of NetworkInterfaceInfos
        in.beginArray();
        
        while (in.hasNext()) {
            NetworkInterfaceInfo info = readNetworkInterfaceInfo(in);
            infos.add(info);
        }
        in.endArray();
        
        return infos;
    }
    
    private NetworkInterfaceInfo readNetworkInterfaceInfo(JsonReader in) throws IOException {
        String interfaceName = null;
        String ipv4Addr = null;
        String ipv6Addr = null;
        String displayName = null;
        
        // Begin parsing a NetworkInterfaceInfo record
        in.beginObject();
        
        while (in.hasNext()) {
            String name = in.nextName();
            switch (name) {
            case INTERFACE_NAME:
                interfaceName = in.nextString();
                break;
            case DISPLAY_NAME:
                displayName = in.nextString();
                break;
            case IP4_ADDR: // Addresses may be null
                ipv4Addr = readStringOrNull(in);
                break;
            case IP6_ADDR:
                ipv6Addr = readStringOrNull(in);
                break;
            default:
                throw new IOException("Unexpected JSON name in record: '" + name + "'");
            }
        }
        
        in.endObject();
        
        // Create NetworkInterfaceInfo if all required fields present
        if (interfaceName == null) {
            throw new IOException("Network interface information record is incomplete");
        }
        NetworkInterfaceInfo result = new NetworkInterfaceInfo(interfaceName);
        result.setIp4Addr(ipv4Addr);
        result.setIp6Addr(ipv6Addr);
        result.setDisplayName(displayName);
        
        return result;
    }
    
    private String readStringOrNull(JsonReader in) throws IOException {
        String result = null;
        JsonToken token = in.peek();
        if (token == JsonToken.NULL) {
            in.nextNull();
        } else {
            result = in.nextString();
        }
        return result;
    }
}

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

package com.redhat.thermostat.host.overview.model;

import java.io.IOException;
import java.util.List;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class HostInfoTypeAdapter extends TypeAdapter<List<HostInfo>> {
    
    private static final String AGENT_ID = "agentId";
    private static final String HOSTNAME = "hostname";
    private static final String TIMESTAMP = "timeStamp";
    private static final String OS_NAME = "osName";
    private static final String OS_KERNEL = "osKernel";
    private static final String CPU_MODEL = "cpuModel";
    private static final String CPU_COUNT = "cpuCount";
    private static final String TOTAL_MEMORY = "totalMemory";
    private static final String TYPE_LONG = "$numberLong";

    @Override
    public void write(JsonWriter out, List<HostInfo> value) throws IOException {
        // Request is an array of HostInfo objects
        out.beginArray();
        
        for (HostInfo info : value) {
            writeHostInfo(out, info);
        }
        
        out.endArray();
    }

    private void writeHostInfo(JsonWriter out, HostInfo info) throws IOException {
        out.beginObject();
        
        // Write each field of HostInfo as part of a JSON object
        out.name(AGENT_ID);
        out.value(info.getAgentId());
        out.name(HOSTNAME);
        out.value(info.getHostname());
        out.name(TIMESTAMP);
        writeLong(out, info.getTimeStamp());
        out.name(OS_NAME);
        out.value(info.getOsName());
        out.name(OS_KERNEL);
        out.value(info.getOsKernel());
        out.name(CPU_MODEL);
        out.value(info.getCpuModel());
        out.name(CPU_COUNT);
        out.value(info.getCpuCount());
        out.name(TOTAL_MEMORY);
        writeLong(out, info.getTotalMemory());
        
        out.endObject();
    }

    private void writeLong(JsonWriter out, long totalMemory) throws IOException {
        // Write MongoDB representation of a Long
        out.beginObject();
        out.name(TYPE_LONG);
        out.value(String.valueOf(totalMemory));
        out.endObject();
    }

    @Override
    public List<HostInfo> read(JsonReader in) throws IOException {
        throw new UnsupportedOperationException();
    }

}

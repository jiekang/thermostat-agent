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

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.redhat.thermostat.vm.memory.agent.model.VmTlabStat;

import java.io.IOException;
import java.util.List;

public class VmTlabStatTypeAdapter extends TypeAdapter<List<VmTlabStat>> {

    private static final String AGENT_ID = "agentId";
    private static final String JVM_ID = "jvmId";
    private static final String TIMESTAMP = "timeStamp";
    private static final String ALLOC_THREADS = "allocThreads";
    private static final String TOTAL_ALLOCATIONS = "totalAllocations";
    private static final String REFILLS = "refills";
    private static final String MAX_REFILLS = "maxRefills";
    private static final String SLOW_ALLOCATIONS = "slowAllocations";
    private static final String MAX_SLOW_ALLOCATIONS = "maxSlowAllocations";
    private static final String GC_WASTE = "gcWaste";
    private static final String MAX_GC_WASTE = "maxGcWaste";
    private static final String SLOW_WASTE = "slowWaste";
    private static final String MAX_SLOW_WASTE = "maxSlowWaste";
    private static final String FAST_WASTE = "fastWaste";
    private static final String MAX_FAST_WASTE = "maxFastWaste";
    private static final String TYPE_LONG = "$numberLong";

    @Override
    public List<VmTlabStat> read(JsonReader in) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void write(JsonWriter out, List<VmTlabStat> stats) throws IOException {
        out.beginArray();
        for (VmTlabStat tlabStat : stats) {
            writeStat(out, tlabStat);
        }
        out.endArray();
    }

    public void writeStat(JsonWriter out, VmTlabStat stat) throws IOException {
        out.beginObject();
        out.name(JVM_ID);
        out.value(stat.getJvmId());
        out.name(AGENT_ID);
        out.value(stat.getAgentId());
        out.name(TIMESTAMP);
        writeLong(out, stat.getTimeStamp());
        out.name(ALLOC_THREADS);
        writeLong(out, stat.getTotalAllocatingThreads());
        out.name(TOTAL_ALLOCATIONS);
        writeLong(out, stat.getTotalAllocations());
        out.name(REFILLS);
        writeLong(out, stat.getTotalRefills());
        out.name(MAX_REFILLS);
        writeLong(out, stat.getMaxRefills());
        out.name(SLOW_ALLOCATIONS);
        writeLong(out, stat.getTotalSlowAllocations());
        out.name(MAX_SLOW_ALLOCATIONS);
        writeLong(out, stat.getMaxSlowAllocations());
        out.name(GC_WASTE);
        writeLong(out, stat.getTotalGcWaste());
        out.name(MAX_GC_WASTE);
        writeLong(out, stat.getMaxGcWaste());
        out.name(SLOW_WASTE);
        writeLong(out, stat.getTotalSlowWaste());
        out.name(MAX_SLOW_WASTE);
        writeLong(out, stat.getMaxSlowWaste());
        out.name(FAST_WASTE);
        writeLong(out, stat.getTotalFastWaste());
        out.name(MAX_FAST_WASTE);
        writeLong(out, stat.getMaxFastWaste());
        out.endObject();
    }

    public void writeLong(JsonWriter out, long value) throws IOException {
        // Write MongoDB representation of a Long
        out.beginObject();
        out.name(TYPE_LONG);
        out.value(String.valueOf(value));
        out.endObject();
    }
}

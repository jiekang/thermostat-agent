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
import com.redhat.thermostat.vm.memory.agent.model.VmMemoryStat;

import java.io.IOException;
import java.util.List;

import static com.redhat.thermostat.vm.memory.agent.model.VmMemoryStat.Generation;
import static com.redhat.thermostat.vm.memory.agent.model.VmMemoryStat.Space;

public class VmMemoryStatTypeAdapter extends TypeAdapter<List<VmMemoryStat>> {

    private static final String GENERATIONS = "generations";
    private static final String TIMESTAMP = "timeStamp";
    private static final String AGENT_ID = "agentId";
    private static final String VM_ID = "jvmId";
    private static final String METASPACE_MAX_CAPACITY = "metaspaceMaxCapacity";
    private static final String METASPACE_MIN_CAPACITY = "metaspaceMinCapacity";
    private static final String METASPACE_CAPACITY = "metaspaceCapacity";
    private static final String METASPACE_USED = "metaspaceUsed";
    private static final String NAME = "name";
    private static final String CAPACITY = "capacity";
    private static final String MAX_CAPACITY = "maxCapacity";
    private static final String SPACES = "spaces";
    private static final String COLLECTOR = "collector";
    private static final String INDEX = "index";
    private static final String USED = "used";
    private static final String TYPE_LONG = "$numberLong";

    @Override
    public List<VmMemoryStat> read(JsonReader in) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void write(JsonWriter out, List<VmMemoryStat> stats) throws IOException {
        out.beginArray();
        for (VmMemoryStat stat : stats) {
            writeStat(out, stat);
        }
        out.endArray();
    }

    public void writeStat(JsonWriter out, VmMemoryStat stat) throws IOException {
        out.beginObject();
        out.name(AGENT_ID);
        out.value(stat.getAgentId());
        out.name(VM_ID);
        out.value(stat.getVmId());
        out.name(TIMESTAMP);
        writeLong(out, stat.getTimeStamp());
        out.name(METASPACE_MAX_CAPACITY);
        writeLong(out, stat.getMetaspaceMaxCapacity());
        out.name(METASPACE_MIN_CAPACITY);
        writeLong(out, stat.getMetaspaceMinCapacity());
        out.name(METASPACE_CAPACITY);
        writeLong(out, stat.getMetaspaceCapacity());
        out.name(METASPACE_USED);
        writeLong(out, stat.getMetaspaceUsed());
        out.name(GENERATIONS);
        out.beginArray();
        for (Generation g : stat.getGenerations()) {
            writeGeneration(out, g);
        }
        out.endArray();
        out.endObject();
    }

    public void writeLong(JsonWriter out, long value) throws IOException {
        // Write MongoDB representation of a Long
        out.beginObject();
        out.name(TYPE_LONG);
        out.value(String.valueOf(value));
        out.endObject();
    }

    public void writeGeneration(JsonWriter out, Generation g) throws IOException {
        out.beginObject();
        out.name(NAME);
        out.value(g.getName());
        out.name(CAPACITY);
        writeLong(out, g.getCapacity());
        out.name(MAX_CAPACITY);
        writeLong(out, g.getMaxCapacity());
        out.name(COLLECTOR);
        out.value(g.getCollector());
        out.name(SPACES);
        out.beginArray();
        for (Space s : g.getSpaces()) {
            writeSpace(out, s);
        }
        out.endArray();
        out.endObject();
    }

    public void writeSpace(JsonWriter out, Space s) throws IOException {
        out.beginObject();
        out.name(INDEX);
        out.value(s.getIndex());
        out.name(NAME);
        out.value(s.getName());
        out.name(CAPACITY);
        writeLong(out, s.getCapacity());
        out.name(MAX_CAPACITY);
        writeLong(out, s.getMaxCapacity());
        out.name(USED);
        writeLong(out, s.getUsed());
        out.endObject();
    }
}

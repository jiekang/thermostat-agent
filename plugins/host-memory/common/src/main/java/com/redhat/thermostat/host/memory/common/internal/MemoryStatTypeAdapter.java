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

package com.redhat.thermostat.host.memory.common.internal;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.redhat.thermostat.host.memory.common.model.MemoryStat;

import java.io.IOException;
import java.util.List;

public class MemoryStatTypeAdapter extends TypeAdapter<List<MemoryStat>> {

    private static final String TYPE_LONG = "$numberLong";
    private static final String TIMESTAMP = "timeStamp";
    private static final String TOTAL = "total";
    private static final String FREE = "free";
    private static final String BUFFERS = "buffers";
    private static final String CACHED = "cached";
    private static final String SWAP_TOTAL = "swapTotal";
    private static final String SWAP_FREE = "swapFree";
    private static final String COMMIT_LIMIT = "commitLimit";
    private static final String AGENT_ID = "agentId";


    public void write(JsonWriter out, List<MemoryStat> stats) throws IOException {
        out.beginArray();

        for (MemoryStat stat : stats) {
            writeMemoryStat(out, stat);
        }

        out.endArray();
    }

    public void writeMemoryStat(JsonWriter out, MemoryStat stats) throws IOException {
        out.beginObject();
        out.name(TIMESTAMP);
        writeLong(out, stats.getTimeStamp());
        out.name(TOTAL);
        writeLong(out, stats.getTotal());
        out.name(FREE);
        writeLong(out, stats.getFree());
        out.name(BUFFERS);
        writeLong(out, stats.getBuffers());
        out.name(CACHED);
        writeLong(out, stats.getCached());
        out.name(SWAP_TOTAL);
        writeLong(out, stats.getSwapTotal());
        out.name(SWAP_FREE);
        writeLong(out, stats.getSwapFree());
        out.name(COMMIT_LIMIT);
        writeLong(out, stats.getCommitLimit());
        out.name(AGENT_ID);
        out.value(stats.getAgentId());
        out.endObject();
    }

    public void writeLong(JsonWriter out, long input) throws IOException {
        // Write MongoDB representation of a Long
        out.beginObject();
        out.name(TYPE_LONG);
        out.value(String.valueOf(input));
        out.endObject();
    }

    public List<MemoryStat> read(JsonReader in) throws IOException {
        return null;
    }

}

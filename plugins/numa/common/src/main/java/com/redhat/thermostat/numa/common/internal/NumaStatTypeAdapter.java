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

package com.redhat.thermostat.numa.common.internal;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.redhat.thermostat.numa.common.NumaNodeStat;
import com.redhat.thermostat.numa.common.NumaStat;

import com.google.gson.TypeAdapter;

import java.io.IOException;
import java.util.List;

public class NumaStatTypeAdapter extends TypeAdapter<List<NumaStat>> {

    private static final String TYPE_LONG = "$numberLong";
    private static final String TIMESTAMP = "timeStamp";
    private static final String NODE_STATS = "nodeStats";
    private static final String AGENT_ID = "agentId";
    private static final String NUMA_HIT = "numaHit";
    private static final String NUMA_MISS = "numaMiss";
    private static final String NUMA_FOREIGN = "numaForeign";
    private static final String INTERLEAVE_HIT = "interleaveHit";
    private static final String LOCAL_NODE = "localNode";
    private static final String OTHER_NODE = "otherNode";
    private static final String NODE_ID = "nodeId";


    @Override
    public List<NumaStat> read(JsonReader in) {
        return null;
    }

    @Override
    public void write(JsonWriter out, List<NumaStat> stats) throws IOException {
        out.beginArray();
        for (NumaStat stat : stats) {
            out.beginObject();
            out.name(TIMESTAMP);
            writeLong(out, stat.getTimeStamp());
            out.name(NODE_STATS);
            out.beginArray();
            for (int i = 0; i < stat.getNodeStats().length; i++) {
                writeNumaNodeStat(out, stat.getNodeStats()[i]);
            }
            out.endArray();
            out.name(AGENT_ID);
            out.value(stat.getAgentId());
            out.endObject();
        }
        out.endArray();
    }

    private void writeLong(JsonWriter out, long timestamp) throws IOException {
        // Write MongoDB representation of a Long
        out.beginObject();
        out.name(TYPE_LONG);
        out.value(String.valueOf(timestamp));
        out.endObject();
    }

    private void writeNumaNodeStat(JsonWriter out, NumaNodeStat stat) throws IOException {
        out.beginObject();
        out.name(NUMA_HIT);
        writeLong(out, stat.getNumaHit());
        out.name(NUMA_MISS);
        writeLong(out, stat.getNumaMiss());
        out.name(NUMA_FOREIGN);
        writeLong(out, stat.getNumaForeign());
        out.name(INTERLEAVE_HIT);
        writeLong(out, stat.getInterleaveHit());
        out.name(LOCAL_NODE);
        writeLong(out, stat.getLocalNode());
        out.name(OTHER_NODE);
        writeLong(out, stat.getOtherNode());
        out.name(NODE_ID);
        out.value(stat.getNodeId());
        out.endObject();
    }

}

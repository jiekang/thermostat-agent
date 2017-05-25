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

package com.redhat.thermostat.host.cpu.common.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.google.gson.TypeAdapter;
import com.redhat.thermostat.common.utils.LoggingUtils;
import com.redhat.thermostat.host.cpu.common.model.CpuStat;

public class CpuStatTypeAdapter extends TypeAdapter<List<CpuStat>> {

    private static final String TIMESTAMP = "timeStamp";
    private static final String PROCESSOR_USAGE = "perProcessorUsage";
    private static final String TYPE_LONG = "$numberLong";
    private static final String SERVER_TIME = "time";
    private static final String RESPONSE_ROOT = "response";
    private static final String AGENT_ID = "agentId";

    private static final Logger logger = LoggingUtils.getLogger(CpuStatTypeAdapter.class);

    @Override
    public void write(JsonWriter out, List<CpuStat> value) throws IOException {
        out.beginArray();

        for (CpuStat stat : value) {
            writeCpuStat(out, stat);
        }

        out.endArray();
    }

    private void writeCpuStat(JsonWriter out, CpuStat stat) throws IOException {
        out.beginObject();
        out.name(PROCESSOR_USAGE);
        out.beginArray();
        for (double val : stat.getPerProcessorUsage()) {
            out.value(val);
        }
        out.endArray();
        out.name(TIMESTAMP);
        writeLong(out, stat.getTimeStamp());
        out.name(AGENT_ID);
        out.value(stat.getAgentId());
        out.endObject();
    }

    @Override
    public List<CpuStat> read(JsonReader in) throws IOException {
        List<CpuStat> values = null;
        try {
            in.beginObject();
            while (in.hasNext()) {
                String name = in.nextName();
                switch (name) {
                    case RESPONSE_ROOT:
                        values = readResponse(in);
                        break;
                    case SERVER_TIME:
                        in.nextString();
                        break;
                    default:
                        throw new IOException("Unexpected JSON name: " + name);
                }
            }
            in.endObject();
        } catch (IllegalStateException e) {
            throw new IOException("Reading JSON response failed.");
        }
        return values;
    }

    private List<CpuStat> readResponse(JsonReader in) throws IOException {
        List<CpuStat> values = new ArrayList<>();

        in.beginArray();

        while (in.hasNext()) {
            try {
                CpuStat stat = readCpuStat(in);
                values.add(stat);
            } catch(IOException | IllegalStateException | NumberFormatException e) {
                logger.log(Level.WARNING, "Skipping malformed cpu stat record", e);
            }
        }
        in.endArray();

        return values;
    }

    private CpuStat readCpuStat(JsonReader in) throws IOException {
        String agentId = null;
        double[] perProcessorUsage = null;
        long timestamp = -1;

        in.beginObject();

        while (in.hasNext()) {
            String name = in.nextName();
            switch (name) {
                case PROCESSOR_USAGE:
                    perProcessorUsage = readProcessorUsage(in);
                    break;
                case TIMESTAMP:
                    timestamp = readLong(in);
                    break;
                case AGENT_ID:
                    agentId = in.nextString();
                    break;
                default:
                    throw new IOException("Unexpected JSON name: " + name);
            }
        }

        in.endObject();

        if (perProcessorUsage == null || agentId == null || timestamp < 0) {
            throw new IOException("CpuStat information is incomplete.");
        }
        return new CpuStat(agentId, timestamp,  perProcessorUsage);
    }

    private double[] readProcessorUsage(JsonReader in) throws IOException {
        List<Double> procUsage = new ArrayList<Double>();
        in.beginArray();
        while (in.hasNext()) {
            procUsage.add(in.nextDouble());
        }
        in.endArray();
        double[] result = new double[procUsage.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = procUsage.get(i);
        }
        return result;
    }

    private long readLong(JsonReader in) throws IOException {
        // Read MongoDB representation of a Long
        in.beginObject();
        String name = in.nextName();
        expectName(TYPE_LONG, name);
        long ret = Long.valueOf(in.nextString());
        in.endObject();
        return ret;
    }

    private void writeLong(JsonWriter out, long timestamp) throws IOException {
        // Write MongoDB representation of a Long
        out.beginObject();
        out.name(TYPE_LONG);
        out.value(String.valueOf(timestamp));
        out.endObject();
    }

    private void expectName(String expected, String actual) throws IOException {
        if (!expected.equals(actual)) {
            throw new IOException("Expected JSON name '" + expected + "', got '" + actual + "'");
        }
    }

}

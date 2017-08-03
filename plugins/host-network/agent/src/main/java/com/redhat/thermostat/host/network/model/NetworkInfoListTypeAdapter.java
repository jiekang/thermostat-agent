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
import java.util.List;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class NetworkInfoListTypeAdapter extends TypeAdapter<List<NetworkInfoList>> {

    private static final String AGENT_ID = "agentId";
    private static final String TIMESTAMP = "timeStamp";
    private static final String INTERFACE_LIST = "interfaces";
    private static final String TYPE_LONG = "$numberLong";

    private final NetworkInterfaceInfoTypeAdapter infoTypeAdapter = new NetworkInterfaceInfoTypeAdapter();

    private void write(final JsonWriter out, final NetworkInfoList value) throws IOException {

        out.beginObject();

        out.name(AGENT_ID);
        out.value(value.getAgentId());

        out.name(TIMESTAMP);
        writeLong(out, value.getTimeStamp());

        out.name(INTERFACE_LIST);
        infoTypeAdapter.write(out, value.getList());

        out.endObject();
    }

    private void writeLong(final JsonWriter out, final long input) throws IOException {
        // Write MongoDB representation of a Long
        out.beginObject();
        out.name(TYPE_LONG);
        out.value(String.valueOf(input));
        out.endObject();
    }

    private long readLong(final JsonReader in) throws IOException {
        // Read MongoDB representation of a Long
        in.beginObject();
        String name = in.nextName();
        expectName(TYPE_LONG, name);
        long ret = Long.valueOf(in.nextString());
        in.endObject();
        return ret;
    }

    private void expectName(final String expected, final String actual) throws IOException {
        if (!expected.equals(actual)) {
            throw new IOException("Expected JSON name '" + expected + "', got '" + actual + "'");
        }
    }

    @Override
    public void write(JsonWriter out, List<NetworkInfoList> list) throws IOException {
        out.beginArray();

        for (NetworkInfoList info : list) {
            write(out, info);
        }

        out.endArray();
    }

    @Override
    public List<NetworkInfoList> read(JsonReader jsonReader) throws IOException {
        throw new UnsupportedOperationException();
    }
}

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

package com.redhat.thermostat.storage.internal.dao;

import java.io.IOException;
import java.util.List;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.redhat.thermostat.storage.model.BackendInformation;

public class BackendInformationTypeAdapter extends TypeAdapter<List<BackendInformation>> {
    
    private static final String AGENT_ID = "agentId";
    private static final String NAME = "name";
    private static final String DESCRIPTION = "description";
    private static final String OBSERVE_NEW_JVM = "observeNewJvm";
    private static final String PIDS = "pids";
    private static final String ACTIVE = "active";
    private static final String ORDER_VALUE = "orderValue";
    
    @Override
    public void write(JsonWriter out, List<BackendInformation> value) throws IOException {
        // Request is an array of BackendInformation objects
        out.beginArray();
        
        for (BackendInformation info : value) {
            writeBackendInformation(out, info);
        }
        
        out.endArray();
    }

    private void writeBackendInformation(JsonWriter out, BackendInformation info) throws IOException {
        out.beginObject();
        
        // Write each field of BackendInformation as part of a JSON object
        out.name(AGENT_ID);
        out.value(info.getAgentId());
        out.name(NAME);
        out.value(info.getName());
        out.name(DESCRIPTION);
        out.value(info.getDescription());
        out.name(OBSERVE_NEW_JVM);
        out.value(info.isObserveNewJvm());
        out.name(PIDS);
        writePidArray(out, info.getPids());
        out.name(ACTIVE);
        out.value(info.isActive());
        out.name(ORDER_VALUE);
        out.value(info.getOrderValue());
        
        out.endObject();
    }

    private void writePidArray(JsonWriter out, int[] pids) throws IOException {
        // Output JSON array of PIDs
        out.beginArray();
        for (int pid : pids) {
            out.value(pid);
        }
        out.endArray();
    }

    @Override
    public List<BackendInformation> read(JsonReader in) throws IOException {
        throw new UnsupportedOperationException();
    }
    
}

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

package com.redhat.thermostat.agent.internal;

import java.io.IOException;
import java.util.List;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.redhat.thermostat.agent.internal.AgentInfoDAOImpl.AgentInformationUpdate;
import com.redhat.thermostat.storage.model.AgentInformation;

public class AgentInformationTypeAdapter extends TypeAdapter<List<AgentInformation>> {
    
    private static final String AGENT_ID = "agentId";
    private static final String START_TIME = "startTime";
    private static final String STOP_TIME = "stopTime";
    private static final String ALIVE = "alive";
    private static final String TYPE_LONG = "$numberLong";
    
    @Override
    public void write(JsonWriter out, List<AgentInformation> value) throws IOException {
        // Request is an array of AgentInformation objects
        out.beginArray();
        
        for (AgentInformation info : value) {
            writeAgentInformation(out, info);
        }
        
        out.endArray();
    }

    private void writeAgentInformation(JsonWriter out, AgentInformation info) throws IOException {
        out.beginObject();
        
        // Write each field of AgentInformation as part of a JSON object
        out.name(AGENT_ID);
        out.value(info.getAgentId());
        out.name(START_TIME);
        writeLong(out, info.getStartTime());
        out.name(STOP_TIME);
        writeLong(out, info.getStopTime());
        out.name(ALIVE);
        out.value(info.isAlive());
        
        out.endObject();
    }

    private static void writeLong(JsonWriter out, long value) throws IOException {
        // Write MongoDB representation of a Long
        out.beginObject();
        out.name(TYPE_LONG);
        out.value(String.valueOf(value));
        out.endObject();
    }
    
    @Override
    public List<AgentInformation> read(JsonReader in) throws IOException {
        throw new UnsupportedOperationException();
    }
    
    static class AgentInformationUpdateTypeAdapter extends TypeAdapter<AgentInformationUpdate> {
        
        private static final String SET = "set";

        @Override
        public void write(JsonWriter out, AgentInformationUpdate value) throws IOException {
            // List fields to update as part of a JSON object with name "set"
            out.beginObject();
            out.name(SET);
            
            AgentInformation info = value.getInfo();
            out.beginObject();
            out.name(START_TIME);
            writeLong(out, info.getStartTime());
            out.name(STOP_TIME);
            writeLong(out, info.getStopTime());
            out.name(ALIVE);
            out.value(info.isAlive());
            out.endObject();
            
            out.endObject();
        }

        @Override
        public AgentInformationUpdate read(JsonReader in) throws IOException {
            throw new UnsupportedOperationException();
        }
        
    }
    
}

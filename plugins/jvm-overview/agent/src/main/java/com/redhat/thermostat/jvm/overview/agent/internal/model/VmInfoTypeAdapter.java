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

package com.redhat.thermostat.jvm.overview.agent.internal.model;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.redhat.thermostat.jvm.overview.agent.internal.model.VmInfoDAOImpl.VmInfoUpdate;
import com.redhat.thermostat.jvm.overview.agent.model.VmInfo;

public class VmInfoTypeAdapter extends TypeAdapter<List<VmInfo>> {
    
    private static final String AGENT_ID = "agentId";
    private static final String VM_ID = "vmId";
    private static final String VM_PID = "vmPid";
    private static final String JAVA_VERSION = "javaVersion";
    private static final String JAVA_HOME = "javaHome";
    private static final String MAIN_CLASS = "mainClass";
    private static final String JAVA_COMMAND_LINE = "javaCommandLine";
    private static final String VM_ARGUMENTS = "vmArguments";
    private static final String VM_NAME = "vmName";
    private static final String VM_INFO = "vmInfo";
    private static final String VM_VERSION = "vmVersion";
    private static final String PROPERTIES_AS_ARRAY = "propertiesAsArray";
    private static final String ENVIRONMENT_AS_ARRAY = "environmentAsArray";
    private static final String LOADED_NATIVE_LIBRARIES = "loadedNativeLibraries";
    private static final String START_TIME_STAMP = "startTimeStamp";
    private static final String STOP_TIME_STAMP = "stopTimeStamp";
    private static final String UID = "uid";
    private static final String USERNAME = "username";
    private static final String TYPE_LONG = "$numberLong";
    private static final String KEY = "key";
    private static final String VALUE = "value";
    
    @Override
    public void write(JsonWriter out, List<VmInfo> value) throws IOException {
        // Request is an array of VmInfo objects
        out.beginArray();
        
        for (VmInfo info : value) {
            writeVmInfo(out, info);
        }
        
        out.endArray();
    }

    private void writeVmInfo(JsonWriter out, VmInfo info) throws IOException {
        out.beginObject();
        
        // Write each field of VmInfo as part of a JSON object
        out.name(AGENT_ID);
        out.value(info.getAgentId());
        out.name(VM_ID);
        out.value(info.getVmId());
        out.name(VM_PID);
        out.value(info.getVmPid());
        out.name(START_TIME_STAMP);
        writeLong(out, info.getStartTimeStamp());
        out.name(STOP_TIME_STAMP);
        writeLong(out, info.getStopTimeStamp());
        out.name(JAVA_VERSION);
        out.value(info.getJavaVersion());
        out.name(JAVA_HOME);
        out.value(info.getJavaHome());
        out.name(MAIN_CLASS);
        out.value(info.getMainClass());
        out.name(JAVA_COMMAND_LINE);
        out.value(info.getJavaCommandLine());
        out.name(VM_NAME);
        out.value(info.getVmName());
        out.name(VM_ARGUMENTS);
        out.value(info.getVmArguments());
        out.name(VM_INFO);
        out.value(info.getVmInfo());
        out.name(VM_VERSION);
        out.value(info.getVmVersion());
        out.name(PROPERTIES_AS_ARRAY);
        writeStringMap(out, info.getProperties());
        out.name(ENVIRONMENT_AS_ARRAY);
        writeStringMap(out, info.getEnvironment());
        out.name(LOADED_NATIVE_LIBRARIES);
        writeStringArray(out, info.getLoadedNativeLibraries());
        out.name(UID);
        writeLong(out, info.getUid());
        out.name(USERNAME);
        out.value(info.getUsername());
        
        out.endObject();
    }
    
    private void writeStringMap(JsonWriter out, Map<String, String> map) throws IOException {
        // Write contents of Map as an array of JSON objects
        out.beginArray();
        
        Set<Entry<String, String>> entries = map.entrySet();
        for (Map.Entry<String, String> entry : entries) {
            // Create JSON object with key and value labeled as JSON names
            out.beginObject();
            out.name(KEY);
            out.value(entry.getKey());
            out.name(VALUE);
            out.value(entry.getValue());
            out.endObject();
        }
        
        out.endArray();
    }
    
    private void writeStringArray(JsonWriter out, String[] array) throws IOException {
        // Write String[] as JSON array
        out.beginArray();
        
        for (String item : array) {
            out.value(item);
        }
        
        out.endArray();
    }
    
    private static void writeLong(JsonWriter out, long value) throws IOException {
        // Write MongoDB representation of a Long
        out.beginObject();
        out.name(TYPE_LONG);
        out.value(String.valueOf(value));
        out.endObject();
    }

    @Override
    public List<VmInfo> read(JsonReader in) throws IOException {
        throw new UnsupportedOperationException();
    }
    
    static class VmInfoUpdateTypeAdapter extends TypeAdapter<VmInfoUpdate> {

        private static final String SET = "set";
        
        @Override
        public void write(JsonWriter out, VmInfoUpdate value) throws IOException {
            // List fields to update as part of a JSON object with name "set"
            out.beginObject();
            out.name(SET);
            
            out.beginObject();
            out.name(STOP_TIME_STAMP);
            writeLong(out, value.getStoppedTime());
            out.endObject();
            
            out.endObject();
        }

        @Override
        public VmInfoUpdate read(JsonReader in) throws IOException {
            throw new UnsupportedOperationException();
        }
        
    }
    
}

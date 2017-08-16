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

package com.redhat.thermostat.jvm.overview.agent.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.redhat.thermostat.storage.core.Entity;
import com.redhat.thermostat.storage.core.Persist;
import com.redhat.thermostat.storage.model.AgentInformation;
import com.redhat.thermostat.storage.model.BasePojo;
import com.redhat.thermostat.storage.model.Pojo;

@Entity
public class VmInfo extends BasePojo {

    public enum AliveStatus {
        RUNNING,
        EXITED,
        /**
         * We don't know what the status of this VM is. Possible cause: agent
         * was shut down before the VM was.
         */
        UNKNOWN,
    }

    @Entity
    public static class KeyValuePair implements Pojo {
    
        private String key;
        private String value;

        public KeyValuePair() {
            this(null, null);
        }

        public KeyValuePair(String key, String value) {
            this.key = key;
            this.value = value;
        }

        @Persist
        public String getKey() {
            return key;
        }

        @Persist
        public void setKey(String key) {
            this.key = key;
        }

        @Persist
        public String getValue() {
            return value;
        }

        @Persist
        public void setValue(String value) {
            this.value = value;
        }

        
    }

    private String jvmId;
    private int jvmPid = 0;
    private long startTime = System.currentTimeMillis();
    private long stopTime = Long.MIN_VALUE;
    private String javaVersion = "unknown";
    private String javaHome = "unknown";
    private String javaCommandLine = "unknown";
    private String mainClass = "unknown";
    private String jvmName = "unknown";
    private String jvmInfo = "unknown";
    private String jvmVersion = "unknown";
    private String jvmArguments = "unknown";
    private Map<String, String> properties = new HashMap<String, String>();
    private Map<String, String> environment = new HashMap<String, String>();
    private String[] loadedNativeLibraries;
    private long uid;
    private String username;

    public VmInfo() {
        /* use defaults */
        super(null);
    }

    public VmInfo(String writerId, String vmId, int vmPid, long startTime, long stopTime,
                  String javaVersion, String javaHome,
                  String mainClass, String commandLine,
                  String jvmName, String vmInfo, String vmVersion, String jvmArguments,
                  Map<String, String> properties, Map<String, String> environment, String[] loadedNativeLibraries,
                  long uid, String username) {
        super(writerId);
        this.jvmId = vmId;
        this.jvmPid = vmPid;
        this.startTime = startTime;
        this.stopTime = stopTime;
        this.javaVersion = javaVersion;
        this.javaHome = javaHome;
        this.mainClass = mainClass;
        this.javaCommandLine = commandLine;
        this.jvmName = jvmName;
        this.jvmInfo = vmInfo;
        this.jvmVersion = vmVersion;
        this.jvmArguments = jvmArguments;
        this.properties = properties;
        this.environment = environment;
        this.loadedNativeLibraries = loadedNativeLibraries;
        this.uid = uid;
        this.username = username;
    }

    @Persist
    public String getJvmId() {
        return jvmId;
    }

    @Persist
    public void setJvmId(String jvmId) {
        this.jvmId = jvmId;
    }

    @Persist
    public int getJvmPid() {
        return jvmPid;
    }

    @Persist
    public void setJvmPid(int jvmPid) {
        this.jvmPid = jvmPid;
    }

    @Persist
    public long getStartTimeStamp() {
        return startTime;
    }

    @Persist
    public void setStartTimeStamp(long startTime) {
        this.startTime = startTime;
    }

    @Persist
    public long getStopTimeStamp() {
        return stopTime;
    }

    @Persist
    public void setStopTimeStamp(long stopTime) {
        this.stopTime = stopTime;
    }

    @Persist
    public String getJavaVersion() {
        return javaVersion;
    }

    @Persist
    public void setJavaVersion(String javaVersion) {
        this.javaVersion = javaVersion;
    }

    @Persist
    public String getJavaHome() {
        return javaHome;
    }

    @Persist
    public void setJavaHome(String javaHome) {
        this.javaHome = javaHome;
    }

    /**
     * If java is invoked as {@code java -jar foo.jar}, then the main class name
     * is {@code foo.jar}.
     */
    @Persist
    public String getMainClass() {
        return mainClass;
    }

    @Persist
    public void setMainClass(String mainClass) {
        this.mainClass = mainClass;
    }

    @Persist
    public String getJavaCommandLine() {
        return javaCommandLine;
    }

    @Persist
    public void setJavaCommandLine(String javaCommandLine) {
        this.javaCommandLine = javaCommandLine;
    }

    @Persist
    public String getJvmName() {
        return jvmName;
    }

    @Persist
    public void setJvmName(String jvmName) {
        this.jvmName = jvmName;
    }

    @Persist
    public String getJvmArguments() {
        return jvmArguments;
    }

    @Persist
    public void setJvmArguments(String jvmArguments) {
        this.jvmArguments = jvmArguments;
    }

    @Persist
    public String getJvmInfo() {
        return jvmInfo;
    }

    @Persist
    public void setJvmInfo(String jvmInfo) {
        this.jvmInfo = jvmInfo;
    }

    @Persist
    public String getJvmVersion() {
        return jvmVersion;
    }

    @Persist
    public void setJvmVersion(String jvmVersion) {
        this.jvmVersion = jvmVersion;
    }

    /**
     * @deprecated This can incorrectly show a VM as running when the actual
     *             status is unknown. Use {@link #isAlive(AgentInformation)}
     *             instead.
     */
    @Deprecated
    public boolean isAlive() {
        return getStartTimeStamp() > getStopTimeStamp();
    }

    public AliveStatus isAlive(AgentInformation agentInfo) {
        if (agentInfo.isAlive()) {
            return (isAlive() ? AliveStatus.RUNNING : AliveStatus.EXITED);
        } else {
            return (isAlive() ? AliveStatus.UNKNOWN: AliveStatus.EXITED);
        }
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    @Persist
    public KeyValuePair[] getPropertiesAsArray() {
        return getMapAsArray(properties);
    }

    @Persist
    public void setPropertiesAsArray(KeyValuePair[] properties) {
        this.properties = getArrayAsMap(properties);
    }

    public Map<String, String> getEnvironment() {
        return environment;
    }

    public void setEnvironment(Map<String, String> environment) {
        this.environment = environment;
    }

    @Persist
    public KeyValuePair[] getEnvironmentAsArray() {
        return getMapAsArray(environment);
    }

    @Persist
    public void setEnvironmentAsArray(KeyValuePair[] environment) {
        this.environment = getArrayAsMap(environment);
    }

    private KeyValuePair[] getMapAsArray(Map<String, String> map) {
        if (map == null) {
            return null;
        }
        Set<String> keys = map.keySet();
        KeyValuePair[] tuples = new KeyValuePair[keys.size()];
        int i = 0;
        for (String key: keys) {
            tuples[i] = new KeyValuePair(key, map.get(key));
            i++;
        }
        return tuples;
    }

    private Map<String,String> getArrayAsMap(KeyValuePair[] tuples) {
        if (tuples == null) {
            return null;
        }
        Map<String,String> map = new HashMap<>();
        for (KeyValuePair tuple : tuples) {
            map.put(tuple.getKey(), tuple.getValue());
        }
        return map;
    }

    @Persist
    public String[] getLoadedNativeLibraries() {
        return loadedNativeLibraries;
    }

    @Persist
    public void setLoadedNativeLibraries(String[] loadedNativeLibraries) {
        this.loadedNativeLibraries = loadedNativeLibraries;
    }
    
    /**
     * Returns the system user id for the owner of this JVM process,
     * or -1 if an owner could not be found.
     */
    @Persist
    public long getUid() {
        return uid;
    }
    
    @Persist
    public void setUid(long uid) {
        this.uid = uid;
    }
    
    /**
     * Returns the system user name for the owner of this JVM process,
     * or null if an owner could not be found.
     */
    @Persist
    public String getUsername() {
        return username;
    }
    
    @Persist
    public void setUsername(String username) {
        this.username = username;
    }
}


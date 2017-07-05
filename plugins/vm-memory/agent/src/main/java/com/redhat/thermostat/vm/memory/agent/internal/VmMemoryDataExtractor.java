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

package com.redhat.thermostat.vm.memory.agent.internal;

import com.redhat.thermostat.jvm.overview.agent.VmUpdate;
import com.redhat.thermostat.jvm.overview.agent.VmUpdateException;
import com.redhat.thermostat.vm.memory.agent.model.VmMemoryStat.Generation;

/**
 * A helper class to provide type-safe access to commonly used jvmstat monitors
 * <p>
 * Implementation details: For local vms, jvmstat uses a ByteBuffer
 * corresponding to mmap()ed hsperfdata file. The hsperfdata file is updated
 * asynchronously by the vm that created the file. The polling that jvmstat api
 * provides is merely an abstraction over this (possibly always up-to-date)
 * ByteBuffer. So the data this class extracts is as current as possible, and
 * does not correspond to when the jvmstat update events fired.
 */
public class VmMemoryDataExtractor {

    /*
     * Note, there may be a performance issue to consider here. We have a lot of
     * string constants. When we start adding some of the more heavyweight
     * features, and running into CPU issues this may need to be reconsidered in
     * order to avoid the String pool overhead. See also:
     * http://docs.oracle.com/javase/6/docs/api/java/lang/String.html#intern()
     */

    private final VmUpdate update;

    public VmMemoryDataExtractor(VmUpdate update) {
        this.update = update;
    }
    
    public Long getTotalGcGenerations() throws VmUpdateException {
        return update.getPerformanceCounterLong("sun.gc.policy.generations");
    }

    public String getGenerationName(long generation) throws VmUpdateException {
        return update.getPerformanceCounterString("sun.gc.generation." + generation + ".name");
    }

    public Long getGenerationCapacity(long generation) throws VmUpdateException {
        return update.getPerformanceCounterLong("sun.gc.generation." + generation + ".capacity");
    }

    public Long getGenerationMaxCapacity(long generation) throws VmUpdateException {
        return update.getPerformanceCounterLong("sun.gc.generation." + generation + ".maxCapacity");
    }

    public String getGenerationCollector(long generation) throws VmUpdateException {
        // this is just re-implementing getCollectorName()
        // TODO check generation number and collector number are always associated
        String collector = update.getPerformanceCounterString("sun.gc.collector." + generation + ".name");
        if (collector == null) {
            collector = Generation.COLLECTOR_NONE;
        }
        return collector;
    }

    public Long getTotalSpaces(long generation) throws VmUpdateException {
        return update.getPerformanceCounterLong("sun.gc.generation." + generation + ".spaces");
    }

    public String getSpaceName(long generation, long space) throws VmUpdateException {
        return update.getPerformanceCounterString("sun.gc.generation." + generation + ".space." + space + ".name");
    }

    public Long getSpaceCapacity(long generation, long space) throws VmUpdateException {
        return update.getPerformanceCounterLong("sun.gc.generation." + generation + ".space." + space + ".capacity");
    }

    public Long getSpaceMaxCapacity(long generation, long space) throws VmUpdateException {
        return update.getPerformanceCounterLong("sun.gc.generation." + generation + ".space." + space + ".maxCapacity");
    }

    public Long getSpaceUsed(long generation, long space) throws VmUpdateException {
        return update.getPerformanceCounterLong("sun.gc.generation." + generation + ".space." + space + ".used");
    }

    public long getMetaspaceMaxCapacity(long defaultValue) {
        return getLongValueOrDefault("sun.gc.metaspace.maxCapacity", defaultValue);
    }

    public long getMetaspaceMinCapacity(long defaultValue) {
        return getLongValueOrDefault("sun.gc.metaspace.minCapacity", defaultValue);
    }

    public long getMetaspaceCapacity(long defaultValue) {
        return getLongValueOrDefault("sun.gc.metaspace.capacity", defaultValue);
    }

    public long getMetaspaceUsed(long defaultValue) {
        return getLongValueOrDefault("sun.gc.metaspace.used", defaultValue);
    }

    // See https://blogs.oracle.com/jonthecollector/entry/the_real_thing

    public long getTlabTotalAllocatingThreads(long defaultValue) {
        return getLongValueOrDefault("sun.gc.tlab.allocThreads", defaultValue);
    }

    public long getTlabTotalAllocations(long defaultValue) {
        return getLongValueOrDefault("sun.gc.tlab.alloc", defaultValue);
    }

    public long getTlabTotalRefills(long defaultValue) {
        return getLongValueOrDefault("sun.gc.tlab.fills", defaultValue);
    }

    public long getTlabMaxRefills(long defaultValue) {
        return getLongValueOrDefault("sun.gc.tlab.maxFills", defaultValue);
    }

    public long getTlabTotalSlowAllocs(long defaultValue) {
        return getLongValueOrDefault("sun.gc.tlab.slowAlloc", defaultValue);
    }

    public long getTlabMaxSlowAllocs(long defaultValue) {
        return getLongValueOrDefault("sun.gc.tlab.maxSlowAlloc", defaultValue);
    }

    public long getTlabTotalGcWaste(long defaultValue) {
        return getLongValueOrDefault("sun.gc.tlab.gcWaste", defaultValue);
    }

    public long getTlabMaxGcWaste(long defaultValue) {
        return getLongValueOrDefault("sun.gc.tlab.maxGcWaste", defaultValue);
    }

    public long getTlabTotalSlowWaste(long defaultValue) {
        return getLongValueOrDefault("sun.gc.tlab.slowWaste", defaultValue);
    }

    public long getTlabMaxSlowWaste(long defaultValue) {
        return getLongValueOrDefault("sun.gc.tlab.maxSlowWaste", defaultValue);
    }

    public long getTlabTotalFastWaste(long defaultValue) {
        return getLongValueOrDefault("sun.gc.tlab.fastWaste", defaultValue);
    }

    public long getTlabMaxFastWaste(long defaultValue) {
        return getLongValueOrDefault("sun.gc.tlab.maxFastWaste", defaultValue);
    }

    /** package private for testing */
    long getLongValueOrDefault(String name, long defaultValue) {
        try {
            Long value = update.getPerformanceCounterLong(name);
            if (value == null) {
                return defaultValue;
            }
            return value.longValue();
        } catch (VmUpdateException e) {
            return defaultValue;
        }
    }

}


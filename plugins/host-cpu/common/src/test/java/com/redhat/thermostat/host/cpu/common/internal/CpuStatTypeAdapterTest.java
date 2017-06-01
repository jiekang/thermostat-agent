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

import com.redhat.thermostat.host.cpu.common.model.CpuStat;
import com.google.gson.GsonBuilder;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;

import java.util.List;
import java.util.ArrayList;
import java.lang.reflect.Type;

public class CpuStatTypeAdapterTest {

    @Test
    public void testTypeAdapterSerializesSingletonCorrectly() {
        GsonBuilder builder = new GsonBuilder();
        Type cpuStatListType = new TypeToken<ArrayList<CpuStat>>(){}.getType();
        builder.registerTypeAdapter(cpuStatListType, new CpuStatTypeAdapter().nullSafe());
        Gson gson = builder.create();
        List<CpuStat> stats = new ArrayList<>();
        stats.add(new CpuStat("1", 1234567890l, new double[]{9.99, 8.88, 3.1415, 7.777}));
        String result = gson.toJson(stats, cpuStatListType);
        assertEquals("[{\"perProcessorUsage\":[9.99,8.88,3.1415,7.777],\"timeStamp\":{\"$numberLong\":\"1234567890\"},\"agentId\":\"1\"}]", result);
    }

    @Test
    public void testTypeAdapterSerializesListCorrectly() {
        GsonBuilder builder = new GsonBuilder();
        Type cpuStatListType = new TypeToken<ArrayList<CpuStat>>(){}.getType();
        builder.registerTypeAdapter(cpuStatListType, new CpuStatTypeAdapter().nullSafe());
        Gson gson = builder.create();
        List<CpuStat> stats = new ArrayList<>();
        stats.add(new CpuStat("1", 1000230101l, new double[]{1.23, 4.56, 7.89, 10.1112}));
        stats.add(new CpuStat("2", 10002333101l, new double[]{1.323, 4.456, 7.789, 10.101112}));
        stats.add(new CpuStat("3", 10002320101l, new double[]{1.234, 4.567, 7.8910, 10.111213}));
        stats.add(new CpuStat("4", 100023313101l, new double[]{1.3235, 4.4567, 7.78911, 10.10111241}));
        String result = gson.toJson(stats, cpuStatListType);
        assertEquals("[{\"perProcessorUsage\":[1.23,4.56,7.89,10.1112],\"timeStamp\":{\"$numberLong\":\"1000230101\"},\"agentId\":\"1\"}," +
                "{\"perProcessorUsage\":[1.323,4.456,7.789,10.101112],\"timeStamp\":{\"$numberLong\":\"10002333101\"},\"agentId\":\"2\"}," +
                "{\"perProcessorUsage\":[1.234,4.567,7.891,10.111213],\"timeStamp\":{\"$numberLong\":\"10002320101\"},\"agentId\":\"3\"}," +
                "{\"perProcessorUsage\":[1.3235,4.4567,7.78911,10.10111241],\"timeStamp\":{\"$numberLong\":\"100023313101\"},\"agentId\":\"4\"}]", result);
    }

    @Test
    public void testTypeAdapterDeserializesSingletonCorrectly() {
        GsonBuilder builder = new GsonBuilder();
        Type cpuStatListType = new TypeToken<ArrayList<CpuStat>>(){}.getType();
        builder.registerTypeAdapter(cpuStatListType, new CpuStatTypeAdapter().nullSafe());
        Gson gson = builder.create();
        String serialized = "{\"response\":[{\"perProcessorUsage\":[1.23,4.56,7.89,11.1],\"timeStamp\":{\"$numberLong\":\"12345\"},\"agentId\":\"1\"}],\"time\":\"123\"}";
        List<CpuStat> result = gson.fromJson(serialized, cpuStatListType);
        CpuStat stat = result.get(0);
        assertEquals(12345, stat.getTimeStamp());
        assertArrayEquals(new double[]{1.23, 4.56, 7.89, 11.1}, stat.getPerProcessorUsage(), 0.000001);
        assertEquals("1", stat.getAgentId());
    }

    @Test
    public void testTypeAdapterDeserializesListCorrectly() {
        GsonBuilder builder = new GsonBuilder();
        Type cpuStatListType = new TypeToken<List<CpuStat>>(){}.getType();
        builder.registerTypeAdapter(cpuStatListType, new CpuStatTypeAdapter().nullSafe());
        Gson gson = builder.create();
        String serialized = "{\"response\":[" +
                "{\"perProcessorUsage\":[1.23,4.56,7.89,11.1],\"timeStamp\":{\"$numberLong\":\"12345\"},\"agentId\":\"1\"}," +
                "{\"perProcessorUsage\":[3.21,6.54,9.87,1.11],\"timeStamp\":{\"$numberLong\":\"54321\"},\"agentId\":\"2\"}," +
                "{\"perProcessorUsage\":[3.1415,2.765,9.999,1.11234],\"timeStamp\":{\"$numberLong\":\"98765\"},\"agentId\":\"3\"}," +
                "{\"perProcessorUsage\":[444.678,9000.99,1243.5654,7.897867],\"timeStamp\":{\"$numberLong\":\"56789\"},\"agentId\":\"4\"}" +
                "],\"time\":\"123\"}";
        List<CpuStat> result = gson.fromJson(serialized, cpuStatListType);
        CpuStat stat = result.get(0);
        assertEquals(12345, stat.getTimeStamp());
        assertArrayEquals(new double[]{1.23, 4.56, 7.89, 11.1}, stat.getPerProcessorUsage(), 0.000001);
        assertEquals("1", stat.getAgentId());
        stat = result.get(1);
        assertEquals(54321, stat.getTimeStamp());
        assertArrayEquals(new double[]{3.21,6.54,9.87,1.11}, stat.getPerProcessorUsage(), 0.000001);
        assertEquals("2", stat.getAgentId());
        stat = result.get(2);
        assertEquals(98765, stat.getTimeStamp());
        assertArrayEquals(new double[]{3.1415,2.765,9.999,1.11234}, stat.getPerProcessorUsage(), 0.000001);
        assertEquals("3", stat.getAgentId());
        stat = result.get(3);
        assertEquals(56789, stat.getTimeStamp());
        assertArrayEquals(new double[]{444.678,9000.99,1243.5654,7.897867}, stat.getPerProcessorUsage(), 0.000001);
        assertEquals("4", stat.getAgentId());
    }

}

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

import com.google.gson.reflect.TypeToken;
import org.junit.Test;

import static com.redhat.thermostat.testutils.JsonUtils.assertJsonEquals;

import com.redhat.thermostat.numa.common.NumaNodeStat;
import com.redhat.thermostat.numa.common.NumaStat;

import com.google.gson.GsonBuilder;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.lang.reflect.Type;

public class NumaStatTypeAdapterTest {

    @Test
    public void testNumaNodeStatGetsSerializedCorrectly() {
        GsonBuilder builder = new GsonBuilder();
        Type list_type = new TypeToken<List<NumaStat>>(){}.getType();
        builder.registerTypeAdapter(list_type, new NumaStatTypeAdapter().nullSafe());
        Gson gson = builder.create();
        List<NumaStat> testStats = new ArrayList<>();
        NumaStat stat = new NumaStat("1");
        stat.setTimeStamp(2l);
        NumaNodeStat[] stats = new NumaNodeStat[1];
        NumaNodeStat s = new NumaNodeStat();
        s.setNumaHit(1l);
        s.setNumaMiss(2l);
        s.setNumaForeign(3l);
        s.setInterleaveHit(4l);
        s.setLocalNode(5l);
        s.setOtherNode(6l);
        s.setNodeId(7);
        stats[0] = s;
        stat.setNodeStats(stats);
        testStats.add(stat);
        assertJsonEquals("[{\"timeStamp\":{\"$numberLong\":\"2\"}," +
                "\"nodeStats\":[{\"numaHit\":{\"$numberLong\":\"1\"}," +
                "\"numaMiss\":{\"$numberLong\":\"2\"}," +
                "\"numaForeign\":{\"$numberLong\":\"3\"}," +
                "\"interleaveHit\":{\"$numberLong\":\"4\"}," +
                "\"localNode\":{\"$numberLong\":\"5\"}," +
                "\"otherNode\":{\"$numberLong\":\"6\"}," +
                "\"nodeId\":7" +
                "}],\"agentId\":\"1\"}]", gson.toJson(testStats, list_type));
    }

    @Test
    public void testMultipleNumaStatGetSerializedCorrectly() {
        GsonBuilder builder = new GsonBuilder();
        Type list_type = new TypeToken<List<NumaStat>>(){}.getType();
        builder.registerTypeAdapter(list_type, new NumaStatTypeAdapter().nullSafe());
        Gson gson = builder.create();
        List<NumaStat> testStats = new ArrayList<>();
        NumaStat stat = new NumaStat("1");
        NumaStat stat2 = new NumaStat("2");
        stat.setTimeStamp(2l);
        NumaNodeStat[] stats = new NumaNodeStat[1];
        NumaNodeStat[] stats2 = new NumaNodeStat[1];
        NumaNodeStat s = new NumaNodeStat();
        s.setNumaHit(1l);
        s.setNumaMiss(2l);
        s.setNumaForeign(3l);
        s.setInterleaveHit(4l);
        s.setLocalNode(5l);
        s.setOtherNode(6l);
        s.setNodeId(7);
        stats[0] = s;
        stat.setNodeStats(stats);
        NumaNodeStat s2 = new NumaNodeStat();
        s2.setNumaHit(10l);
        s2.setNumaMiss(20l);
        s2.setNumaForeign(30l);
        s2.setInterleaveHit(40l);
        s2.setLocalNode(50l);
        s2.setOtherNode(60l);
        s2.setNodeId(70);
        stats2[0] = s2;
        stat2.setNodeStats(stats2);
        testStats.add(stat);
        testStats.add(stat2);
        assertJsonEquals("[{\"timeStamp\":{\"$numberLong\":\"2\"},\"nodeStats\":[{\"numaHit\":{\"$numberLong\":\"1\"},\"numaMiss\":{\"$numberLong\":\"2\"},\"numaForeign\":{\"$numberLong\":\"3\"},\"interleaveHit\":{\"$numberLong\":\"4\"},\"localNode\":{\"$numberLong\":\"5\"},\"otherNode\":{\"$numberLong\":\"6\"},\"nodeId\":7}],\"agentId\":\"1\"},{\"timeStamp\":{\"$numberLong\":\"-1\"},\"nodeStats\":[{\"numaHit\":{\"$numberLong\":\"10\"},\"numaMiss\":{\"$numberLong\":\"20\"},\"numaForeign\":{\"$numberLong\":\"30\"},\"interleaveHit\":{\"$numberLong\":\"40\"},\"localNode\":{\"$numberLong\":\"50\"},\"otherNode\":{\"$numberLong\":\"60\"},\"nodeId\":70}],\"agentId\":\"2\"}]", gson.toJson(testStats, list_type));
    }
}
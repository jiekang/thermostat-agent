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

package com.redhat.thermostat.host.memory.common.internal;

import com.redhat.thermostat.host.memory.common.model.MemoryStat;

import com.google.gson.GsonBuilder;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.junit.Test;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class MemoryStatTypeAdapterTest {

    @Test
    public void testMemoryStatGetsSerializedCorrectly() {
        GsonBuilder builder = new GsonBuilder();
        Type memoryStatListType = new TypeToken<ArrayList<MemoryStat>>(){}.getType();
        builder.registerTypeAdapter(memoryStatListType, new MemoryStatTypeAdapter().nullSafe());
        Gson gson = builder.create();
        List<MemoryStat> stats = new ArrayList<>();
        stats.add(new MemoryStat("1", 123l, 456l, 789l, 101112l, 131415l,
                161718l, 192021l, 222324l));
        System.out.println(gson.toJson(stats, memoryStatListType));
        assertEquals("[{\"timeStamp\":{\"$numberLong\":\"123\"}," +
                "\"total\":{\"$numberLong\":\"456\"}," +
                "\"free\":{\"$numberLong\":\"789\"}," +
                "\"buffers\":{\"$numberLong\":\"101112\"}," +
                "\"cached\":{\"$numberLong\":\"131415\"}," +
                "\"swapTotal\":{\"$numberLong\":\"161718\"}," +
                "\"swapFree\":{\"$numberLong\":\"192021\"}," +
                "\"commitLimit\":{\"$numberLong\":\"222324\"}," +
                "\"agentId\":\"1\"}]",
                gson.toJson(stats, memoryStatListType));
    }

    @Test
    public void testMultipleMemoryStatsGetSerializedCorrectly() {
        GsonBuilder builder = new GsonBuilder();
        Type memoryStatListType = new TypeToken<ArrayList<MemoryStat>>(){}.getType();
        builder.registerTypeAdapter(memoryStatListType, new MemoryStatTypeAdapter().nullSafe());
        Gson gson = builder.create();
        List<MemoryStat> stats = new ArrayList<>();
        stats.add(new MemoryStat("1", 123l, 456l, 789l, 101112l, 131415l,
                161718l, 192021l, 222324l));
        stats.add(new MemoryStat("2", 1l, 2l, 3l, 4l, 5l,6l, 7l, 8l));
        stats.add(new MemoryStat("3", 17756l, 25365323l, 3124213l, 4465434l, 578687l,
                689821l, 786711l, 823542l));
        stats.add(new MemoryStat("4", 13332l, 24441l, 37721l, 4321345l, 542131l,
                64522l, 71232l, 8231321l));
        assertEquals("[{\"timeStamp\":{\"$numberLong\":\"123\"},\"total\":{\"$numberLong\":\"456\"},\"free\":{\"$numberLong\":\"789\"},\"buffers\":{\"$numberLong\":\"101112\"},\"cached\":{\"$numberLong\":\"131415\"},\"swapTotal\":{\"$numberLong\":\"161718\"},\"swapFree\":{\"$numberLong\":\"192021\"},\"commitLimit\":{\"$numberLong\":\"222324\"},\"agentId\":\"1\"}," +
                "{\"timeStamp\":{\"$numberLong\":\"1\"},\"total\":{\"$numberLong\":\"2\"},\"free\":{\"$numberLong\":\"3\"},\"buffers\":{\"$numberLong\":\"4\"},\"cached\":{\"$numberLong\":\"5\"},\"swapTotal\":{\"$numberLong\":\"6\"},\"swapFree\":{\"$numberLong\":\"7\"},\"commitLimit\":{\"$numberLong\":\"8\"},\"agentId\":\"2\"}," +
                "{\"timeStamp\":{\"$numberLong\":\"17756\"},\"total\":{\"$numberLong\":\"25365323\"},\"free\":{\"$numberLong\":\"3124213\"},\"buffers\":{\"$numberLong\":\"4465434\"},\"cached\":{\"$numberLong\":\"578687\"},\"swapTotal\":{\"$numberLong\":\"689821\"},\"swapFree\":{\"$numberLong\":\"786711\"},\"commitLimit\":{\"$numberLong\":\"823542\"},\"agentId\":\"3\"}," +
                "{\"timeStamp\":{\"$numberLong\":\"13332\"},\"total\":{\"$numberLong\":\"24441\"},\"free\":{\"$numberLong\":\"37721\"},\"buffers\":{\"$numberLong\":\"4321345\"},\"cached\":{\"$numberLong\":\"542131\"},\"swapTotal\":{\"$numberLong\":\"64522\"},\"swapFree\":{\"$numberLong\":\"71232\"},\"commitLimit\":{\"$numberLong\":\"8231321\"},\"agentId\":\"4\"}]",
                gson.toJson(stats, memoryStatListType));
    }

}

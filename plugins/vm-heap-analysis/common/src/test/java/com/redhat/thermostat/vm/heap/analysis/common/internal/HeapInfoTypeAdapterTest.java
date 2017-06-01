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

package com.redhat.thermostat.vm.heap.analysis.common.internal;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.redhat.thermostat.vm.heap.analysis.common.model.HeapInfo;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class HeapInfoTypeAdapterTest {

    @Test
    public void testHeapInfoGetsSerializedCorrectly() {
        GsonBuilder builder = new GsonBuilder();
        Type list_type = new TypeToken<List<HeapInfo>>(){}.getType();
        builder.registerTypeAdapter(list_type, new HeapInfoTypeAdapter());
        Gson gson = builder.create();
        List<HeapInfo> infos = new ArrayList<>();
        HeapInfo info = new HeapInfo("1", "1", 100l);
        info.setHeapDumpId("1234f");
        info.setHeapId("2432a");
        info.setHistogramId("5675n");
        infos.add(info);
        assertEquals("[{\"agentId\":\"1\",\"vmId\":\"1\",\"timeStamp\":{\"$numberLong\":\"100\"},\"heapId\":\"2432a\",\"heapDumpId\":\"1234f\",\"histogramId\":\"5675n\"}]", gson.toJson(infos, list_type));
    }

    @Test
    public void testMultipleHeapInfoGetsSerializedCorrectly() {
        GsonBuilder builder = new GsonBuilder();
        Type list_type = new TypeToken<List<HeapInfo>>(){}.getType();
        builder.registerTypeAdapter(list_type, new HeapInfoTypeAdapter());
        Gson gson = builder.create();
        List<HeapInfo> infos = new ArrayList<>();
        HeapInfo info = new HeapInfo("1", "1", 100l);
        info.setHeapDumpId("1234f");
        info.setHeapId("2432a");
        info.setHistogramId("5675n");
        infos.add(info);
        HeapInfo info2 = new HeapInfo("2", "2", 200l);
        info.setHeapDumpId("21234ff");
        info.setHeapId("22432aa");
        info.setHistogramId("25675nn");
        infos.add(info2);
        assertEquals("[{\"agentId\":\"1\",\"vmId\":\"1\",\"timeStamp\":{\"$numberLong\":\"100\"},\"heapId\":\"22432aa\",\"heapDumpId\":\"21234ff\",\"histogramId\":\"25675nn\"},{\"agentId\":\"2\",\"vmId\":\"2\",\"timeStamp\":{\"$numberLong\":\"200\"}}]", gson.toJson(infos, list_type));
    }
}

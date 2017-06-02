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

package com.redhat.thermostat.vm.memory.common.internal;

import com.google.gson.GsonBuilder;
import com.google.gson.Gson;
import com.redhat.thermostat.vm.memory.common.model.VmMemoryStat;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

import static com.redhat.thermostat.vm.memory.common.model.VmMemoryStat.Generation;
import static com.redhat.thermostat.vm.memory.common.model.VmMemoryStat.Space;
import static org.junit.Assert.assertEquals;

public class VmMemoryStatTypeAdapterTest {

    @Test
    public void testWrite() throws IOException {
        VmMemoryStatTypeAdapter typeAdapter = new VmMemoryStatTypeAdapter();
        VmMemoryStat stat = new VmMemoryStat();
        stat.setTimeStamp(100l);
        stat.setVmId("VM-1");
        stat.setAgentId("AGENT-1");
        stat.setMetaspaceCapacity(2000l);
        stat.setMetaspaceMaxCapacity(4096l);
        stat.setMetaspaceMinCapacity(2048l);
        stat.setMetaspaceUsed(3000l);
        Generation[] gens = new Generation[1];
        Generation gen1 = new Generation();
        gen1.setCapacity(1002l);
        gen1.setCollector("Collector 1");
        gen1.setMaxCapacity(2048l);
        gen1.setName("Name");
        Space[] spaces = new Space[1];
        Space space = new Space();
        space.setName("Space Name");
        space.setMaxCapacity(1024l);
        space.setCapacity(500l);
        space.setIndex(1);
        space.setUsed(400l);
        spaces[0] = space;
        gen1.setSpaces(spaces);
        gens[0] = gen1;
        stat.setGenerations(gens);
        assertEquals("[{\"agentId\":\"AGENT-1\",\"jvmId\":\"VM-1\",\"timeStamp\":{\"$numberLong\":\"100\"},\"metaspaceMaxCapacity\":{\"$numberLong\":\"4096\"},\"metaspaceMinCapacity\":{\"$numberLong\":\"2048\"},\"metaspaceCapacity\":{\"$numberLong\":\"2000\"},\"metaspaceUsed\":{\"$numberLong\":\"3000\"},\"generations\":[{\"name\":\"Name\",\"capacity\":{\"$numberLong\":\"1002\"},\"maxCapacity\":{\"$numberLong\":\"2048\"},\"collector\":\"Collector 1\",\"spaces\":[{\"index\":1,\"name\":\"Space Name\",\"capacity\":{\"$numberLong\":\"500\"},\"maxCapacity\":{\"$numberLong\":\"1024\"},\"used\":{\"$numberLong\":\"400\"}}]}]}]", typeAdapter.toJson(Arrays.asList(stat)));
    }
}
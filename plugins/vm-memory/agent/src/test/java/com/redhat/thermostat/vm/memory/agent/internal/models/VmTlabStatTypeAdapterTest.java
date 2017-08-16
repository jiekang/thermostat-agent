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

package com.redhat.thermostat.vm.memory.agent.internal.models;

import com.redhat.thermostat.vm.memory.agent.model.VmTlabStat;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

import static com.redhat.thermostat.testutils.JsonUtils.assertJsonEquals;

public class VmTlabStatTypeAdapterTest {

    @Test
    public void testWrite() throws IOException {
        VmTlabStatTypeAdapter typeAdapter = new VmTlabStatTypeAdapter();
        VmTlabStat stat = new VmTlabStat();
        stat.setAgentId("AGENT-1");
        stat.setJvmId("VM-1");
        stat.setTimeStamp(1000l);
        stat.setTotalAllocatingThreads(10l);
        stat.setTotalAllocations(1342l);
        stat.setTotalRefills(58l);
        stat.setMaxRefills(90l);
        stat.setTotalSlowAllocations(343l);
        stat.setMaxSlowAllocations(989l);
        stat.setTotalGcWaste(788l);
        stat.setMaxGcWaste(992l);
        stat.setTotalSlowWaste(899l);
        stat.setMaxSlowWaste(634l);
        stat.setTotalFastWaste(678l);
        stat.setMaxFastWaste(333l);
        assertJsonEquals("[{\"jvmId\":\"VM-1\",\"agentId\":\"AGENT-1\",\"timeStamp\":{\"$numberLong\":\"1000\"},\"allocThreads\":{\"$numberLong\":\"10\"},\"totalAllocations\":{\"$numberLong\":\"1342\"},\"refills\":{\"$numberLong\":\"58\"},\"maxRefills\":{\"$numberLong\":\"90\"},\"slowAllocations\":{\"$numberLong\":\"343\"},\"maxSlowAllocations\":{\"$numberLong\":\"989\"},\"gcWaste\":{\"$numberLong\":\"788\"},\"maxGcWaste\":{\"$numberLong\":\"992\"},\"slowWaste\":{\"$numberLong\":\"899\"},\"maxSlowWaste\":{\"$numberLong\":\"634\"},\"fastWaste\":{\"$numberLong\":\"678\"},\"maxFastWaste\":{\"$numberLong\":\"333\"}}]", typeAdapter.toJson(Arrays.asList(stat)));
    }

}

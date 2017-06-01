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

package com.redhat.thermostat.vm.compiler.common.internal;

import com.google.gson.GsonBuilder;
import com.google.gson.Gson;
import com.redhat.thermostat.vm.compiler.common.VmCompilerStat;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class VmCompilerStatTypeAdapterTest {

    @Test
    public void testRead() {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(VmCompilerStat.class, new VmCompilerStatTypeAdapter());
        Gson gson = builder.create();
        VmCompilerStat stat = new VmCompilerStat();
        stat.setTimeStamp(100l);
        stat.setVmId("1");
        stat.setAgentId("1");
        stat.setCompilationTime(20l);
        stat.setLastFailedMethod("methodFail()");
        stat.setLastFailedType(1l);
        stat.setLastMethod("successfulMethod()");
        stat.setLastType(2l);
        stat.setLastSize(300l);
        stat.setTotalInvalidates(10l);
        stat.setTotalBailouts(30l);
        stat.setTotalCompiles(40l);
        System.out.println(gson.toJson(stat));
        assertEquals("{\"agentId\":\"1\",\"vmId\":\"1\",\"timeStamp\":{\"$numberLong\":\"100\"},\"totalCompiles\":{\"$numberLong\":\"40\"},\"totalBailouts\":{\"$numberLong\":\"30\"},\"totalInvalidates\":{\"$numberLong\":\"10\"},\"compilationTime\":{\"$numberLong\":\"20\"},\"lastSize\":{\"$numberLong\":\"300\"},\"lastType\":{\"$numberLong\":\"2\"},\"lastMethod\":\"successfulMethod()\",\"lastFailedType\":{\"$numberLong\":\"1\"},\"lastFailedMethod\":\"methodFail()\"}", gson.toJson(stat));
    }
}

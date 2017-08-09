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

package com.redhat.thermostat.thread.dao.internal;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.redhat.thermostat.thread.model.LockInfo;
import org.junit.Test;

import static com.redhat.thermostat.testutils.JsonUtils.assertJsonEquals;

public class LockInfoTypeAdapterTest {

    @Test
    public void testWrite() {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(LockInfo.class, new LockInfoTypeAdapter());
        Gson gson = builder.create();
        LockInfo info = new LockInfo(100l, "Agent-1", "Vm-1", 123l, 321l, 432l, 5454l, 578l, 678574l, 21349l, 0l,
                2342342l, 12311l, 13211l, 934l, 8911l, 305934l, 2194l, 892l, 100l, 2321l);
        assertJsonEquals("{\"agentId\":\"Agent-1\",\"vmId\":\"Vm-1\",\"timeStamp\":{\"$numberLong\":\"100\"},\"contendedLockAttempts\":{\"$numberLong\":\"123\"},\"deflations\":{\"$numberLong\":\"321\"},\"emptyNotifications\":{\"$numberLong\":\"432\"},\"failedSpins\":{\"$numberLong\":\"5454\"},\"futileWakeups\":{\"$numberLong\":\"578\"},\"inflations\":{\"$numberLong\":\"678574\"},\"monExtant\":{\"$numberLong\":\"21349\"},\"monInCirculation\":{\"$numberLong\":\"0\"},\"monScavenged\":{\"$numberLong\":\"2342342\"},\"notifications\":{\"$numberLong\":\"12311\"},\"parks\":{\"$numberLong\":\"13211\"},\"privateA\":{\"$numberLong\":\"934\"},\"privateB\":{\"$numberLong\":\"8911\"},\"slowEnter\":{\"$numberLong\":\"305934\"},\"slowExit\":{\"$numberLong\":\"2194\"},\"slowNotify\":{\"$numberLong\":\"892\"},\"slowNotifyAll\":{\"$numberLong\":\"100\"},\"successfulSpins\":{\"$numberLong\":\"2321\"}}", gson.toJson(info));
    }
}

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

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.redhat.thermostat.thread.model.LockInfo;

import java.io.IOException;

public class LockInfoTypeAdapter extends TypeAdapter<LockInfo> {

    private static final String TIMESTAMP = "timeStamp";
    private static final String TYPE_LONG = "$numberLong";
    private static final String VM_ID = "vmId";
    private static final String AGENT_ID = "agentId";
    private static final String CONTENDED_ATTEMPTS = "contendedLockAttempts";
    private static final String DEFLATIONS = "deflations";
    private static final String EMPTY_NOTIFICATIONS = "emptyNotifications";
    private static final String FAILED_SPINS = "failedSpins";
    private static final String FUTILE_WAKEUPS = "futileWakeups";
    private static final String INFLATIONS = "inflations";
    private static final String EXTANT_MONITORS = "monExtant";
    private static final String MON_IN_CIRCULATION = "monInCirculation";
    private static final String SCAVENGED_MONITORS = "monScavenged";
    private static final String NOTIFICATIONS = "notifications";
    private static final String PARKS = "parks";
    private static final String PRIVATE_A = "privateA";
    private static final String PRIVATE_B = "privateB";
    private static final String SLOW_ENTER = "slowEnter";
    private static final String SLOW_EXIT = "slowExit";
    private static final String SLOW_NOTIFY = "slowNotify";
    private static final String SLOW_NOTIFY_ALL = "slowNotifyAll";
    private static final String SUCCESSFUL_SPINS = "successfulSpins";


    @Override
    public void write(JsonWriter out, LockInfo info) throws IOException {
        out.beginObject();
        out.name(AGENT_ID);
        out.value(info.getAgentId());
        out.name(VM_ID);
        out.value(info.getVmId());
        out.name(TIMESTAMP);
        writeLong(out, info.getTimeStamp());
        out.name(CONTENDED_ATTEMPTS);
        writeLong(out, info.getContendedLockAttempts());
        out.name(DEFLATIONS);
        writeLong(out, info.getDeflations());
        out.name(EMPTY_NOTIFICATIONS);
        writeLong(out, info.getEmptyNotifications());
        out.name(FAILED_SPINS);
        writeLong(out, info.getFailedSpins());
        out.name(FUTILE_WAKEUPS);
        writeLong(out, info.getFutileWakeups());
        out.name(INFLATIONS);
        writeLong(out, info.getInflations());
        out.name(EXTANT_MONITORS);
        writeLong(out, info.getMonExtant());
        out.name(MON_IN_CIRCULATION);
        writeLong(out, info.getMonInCirculation());
        out.name(SCAVENGED_MONITORS);
        writeLong(out, info.getMonScavenged());
        out.name(NOTIFICATIONS);
        writeLong(out, info.getNotifications());
        out.name(PARKS);
        writeLong(out, info.getParks());
        out.name(PRIVATE_A);
        writeLong(out, info.getPrivateA());
        out.name(PRIVATE_B);
        writeLong(out, info.getPrivateB());
        out.name(SLOW_ENTER);
        writeLong(out, info.getSlowEnter());
        out.name(SLOW_EXIT);
        writeLong(out, info.getSlowExit());
        out.name(SLOW_NOTIFY);
        writeLong(out, info.getSlowNotify());
        out.name(SLOW_NOTIFY_ALL);
        writeLong(out, info.getSlowNotifyAll());
        out.name(SUCCESSFUL_SPINS);
        writeLong(out, info.getSuccessfulSpins());
        out.endObject();
    }

    @Override
    public LockInfo read(JsonReader in) throws IOException {
        throw new UnsupportedOperationException();
    }

    private void writeLong(JsonWriter out, long value) throws IOException {
        // Write MongoDB representation of a Long
        out.beginObject();
        out.name(TYPE_LONG);
        out.value(String.valueOf(value));
        out.endObject();
    }
}

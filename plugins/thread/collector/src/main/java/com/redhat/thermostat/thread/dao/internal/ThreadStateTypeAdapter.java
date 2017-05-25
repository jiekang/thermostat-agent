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
import com.redhat.thermostat.thread.model.ThreadState;

import java.io.IOException;

public class ThreadStateTypeAdapter extends TypeAdapter<ThreadState> {

    private static final String AGENT_ID = "agentId";
    private static final String VM_ID = "vmId";
    private static final String NAME = "name";
    private static final String STATE = "state";
    private static final String SESSION = "session";
    private static final String ID = "id";
    private static final String SUSPENDED = "suspended";
    private static final String IN_NATIVE = "inNative";
    private static final String BLOCKED_COUNT = "blockedCount";
    private static final String BLOCKED_TIME = "blockedTime";
    private static final String WAITED_COUNT = "waitedCount";
    private static final String WAITED_TIME = "waitedTime";
    private static final String STACKTRACE = "stackTrace";
    private static final String TYPE_LONG = "$numberLong";

    @Override
    public ThreadState read(JsonReader in) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void write(JsonWriter out, ThreadState state) throws IOException {
        out.beginObject();
        out.name(AGENT_ID);
        out.value(state.getAgentId());
        out.name(VM_ID);
        out.value(state.getVmId());
        out.name(NAME);
        out.value(state.getName());
        out.name(STATE);
        out.value(state.getState());
        out.name(SESSION);
        out.value(state.getSession());
        out.name(ID);
        writeLong(out, state.getId());
        out.name(SUSPENDED);
        out.value(state.isSuspended());
        out.name(IN_NATIVE);
        out.value(state.isInNative());
        out.name(BLOCKED_COUNT);
        writeLong(out, state.getBlockedCount());
        out.name(BLOCKED_TIME);
        writeLong(out, state.getBlockedTime());
        out.name(WAITED_COUNT);
        writeLong(out, state.getWaitedCount());
        out.name(WAITED_TIME);
        writeLong(out, state.getWaitedTime());
        out.name(STACKTRACE);
        out.value(state.getStackTrace());
        out.endObject();
    }

    private void writeLong(JsonWriter out, long value) throws IOException {
        // Write MongoDB representation of a Long
        out.beginObject();
        out.name(TYPE_LONG);
        out.value(String.valueOf(value));
        out.endObject();
    }
}

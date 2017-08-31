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

package com.redhat.thermostat.vm.io.model;

import com.redhat.thermostat.storage.core.Entity;
import com.redhat.thermostat.storage.core.Persist;
import com.redhat.thermostat.storage.model.BasePojo;
import com.redhat.thermostat.storage.model.TimeStampedPojo;

@Entity
public class VmIoStat extends BasePojo implements TimeStampedPojo {

    private long timeStamp;
    private String jvmId;

    private long charactersRead;
    private long charactersWritten;
    private long readSyscalls;
    private long writeSyscalls;

    public VmIoStat() {
        super(null);
    }

    public VmIoStat(String writerId, String jvmId, long timeStamp,
                    long charactersRead, long charactersWritten, long readSyscalls, long writeSyscalls) {
        super(writerId);
        this.jvmId = jvmId;
        this.timeStamp = timeStamp;
        this.charactersRead = charactersRead;
        this.charactersWritten = charactersWritten;
        this.readSyscalls = readSyscalls;
        this.writeSyscalls = writeSyscalls;
    }

    @Override
    @Persist
    public long getTimeStamp() {
        return timeStamp;
    }

    @Persist
    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    @Persist
    public String getJvmId() {
        return jvmId;
    }

    @Persist
    public void setJvmId(String jvmId) {
        this.jvmId = jvmId;
    }

    @Persist
    public long getCharactersRead() {
        return charactersRead;
    }

    @Persist
    public void setCharactersRead(long charactersRead) {
        this.charactersRead = charactersRead;
    }

    @Persist
    public long getCharactersWritten() {
        return charactersWritten;
    }

    @Persist
    public void setCharactersWritten(long charactersWritten) {
        this.charactersWritten = charactersWritten;
    }

    @Persist
    public long getReadSyscalls() {
        return readSyscalls;
    }

    @Persist
    public void setReadSyscalls(long readSyscalls) {
        this.readSyscalls = readSyscalls;
    }

    @Persist
    public long getWriteSyscalls() {
        return writeSyscalls;
    }

    @Persist
    public void setWriteSyscalls(long writeSyscalls) {
        this.writeSyscalls = writeSyscalls;
    }

}

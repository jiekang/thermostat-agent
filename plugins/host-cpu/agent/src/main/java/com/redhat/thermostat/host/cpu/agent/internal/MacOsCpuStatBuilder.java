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

package com.redhat.thermostat.host.cpu.agent.internal;

import com.redhat.thermostat.common.Clock;
import com.redhat.thermostat.common.utils.LoggingUtils;
import com.redhat.thermostat.host.cpu.model.CpuStat;
import com.redhat.thermostat.storage.core.WriterID;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * STUB class
 */
public class MacOsCpuStatBuilder implements CpuStatBuilder {

    private static final Logger logger = LoggingUtils.getLogger(LinuxCpuStatBuilder.class);

    private final Clock clock;
    private final long ticksPerSecond;
    private final WriterID writerId;

    private boolean initialized = false;

    private long[] previousCpuTicks;
    private long previousTime;

    MacOsCpuStatBuilder(Clock clock, long ticksPerSecond, WriterID writerId) {
        this.writerId = writerId;
        this.clock = clock;
        this.ticksPerSecond = ticksPerSecond;
    }

    @Override
    public void initialize() {
        if (initialized) {
            throw new IllegalStateException("already initialized");
        }

        logger.severe("MacOsCpuStatBuilder is a stub class");
        previousTime = clock.getMonotonicTimeNanos();
        previousCpuTicks = getCurrentCpuTicks();
        initialized = true;
    }

    @Override
    public CpuStat build() {
        if (!initialized) {
            throw new IllegalStateException("not initialized yet");
        }

        long currentRealTime = clock.getRealTimeMillis();
        long currentTime = clock.getMonotonicTimeNanos();
        long[] currentValues = getCurrentCpuTicks();

        double[] cpuUsage = new double[currentValues.length];

        double timeDelta = (currentTime - previousTime) * 1E-9;
        for (int i = 0; i < currentValues.length; i++) {
            long cpuTicksDelta = currentValues[i] - previousCpuTicks[i];
            // 100 as in 100 percent.
            cpuUsage[i] = cpuTicksDelta * (100.0 / timeDelta / ticksPerSecond);
        }
        previousTime = currentTime;
        previousCpuTicks = currentValues;
        String wId = writerId.getWriterID();
        return new CpuStat(wId, currentRealTime, cpuUsage);
    }

    private long[] getCurrentCpuTicks() {
        int maxIndex = 0;
        long[] values = new long[1];
        values[0] = 1;

        return values;
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

}


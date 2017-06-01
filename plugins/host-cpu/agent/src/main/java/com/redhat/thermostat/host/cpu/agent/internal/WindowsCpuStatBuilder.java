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
import com.redhat.thermostat.common.portability.PortableHost;
import com.redhat.thermostat.common.portability.PortableHostFactory;
import com.redhat.thermostat.host.cpu.common.model.CpuStat;
import com.redhat.thermostat.storage.core.WriterID;

public class WindowsCpuStatBuilder implements CpuStatBuilder {

    private final Clock clock;
    private final WriterID writerId;
    private final PortableHost portableHost;

    private boolean initialized = false;

    WindowsCpuStatBuilder(Clock clock, WriterID writerId) {
        this.writerId = writerId;
        this.clock = clock;
        this.portableHost = PortableHostFactory.getInstance();
    }

    @Override
    public void initialize() {
        if (initialized) {
            throw new IllegalStateException("already initialized");
        }
        initialized = true;
    }

    @Override
    public  CpuStat build() {
        if (!initialized) {
            throw new IllegalStateException("not initialized yet");
        }
        /*
         * On Windows, we only have the aggegate over all CPUs.
         * But, we also get the idle ticks back.  So we don't need to use the delta time.
         * The calculation here is ((delta busy time) / (delta busy time + delta free time)) * 100%
         */
        final long currentRealTime = clock.getRealTimeMillis();
        int[][] currentValues = portableHost.getCPUUsagePercent();
        final int numCPUs = currentValues.length;
        final double[] cpuUsagePercent = new double[numCPUs];
        for (int i=0; i<numCPUs; i++) {
            // each cpu row returned from getCPUUsagePercent() has idle/system/user percents
            // calculate the usage as the non-idle time
            cpuUsagePercent[i] = (double)(100-currentValues[i][0]);
        }
        String wId = writerId.getWriterID();
        return new CpuStat(wId, currentRealTime, cpuUsagePercent);
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

}


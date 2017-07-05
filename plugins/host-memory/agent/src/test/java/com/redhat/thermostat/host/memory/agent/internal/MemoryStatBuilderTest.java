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

package com.redhat.thermostat.host.memory.agent.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.StringReader;

import com.redhat.thermostat.shared.config.OS;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import com.redhat.thermostat.common.portability.linux.ProcDataSource;
import com.redhat.thermostat.host.memory.model.MemoryStat;
import com.redhat.thermostat.storage.core.WriterID;

public class MemoryStatBuilderTest {

    private static final int KILOBYTES_TO_BYTES = 1024;
    
    private WriterID writerId;
    
    @Before
    public void setup() {
        writerId = mock(WriterID.class);
    }

    @Test
    public void testSimpleBuild() {
        MemoryStat stat = new MemoryStatBuilder(new ProcDataSource(), writerId).build();
        assertNotNull(stat);
        assertTrue(stat.getFree() <= stat.getTotal());
        assertTrue(stat.getSwapFree() <= stat.getSwapTotal());
    }

    @Test
    public void testEmptyBuild() throws IOException {
        Assume.assumeTrue(OS.IS_LINUX);
        String memory = "";
        StringReader memoryReader = new StringReader(memory);
        ProcDataSource dataSource = mock(ProcDataSource.class);
        when(dataSource.getMemInfoReader()).thenReturn(memoryReader);

        MemoryStat stat = new MemoryStatBuilder(dataSource, writerId).build();
        assertNotNull(stat);
        verify(dataSource).getMemInfoReader();
    }

    @Test
    public void testBuild() throws IOException {
        Assume.assumeTrue(OS.IS_LINUX);
        int i = 1;
        final long TOTAL = i++;
        final long FREE = i++;
        final long BUFFERS = i++;
        final long CACHED = i++;
        final long COMMIT_LIMIT = i++;
        final long SWAP_TOTAL = i++;
        final long SWAP_FREE = i++;

        String memory = "" +
                "MemTotal: " + TOTAL + " kB\n" +
                "MemFree:  " + FREE + " kB\n" +
                "Buffers:" + BUFFERS + " kB\n" +
                "Cached: " + CACHED + " kB\n" +
                "CommitLimit: " + COMMIT_LIMIT + " kB\n" +
                "SwapTotal: " + SWAP_TOTAL + " kB\n" +
                "SwapFree: " + SWAP_FREE + " kB\n";

        StringReader memoryReader = new StringReader(memory);
        ProcDataSource dataSource = mock(ProcDataSource.class);
        when(dataSource.getMemInfoReader()).thenReturn(memoryReader);

        MemoryStat stat = new MemoryStatBuilder(dataSource, writerId).build();

        assertEquals(BUFFERS * KILOBYTES_TO_BYTES, stat.getBuffers());
        assertEquals(CACHED * KILOBYTES_TO_BYTES, stat.getCached());
        assertEquals(COMMIT_LIMIT * KILOBYTES_TO_BYTES, stat.getCommitLimit());
        assertEquals(FREE * KILOBYTES_TO_BYTES, stat.getFree());
        assertEquals(SWAP_FREE * KILOBYTES_TO_BYTES, stat.getSwapFree());
        assertEquals(SWAP_TOTAL * KILOBYTES_TO_BYTES, stat.getSwapTotal());
        assertEquals(TOTAL * KILOBYTES_TO_BYTES, stat.getTotal());
        assertTrue(stat.getTimeStamp() != 0 && stat.getTimeStamp() != Long.MIN_VALUE);
        assertTrue(stat.getTimeStamp() <= System.currentTimeMillis());
        verify(dataSource).getMemInfoReader();
    }
}


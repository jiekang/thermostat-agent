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

package com.redhat.thermostat.common.portability.linux;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import com.redhat.thermostat.common.portability.linux.ProcDataSource.ReaderCreator;
import com.redhat.thermostat.shared.config.OS;

public class ProcDataSourceTest {
    
    private ProcDataSource source;
    private ReaderCreator readerCreator;
    
    @Before
    public void setup() throws Exception {
        readerCreator = mock(ReaderCreator.class);
        FileReader reader = mock(FileReader.class);
        when(readerCreator.createFileReader(anyString())).thenReturn(reader);
        source = new ProcDataSource(readerCreator);
    }

    @Test
    public void testGetCpuInfoReader() throws IOException {
        Assume.assumeTrue(OS.IS_LINUX);
        Reader r = source.getCpuInfoReader();
        assertNotNull(r);
        verify(readerCreator).createFileReader("/proc/cpuinfo");
    }

    @Test
    public void testGetCpuLoadReader() throws IOException {
        Assume.assumeTrue(OS.IS_LINUX);
        Reader r = source.getCpuLoadReader();
        assertNotNull(r);
        verify(readerCreator).createFileReader("/proc/loadavg");
    }

    @Test
    public void testGetMemInfoReader() throws IOException {
        Assume.assumeTrue(OS.IS_LINUX);
        Reader r = source.getMemInfoReader();
        assertNotNull(r);
        verify(readerCreator).createFileReader("/proc/meminfo");
    }

    @Test
    public void testGetStatReader() throws IOException {
        Assume.assumeTrue(OS.IS_LINUX);
        Reader r = source.getStatReader();
        assertNotNull(r);
        verify(readerCreator).createFileReader("/proc/stat");
    }

    @Test
    public void testGetEnvironReader() throws IOException {
        Assume.assumeTrue(OS.IS_LINUX);
        Reader r = source.getEnvironReader(1234);
        assertNotNull(r);
        verify(readerCreator).createFileReader("/proc/1234/environ");
    }

    @Test
    public void testIoReader() throws Exception {
        Assume.assumeTrue(OS.IS_LINUX);
        Reader r = source.getIoReader(1234);
        assertNotNull(r);
        verify(readerCreator).createFileReader("/proc/1234/io");
    }

    @Test
    public void testStatReader() throws Exception {
        Assume.assumeTrue(OS.IS_LINUX);
        Reader r = source.getStatReader(1234);
        assertNotNull(r);
        verify(readerCreator).createFileReader("/proc/1234/stat");
    }

    @Test
    public void testStatusReader() throws Exception {
        Assume.assumeTrue(OS.IS_LINUX);
        Reader r = source.getStatusReader(1234);
        assertNotNull(r);
        verify(readerCreator).createFileReader("/proc/1234/status");
    }

    @Test
    public void testNumaMapsReader() throws Exception {
        Assume.assumeTrue(OS.IS_LINUX);
        Reader r = source.getNumaMapsReader(1234);
        assertNotNull(r);
        verify(readerCreator).createFileReader("/proc/1234/numa_maps");
    }
}


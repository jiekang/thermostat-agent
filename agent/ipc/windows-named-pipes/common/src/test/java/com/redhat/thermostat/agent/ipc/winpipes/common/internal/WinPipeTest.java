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

package com.redhat.thermostat.agent.ipc.winpipes.common.internal;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class WinPipeTest {

    private WinPipesNativeHelper mockHelper;
    private final String PIPE_NAME = "pipeNameFoo";
    private final WinPipe.Id PIPE_ID = new WinPipe.Id(PIPE_NAME);

    @Before
    public void setup() {
        mockHelper = mock(WinPipesNativeHelper.class);
        when(mockHelper.openNamedPipe(anyString())).thenReturn(WinPipesNativeHelper.INVALID_HANDLE);
        when(mockHelper.openNamedPipe(eq(PIPE_NAME))).thenReturn(999L);
        when(mockHelper.createNamedPipe(anyString(),anyInt(),anyInt())).thenReturn(WinPipesNativeHelper.INVALID_HANDLE);
        when(mockHelper.createNamedPipe(eq(PIPE_NAME),anyInt(),anyInt())).thenReturn(999L);
    }

    @Test
    public void canConstruct() {
        final WinPipe p1 = new WinPipe(mockHelper, PIPE_NAME);
        assertFalse(p1.isOpen());
        new WinPipe(mockHelper, PIPE_ID);
    }

    @Test
    public void testBadOpen() {
        try {
            final long badHandle = new WinPipe(mockHelper, "BADNAME").open();
            fail("Expected IOException");
        } catch (IOException ignored) {
        }
    }

    @Test
    public void testGoodOpen() throws IOException {
        new WinPipe(mockHelper, PIPE_NAME).open();
        verify(mockHelper).openNamedPipe(eq(PIPE_NAME));
    }

    @Test
    public void testCreatePipe() {
        final WinPipe p1 = new WinPipe(mockHelper, PIPE_NAME);
        assertFalse(p1.isOpen());
        final long goodHandle = p1.createWindowsNamedPipe(5, 7);
        assertEquals(999L, goodHandle);
        verify(mockHelper).createNamedPipe(eq(PIPE_NAME), eq(5), eq(7));
    }

    @Test
    public void testClose() throws IOException {
        final WinPipe p1 = new WinPipe(mockHelper, PIPE_NAME);
        assertFalse(p1.isOpen());
        long handle = p1.open();
        assertTrue(p1.isOpen());
        p1.close();
        assertFalse(p1.isOpen());
        verify(mockHelper).closeHandle(eq(handle));
    }

    @Test
    public void testRead() {
        final WinPipe p1 = new WinPipe(mockHelper, PIPE_NAME);
        final byte[] messageBytes = "Hello".getBytes(Charset.forName("UTF-8"));
        final ByteBuffer readBuff = ByteBuffer.allocate(messageBytes.length);
        when(mockHelper.readFile(anyInt(), any(ByteBuffer.class))).thenReturn(messageBytes.length);
        final int n1 = p1.read(readBuff);
        assertEquals(messageBytes.length, n1);
    }

    @Test
    public void testWrite() {
        final WinPipe p1 = new WinPipe(mockHelper, PIPE_NAME);
        final byte[] messageBytes = "Hello".getBytes(Charset.forName("UTF-8"));
        final ByteBuffer writeBuff = ByteBuffer.wrap(messageBytes);
        when(mockHelper.writeFile(anyInt(), any(ByteBuffer.class))).thenReturn(messageBytes.length);
        final int n1 = p1.write(writeBuff);
        assertEquals(messageBytes.length, n1);
    }

}

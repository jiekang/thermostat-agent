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

package com.redhat.thermostat.agent.ipc.winpipes.server.internal;

import com.redhat.thermostat.agent.ipc.winpipes.common.internal.WinPipesNativeHelper;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class WritePipeImplTest {

    private static final long GOOD_HANDLE = 999L;
    private static final int BUFSIZE = 4096;
    private static final String PIPE_NAME = "SomePipeName";

    private PipeManager mgrMock;
    private WinPipesNativeHelper nativeMock;
    private ClientHandler clientHandlerMock;

    @Before
    public void setup() {
        mgrMock = mock(PipeManager.class);
        clientHandlerMock = mock(ClientHandler.class);
        when(mgrMock.handleNewClientConnection()).thenReturn(clientHandlerMock);

        nativeMock = mock(WinPipesNativeHelper.class);
        try {
            when(nativeMock.connectNamedPipe(anyLong(), any(ByteBuffer.class))).thenReturn(WinPipesNativeHelper.ERROR_BROKEN_PIPE);
            when(nativeMock.connectNamedPipe(eq(GOOD_HANDLE), any(ByteBuffer.class))).thenReturn(WinPipesNativeHelper.ERROR_SUCCESS);
            when(nativeMock.createEvent()).thenReturn(88L);
            final ByteBuffer bb = ByteBuffer.allocate(BUFSIZE);
            bb.limit(0);
            when(nativeMock.createDirectBuffer(anyInt())).thenReturn(bb);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testCanConstruct() throws IOException {
        new WritePipeImpl(mgrMock, PIPE_NAME, GOOD_HANDLE, BUFSIZE, nativeMock);
    }

    @Test
    public void testWrite() throws IOException {
        final WritePipeImpl impl = new WritePipeImpl(mgrMock, PIPE_NAME, GOOD_HANDLE, BUFSIZE, nativeMock);
        ByteBuffer bb = ByteBuffer.wrap("SomeData".getBytes());
        impl.write(bb);
        impl.enqueueWrite();
        verify(nativeMock).writeFileOverlapped(anyLong(), any(ByteBuffer.class), any(ByteBuffer.class));
        assertEquals(impl.getWriteState(), WritePipeImpl.WritePipeState.WRITING_STATE);
    }

    @Test
    public void testEnqueueOperation() throws IOException {
        final WritePipeImpl impl = new WritePipeImpl(mgrMock, PIPE_NAME, GOOD_HANDLE, BUFSIZE, nativeMock);
        impl.enqueueWrite();
        verify(nativeMock, never()).writeFileOverlapped(anyLong(), any(ByteBuffer.class), any(ByteBuffer.class));
        assertEquals(impl.getWriteState(), WritePipeImpl.WritePipeState.QUIET_STATE);
    }

    @Test
    public void testHandlePendingOperation() throws IOException {
        final WritePipeImpl impl = new WritePipeImpl(mgrMock, PIPE_NAME, GOOD_HANDLE, BUFSIZE, nativeMock);
        impl.handlePendingWrite();
        verify(nativeMock, never()).getOverlappedResult(anyLong(), any(ByteBuffer.class), anyBoolean());
        assertEquals(impl.getWriteState(), WritePipeImpl.WritePipeState.QUIET_STATE);
    }

    @Test
    public void testClose() throws IOException {
        final WritePipeImpl impl = new WritePipeImpl(mgrMock, PIPE_NAME, GOOD_HANDLE, BUFSIZE, nativeMock);
        impl.close();
        assertEquals(impl.getWriteState(), WritePipeImpl.WritePipeState.CLOSED_STATE);
    }
}


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

import static com.redhat.thermostat.agent.ipc.winpipes.server.internal.ReadPipeImpl.ReadPipeState.CLOSED_STATE;
import static com.redhat.thermostat.agent.ipc.winpipes.server.internal.ReadPipeImpl.ReadPipeState.READING_STATE;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ReadPipeImplTest {

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
            when(nativeMock.createDirectBuffer(anyInt())).thenReturn(ByteBuffer.allocate(BUFSIZE));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testCanConstruct() throws IOException {
        new ReadPipeImpl(mgrMock, PIPE_NAME, GOOD_HANDLE, BUFSIZE, nativeMock);
    }

    @Test
    public void testGoodConnect() throws IOException {
        final ReadPipeImpl impl = new ReadPipeImpl(mgrMock, PIPE_NAME, GOOD_HANDLE, BUFSIZE, nativeMock);
        final boolean ret = impl.connectToNewClient();
        verify(nativeMock).connectNamedPipe(anyLong(), any(ByteBuffer.class));
        assertFalse(ret);
    }

    @Test
    public void testBadConnect() throws IOException {
        final ReadPipeImpl impl = new ReadPipeImpl(mgrMock, PIPE_NAME, 22, BUFSIZE, nativeMock);
        try {
            impl.connectToNewClient();
            fail();
        } catch (IOException e) {
            ;
        }
        verify(nativeMock).connectNamedPipe(anyLong(), any(ByteBuffer.class));
    }

    @Test
    public void testEnqueueOperation() throws IOException {
        final ReadPipeImpl impl = new ReadPipeImpl(mgrMock, PIPE_NAME, GOOD_HANDLE, BUFSIZE, nativeMock);
        when(nativeMock.readFileOverlapped(eq(GOOD_HANDLE), any(ByteBuffer.class), any(ByteBuffer.class))).thenReturn(true);
        impl.enqueueRead();
        verify(mgrMock, never()).resetPipe();
    }

    @Test
    public void testBadEnqueueOperation() throws IOException {
        final ReadPipeImpl impl = new ReadPipeImpl(mgrMock, PIPE_NAME, GOOD_HANDLE, BUFSIZE, nativeMock);
        when(nativeMock.readFileOverlapped(eq(GOOD_HANDLE), any(ByteBuffer.class), any(ByteBuffer.class))).thenReturn(false);
        when(nativeMock.getLastError()).thenReturn(WinPipesNativeHelper.ERROR_BROKEN_PIPE);
        impl.enqueueRead();
        verify(mgrMock).resetPipe();
    }

    @Test
    public void testHandlePendingOperation() throws IOException {
        final ReadPipeImpl impl = new ReadPipeImpl(mgrMock, PIPE_NAME, GOOD_HANDLE, BUFSIZE, nativeMock);
        final boolean ret = impl.connectToNewClient(); // force READING_STATE
        impl.handlePendingRead();
        verify(mgrMock, never()).resetPipe();
        assertEquals(impl.getReadState(), READING_STATE);
    }

    @Test
    public void testClose() throws IOException {
        final ReadPipeImpl impl = new ReadPipeImpl(mgrMock, PIPE_NAME, GOOD_HANDLE, BUFSIZE, nativeMock);
        final boolean ret = impl.connectToNewClient();
        impl.close();
        assertEquals(impl.getReadState(), CLOSED_STATE);
    }
}

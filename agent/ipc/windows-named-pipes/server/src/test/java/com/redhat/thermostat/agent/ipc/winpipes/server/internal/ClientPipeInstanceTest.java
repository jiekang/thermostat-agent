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

import com.redhat.thermostat.agent.ipc.server.ThermostatIPCCallbacks;
import com.redhat.thermostat.agent.ipc.winpipes.common.internal.WinPipesNativeHelper;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ClientPipeInstanceTest {

    private final String PIPE_NAME = "SomePipeName";

    private ClientPipeInstance cpiMock;
    private ClientPipeInstance.ClientPipeInstanceHelper helperMock;
    private ExecutorService execMock;
    private ThermostatIPCCallbacks cbMock;
    private ReadPipeImpl readerMock;
    private WritePipeImpl writerMock;

    @Before
    public void setup() {
        helperMock = mock(ClientPipeInstance.ClientPipeInstanceHelper.class);
        when(helperMock.createNamedPipe(anyString(), anyInt(), anyInt())).thenReturn(WinPipesNativeHelper.INVALID_HANDLE);
        when(helperMock.createNamedPipe(eq(PIPE_NAME), anyInt(), anyInt())).thenReturn(999L);
        when(helperMock.disconnectNamedPipe(anyInt())).thenReturn(true);
        readerMock = mock(ReadPipeImpl.class);
        writerMock = mock(WritePipeImpl.class);
        try {
            when(helperMock.createPipeReader(any(ClientPipeInstance.class), eq(PIPE_NAME), anyLong(), anyInt())).thenReturn(readerMock);
            when(helperMock.createPipeWriter(any(ClientPipeInstance.class), eq(PIPE_NAME), anyLong(), anyInt())).thenReturn(writerMock);
        } catch (IOException e) {
            fail();
        }
        execMock = mock(ExecutorService.class);
        cbMock = mock(ThermostatIPCCallbacks.class);
    }

    @Test
    public void testCanConstruct() {
        ClientPipeInstance cpi;
        try {
            cpi = new ClientPipeInstance(PIPE_NAME, 5, 4096, execMock, cbMock, helperMock);
        } catch (IOException e) {
            fail();
            return;
        }
        assertEquals(readerMock, cpi.getReadHandler());
        assertEquals(writerMock, cpi.getWriteHandler());
    }

    @Test
    public void testConnectNewClient() throws IOException {
        ClientPipeInstance cpi = new ClientPipeInstance(PIPE_NAME, 5, 4096, execMock, cbMock, helperMock);
        when(readerMock.getReadState()).thenReturn(ReadPipeImpl.ReadPipeState.READING_STATE);
        cpi.connectToNewClient();
        assertTrue(cpi.isOpen());
    }

    @Test
    public void testClose() throws IOException {
        ClientPipeInstance cpi = new ClientPipeInstance(PIPE_NAME, 5, 4096, execMock, cbMock, helperMock);
        when(readerMock.getReadState()).thenReturn(ReadPipeImpl.ReadPipeState.READING_STATE);
        cpi.connectToNewClient();
        cpi.close();
        assertFalse(cpi.isOpen());
    }

    @Test
    public void testReset() throws IOException {
        ClientPipeInstance cpi = new ClientPipeInstance(PIPE_NAME, 5, 4096, execMock, cbMock, helperMock);
        when(readerMock.getReadState()).thenReturn(ReadPipeImpl.ReadPipeState.READING_STATE);
        cpi.connectToNewClient();
        cpi.resetPipe();
        assertTrue(cpi.isOpen());
    }

}

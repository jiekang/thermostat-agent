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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

import com.redhat.thermostat.agent.ipc.winpipes.common.internal.WinPipe;
import org.junit.Before;
import org.junit.Test;

import com.redhat.thermostat.agent.ipc.server.ThermostatIPCCallbacks;

public class AcceptThreadTest {

    private final String PIPE_NAME = "SomePipeName";
    private final static int BUFSIZE = 4096;

    private ExecutorService execMock;
    private WinPipesServerChannelImpl channelMock;
    private WinPipe pipeMock;
    private ClientPipeInstance clientMock;
    private ThermostatIPCCallbacks callbacks;
    private AcceptThread thread;
    private AcceptThread.TestHelper atMock;
    private WindowsEventSelector selectorMock;
    private WindowsEventSelector.EventHandler ehMock;

    @Before
    public void setUp() throws IOException {

        channelMock = mock(WinPipesServerChannelImpl.class);

        clientMock = mock(ClientPipeInstance.class);
        when(clientMock.connectToNewClient()).thenReturn(true);
        when(clientMock.getName()).thenReturn(PIPE_NAME);

        callbacks = mock(ThermostatIPCCallbacks.class);
        when(channelMock.getCallbacks()).thenReturn(callbacks);

        execMock = mock(ExecutorService.class);
        pipeMock = mock(WinPipe.class);
        when(pipeMock.getPipeName()).thenReturn(PIPE_NAME);
        selectorMock = mock(WindowsEventSelector.class);
        atMock = mock(AcceptThread.TestHelper.class);
        when(atMock.createPipeInstance(eq(PIPE_NAME), eq(1), eq(BUFSIZE), eq(execMock), eq(callbacks))).thenReturn(clientMock);

        ehMock = mock(WindowsEventSelector.EventHandler.class);
    }

    @Test
    public void canCreate() {
        final AcceptThread at = new AcceptThread(channelMock, execMock, 1, pipeMock, selectorMock, atMock);
        assertFalse(at.isShutdown());
    }

    @Test
    public void testCreateInstances() {
        final AcceptThread at = new AcceptThread(channelMock, execMock, 1, pipeMock, selectorMock, atMock);
        try {
            when(atMock.createPipeInstance(anyString(), anyInt(), anyInt(), eq(execMock), eq(callbacks))).thenReturn(clientMock);
        } catch (IOException e) {
            fail();
        }
        try {
            at.createInstances();
        } catch (IOException e) {
            fail();
        }
        try {
            verify(clientMock).connectToNewClient();
        } catch (IOException e) {
            fail();
        }
    }

    @Test
    public void testRun() {
        final AcceptThread at = new AcceptThread(channelMock, execMock, 1, pipeMock, selectorMock, atMock);
        try {
            when(atMock.createPipeInstance(anyString(), anyInt(), anyInt(), eq(execMock), eq(callbacks))).thenReturn(clientMock);
        } catch (IOException e) {
            fail();
        }
        try {
            when(selectorMock.waitForEvent()).thenReturn(ehMock);
        } catch (IOException e) {
            fail();
        }

        Thread testThread = new Thread() {
            public void run() {
                at.run();
            }
        };
        testThread.start();
        try {
            Thread.sleep(1);
            at.shutdown();
        } catch (InterruptedException e) {
            fail();
        } catch (IOException e) {
            fail();
        }

    }

    @Test
    public void testShutdown() {
        final AcceptThread at = new AcceptThread(channelMock, execMock, 1, pipeMock, selectorMock, atMock);
        try {
            at.shutdown();
        } catch (IOException e) {
            fail();
        }
        assertTrue(at.isShutdown());
    }
}


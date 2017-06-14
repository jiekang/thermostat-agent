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

package com.redhat.thermostat.commands.agent.internal.socket;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Matchers.eq;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;

import org.eclipse.jetty.util.BufferUtil;
import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.extensions.Frame;
import org.eclipse.jetty.websocket.api.extensions.Frame.Type;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.google.gson.Gson;
import com.redhat.thermostat.commands.agent.internal.socket.CmdChannelAgentSocket.OnMessageCallBack;
import com.redhat.thermostat.commands.model.Message;

public class CmdChannelAgentSocketTest {

    @Test(timeout = 2000)
    public void connectReleasesConnectLatch() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        CmdChannelAgentSocket socket = new CmdChannelAgentSocket(null, latch, "foo-agent");
        socket.onConnect(mock(Session.class)); // should release latch
        latch.await();
    }
    
    @Test
    public void canHandlePings() throws InterruptedException, IOException {
        CountDownLatch connectLatch = new CountDownLatch(1);
        Session session = mock(Session.class);
        RemoteEndpoint remoteEndpoint = mock(RemoteEndpoint.class);
        when(session.getRemote()).thenReturn(remoteEndpoint);
        CmdChannelAgentSocket socket = new CmdChannelAgentSocket(null, connectLatch, "foo-agent");
        socket.onConnect(session);
        connectLatch.await();
        ByteBuffer pingPayload = ByteBuffer.wrap("ping".getBytes());
        Frame pingFrame = mock(Frame.class);
        when(pingFrame.getType()).thenReturn(Type.PING);
        when(pingFrame.getPayload()).thenReturn(pingPayload.slice());
        when(pingFrame.hasPayload()).thenReturn(true);
        socket.onFrame(pingFrame);
        ArgumentCaptor<ByteBuffer> payloadCaptor = ArgumentCaptor.forClass(ByteBuffer.class);
        verify(remoteEndpoint).sendPong(payloadCaptor.capture());
        ByteBuffer pongPayload = payloadCaptor.getValue();
        String actual = BufferUtil.toUTF8String(pongPayload);
        assertEquals("ping", actual);
    }
    
    @Test
    public void callsCallBackOnStringMessage() {
        final GsonFacade gsonFacade = mock(GsonFacade.class);
        final Gson myGson = new Gson();
        when(gsonFacade.toGson()).thenReturn(myGson);
        final Session mySession = mock(Session.class);
        final boolean[] hasRun = new boolean[1];
        CmdChannelAgentSocket socket = new CmdChannelAgentSocket(new OnMessageCallBack() {
            
            @Override
            public void run(Session session, Message msg, Gson gson) {
                assertSame(mySession, session);
                assertSame(myGson, gson);
                hasRun[0] = true;
            }
        }, (CountDownLatch)null, "some-agent", gsonFacade);
        
        String strMessage = "{ \"foo\": \"bar\" }";
        socket.onMessage(mySession, strMessage);
        verify(gsonFacade).fromJson(eq(strMessage), eq(Message.class));
        assertTrue(hasRun[0]);
    }
    
    @Test
    public void verifyCloseSession() throws InterruptedException {
        CountDownLatch connectLatch = new CountDownLatch(1);
        Session session = mock(Session.class);
        CmdChannelAgentSocket socket = new CmdChannelAgentSocket(null, connectLatch, "foo-agent");
        socket.onConnect(session);
        connectLatch.await();
        socket.closeSession();
        verify(session).close();
    }
}

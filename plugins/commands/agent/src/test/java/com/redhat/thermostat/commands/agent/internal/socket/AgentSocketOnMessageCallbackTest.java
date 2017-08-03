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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Matchers.eq;

import java.io.IOException;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;

import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.redhat.thermostat.commands.agent.internal.receiver.PingReceiver;
import com.redhat.thermostat.commands.agent.internal.socket.AgentSocketOnMessageCallback.CmdChannelRequestHandler;
import com.redhat.thermostat.commands.agent.internal.typeadapters.MessageTypeAdapterFactory;
import com.redhat.thermostat.commands.agent.receiver.ReceiverRegistry;
import com.redhat.thermostat.commands.agent.receiver.RequestReceiver;
import com.redhat.thermostat.commands.model.AgentRequest;
import com.redhat.thermostat.commands.model.ClientRequest;
import com.redhat.thermostat.commands.model.WebSocketResponse;
import com.redhat.thermostat.commands.model.WebSocketResponse.ResponseType;
import com.redhat.thermostat.shared.config.InvalidConfigurationException;

public class AgentSocketOnMessageCallbackTest {

    private Gson gson;
    
    @Before
    public void setup() {
        gson = new GsonBuilder().registerTypeAdapterFactory(new MessageTypeAdapterFactory()).create();
    }
    
    /**
     * Request handling is asynchronous, thus the test synchronizes using a CountDownLatch.
     * It cannot make assumptions on what is being sent on the session, though, as this is
     * being handled after the receiver did it's work.
     * 
     * @throws InterruptedException
     */
    @Test
    public void handlesAgentRequestsProperly() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final String jvmId = "jvm_id";
        final String systemId = "system_id";
        final long sequenceId = 333L;
        final String actionName = "foo-action";
        ReceiverRegistry reg = mock(ReceiverRegistry.class);
        RequestReceiver receiver = new PingReceiver() {
            
            @Override
            public WebSocketResponse receive(AgentRequest request) {
                WebSocketResponse resp = super.receive(request);
                assertEquals(jvmId, request.getJvmId());
                assertEquals(systemId, request.getSystemId());
                assertEquals(sequenceId, request.getSequenceId());
                latch.countDown();
                return resp;
            }
            
        };
        when(reg.getReceiver(eq(actionName))).thenReturn(receiver);
        AgentSocketOnMessageCallback cb = new AgentSocketOnMessageCallback(reg);
        Session session = mock(Session.class);
        when(session.getRemote()).thenReturn(mock(RemoteEndpoint.class)); // Prevent spurious NPEs
        SortedMap<String, String> params = new TreeMap<>();
        AgentRequest agentRequest = new AgentRequest(333L, actionName, systemId, jvmId, params);
        
        // Main method under test
        cb.run(session, agentRequest, gson);
        
        // wait for request to be handled. Assertions are done in the
        // receiver.
        latch.await();
    }
    
    @Test
    public void handlerThreadSendsResponseToSession() throws InterruptedException, IOException {
        ArgumentCaptor<String> jsonCaptor = ArgumentCaptor.forClass(String.class);
        final CountDownLatch receiverHandled = new CountDownLatch(1);
        final CountDownLatch sentLatch = new CountDownLatch(1);
        final String actionName = "ping"; // must be ping otherwise receiver will return ERROR
        ReceiverRegistry reg = mock(ReceiverRegistry.class);
        RequestReceiver receiver = new PingReceiver() {
            
            @Override
            public WebSocketResponse receive(AgentRequest request) {
                WebSocketResponse resp = super.receive(request);
                receiverHandled.countDown();
                return resp;
            }
            
        };
        when(reg.getReceiver(eq(actionName))).thenReturn(receiver);
        Session session = mock(Session.class);
        RemoteEndpoint mockEndpoint = mock(RemoteEndpoint.class);
        when(session.getRemote()).thenReturn(mockEndpoint);
        SortedMap<String, String> params = new TreeMap<>();
        AgentRequest agentRequest = new AgentRequest(344L, actionName, "system_id", "jvm_id", params);
        
        CmdChannelRequestHandler handler = new CmdChannelRequestHandler(session, agentRequest, reg, gson, sentLatch);
        handler.start(); // start asynchronously
        
        receiverHandled.await(); // wait for receiver to handle request
        sentLatch.await(); // wait for sending to actually happen
        verify(mockEndpoint).sendString(jsonCaptor.capture());
        String json = jsonCaptor.getValue();
        String expected = "{\"type\":100,\"sequence\":344,\"payload\":{\"respType\":\"OK\"}}";
        assertEquals(expected, json);
    }
    
    /**
     * A "receiver=<name>" argument is expected at a bare minimum. If no receiver name is
     * specified an error response is expected to be sent to the client.
     * 
     * @throws InterruptedException
     * @throws IOException
     */
    @Test
    public void noReceiverSendsErrorResponseToSession() throws InterruptedException, IOException {
        ArgumentCaptor<String> jsonCaptor = ArgumentCaptor.forClass(String.class);
        CountDownLatch sentLatch = new CountDownLatch(1);
        Session session = mock(Session.class);
        RemoteEndpoint mockEndpoint = mock(RemoteEndpoint.class);
        when(session.getRemote()).thenReturn(mockEndpoint);
        SortedMap<String, String> emptyParams = new TreeMap<>();
        AgentRequest agentRequest = new AgentRequest(888L, "not-exist", "system_id", "jvm_id", emptyParams);
        
        CmdChannelRequestHandler handler = new CmdChannelRequestHandler(session, agentRequest, mock(ReceiverRegistry.class), gson, sentLatch);
        handler.start(); // start asynchronously
        
        sentLatch.await(); // wait for sending to actually happen
        verify(mockEndpoint).sendString(jsonCaptor.capture());
        String json = jsonCaptor.getValue();
        String expected = "{\"type\":100,\"sequence\":888,\"payload\":{\"respType\":\"ERROR\"}}";
        assertEquals(expected, json);
    }
    
    /**
     * When the connect to the endpoint fails due to authentication/authorization issues a
     * WebSocketResponse is being sent back. This case needs to be handled.
     */
    @Test(expected = InvalidConfigurationException.class)
    public void handlesAuthFailResponsesProperly() {
        WebSocketResponse response = new WebSocketResponse(WebSocketResponse.UNKNOWN_SEQUENCE, ResponseType.AUTH_FAIL);
        AgentSocketOnMessageCallback cb = new AgentSocketOnMessageCallback(mock(ReceiverRegistry.class));
        cb.run(null, response, gson); // throws exception
    }
    
    /**
     * There are other message types the agent endpoint is not expected to receive directly.
     * They are for client->gateway interactions or something else entirely.
     */
    @Test(expected = IllegalStateException.class)
    public void unexpectedMessageTypesThrowException() {
        ClientRequest request = new ClientRequest(212);
        AgentSocketOnMessageCallback cb = new AgentSocketOnMessageCallback(mock(ReceiverRegistry.class));
        cb.run(null, request, gson); // throws exception
    }
}

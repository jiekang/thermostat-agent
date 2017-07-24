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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jetty.util.BufferUtil;
import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketFrame;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.api.extensions.Frame;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.redhat.thermostat.commands.agent.internal.typeadapters.MessageTypeAdapterFactory;
import com.redhat.thermostat.commands.model.Message;
import com.redhat.thermostat.common.utils.LoggingUtils;

/**
 * Handles the agent connections to/from the commands microservice.
 */
@WebSocket
public class CmdChannelAgentSocket {

    private static final long SOCKET_SESSION_IDLE_TIMEOUT = TimeUnit.MINUTES.toMillis(10);
    private static final Logger logger = LoggingUtils
            .getLogger(CmdChannelAgentSocket.class);
    private final GsonFacade gson;
    private final CountDownLatch connectLatch;
    private final OnMessageCallBack onMessage;
    private final String agentId;
    private Session session;

    public CmdChannelAgentSocket(OnMessageCallBack onMessage, CountDownLatch connect, String agentId) {
        this(onMessage, connect, agentId,
                new GsonFacadeImpl(new GsonBuilder()
                .registerTypeAdapterFactory(new MessageTypeAdapterFactory())
                .serializeNulls()
                .create()));
    }
    
    // for testing purposes
    CmdChannelAgentSocket(OnMessageCallBack onMessage, CountDownLatch connect, String agentId, GsonFacade gson) {
        this.onMessage = onMessage;
        this.connectLatch = connect;
        this.agentId = agentId;
        this.gson = gson;
    }

    @OnWebSocketFrame
    public void onFrame(Frame frame) {
        switch (frame.getType()) {
        case PONG:
            handlePong(frame.getPayload());
            break;
        case PING:
            ByteBuffer payload = null;
            if (frame.hasPayload()) {
                payload = frame.getPayload();
            }
            handlePing(payload);
            break;
        default:
            // nothing to do
        }
    }

    private void handlePing(ByteBuffer payload) {
        logger.fine("Handling WebSocket ping for " + agentId);
        try {
            synchronized (session) {
                RemoteEndpoint endPoint = session.getRemote();
                endPoint.sendPong(payload);
            }
        } catch (IOException e) {
            logger.warning("Failed to send pong response!");
            logger.log(Level.FINE, "failed to send pong response", e);
        }
    }

    private void handlePong(ByteBuffer payload) {
        String pongMsg = BufferUtil.toUTF8String(payload);
        logger.fine("Got pong from peer: " + pongMsg);
    }

    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        logger.fine(
                "Connection closed. code=" + statusCode + ", reason=" + reason);
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        this.session = session;
        // Be sure to have the socket timeout the same as on the
        // microservice endpoint. Otherwise the agent socket might
        // time out where it should not since the microservice will
        // send a ping periodically which is strictly less than
        // the configured idle timeout.
        this.session.setIdleTimeout(SOCKET_SESSION_IDLE_TIMEOUT);
        logger.config("Socket session idle timeout: " + session.getIdleTimeout() + "ms");
        this.connectLatch.countDown();
    }

    @OnWebSocketError
    public void onError(Throwable cause) {
        Throwable realCause = cause.getCause();
        while (realCause != null) {
            logger.log(Level.WARNING, realCause.getMessage());
            logger.log(Level.FINE, realCause.getMessage(), realCause);
            realCause = realCause.getCause();
        }
    }

    @OnWebSocketMessage
    public void onMessage(final Session session, final String msg) {
        final Message message = gson.fromJson(msg, Message.class);
        onMessage.run(session, message, gson.toGson());
    }

    public void closeSession() {
        if (session != null) {
            this.session.close();
        }
    }

    public void sendPingToServer(String msgPayload) throws IOException {
        if (this.session == null) {
            throw new NullPointerException(
                    "Session null. Agent not connected?");
        }
        synchronized (session) {
            RemoteEndpoint endpoint = session.getRemote();
            ByteBuffer pingPayload = ByteBuffer.wrap(msgPayload.getBytes());
            endpoint.sendPing(pingPayload);
        }
        logger.fine("Client: Ping msg sent <<" + msgPayload + ">>");
    }

    public interface OnMessageCallBack {
        public void run(Session session, Message msg, Gson gson);
    }
}

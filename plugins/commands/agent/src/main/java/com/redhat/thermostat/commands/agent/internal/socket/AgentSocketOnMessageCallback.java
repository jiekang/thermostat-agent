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
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;

import com.google.gson.Gson;
import com.redhat.thermostat.commands.agent.internal.socket.CmdChannelAgentSocket.OnMessageCallBack;
import com.redhat.thermostat.commands.agent.receiver.ReceiverRegistry;
import com.redhat.thermostat.commands.agent.receiver.RequestReceiver;
import com.redhat.thermostat.commands.model.AgentRequest;
import com.redhat.thermostat.commands.model.Message;
import com.redhat.thermostat.commands.model.WebSocketResponse;
import com.redhat.thermostat.commands.model.WebSocketResponse.ResponseType;
import com.redhat.thermostat.common.utils.LoggingUtils;
import com.redhat.thermostat.shared.config.InvalidConfigurationException;

public class AgentSocketOnMessageCallback implements OnMessageCallBack {

    private final ReceiverRegistry receivers;

    public AgentSocketOnMessageCallback(ReceiverRegistry receivers) {
        this.receivers = receivers;
    }

    @Override
    public void run(Session session, Message msg, Gson gson) {
        switch (msg.getMessageType()) {
        case AGENT_REQUEST:
            AgentRequest request = (AgentRequest) msg;
            Thread handler = new CmdChannelRequestHandler(session, request, receivers, gson);
            handler.start();
            break;
        case RESPONSE:
            // Auth-failed
            WebSocketResponse resp = (WebSocketResponse) msg;
            throw new InvalidConfigurationException(
                    "Failed to connect web socket. Reason: "
                            + resp.getResponseType());
        default:
            throw new IllegalStateException(
                    "Unexpected message type: " + msg.getMessageType());
        }
    }

    static class CmdChannelRequestHandler extends Thread {

        private static final Logger logger = LoggingUtils.getLogger(CmdChannelRequestHandler.class);
        private final Session session;
        private final AgentRequest request;
        private final ReceiverRegistry receivers;
        private final Gson gson;
        private final CountDownLatch sentLatch;

        CmdChannelRequestHandler(Session session, AgentRequest request, ReceiverRegistry receivers, Gson gson) {
            this(session, request, receivers, gson, null);
        }
        
        CmdChannelRequestHandler(Session session, AgentRequest request, ReceiverRegistry receivers, Gson gson, CountDownLatch sentLatch) {
            super("Thermostat-WS-CMD-CH-Handler");
            this.session = session;
            this.request = request;
            this.receivers = receivers;
            this.gson = gson;
            this.sentLatch = sentLatch;
        }

        @Override
        public void run() {
            String receiverName = request.getAction();
            RequestReceiver receiver = receivers.getReceiver(receiverName);
            if (receiver == null) {
                String msg = "Got cmd-channel request for receiver '"
                        + receiverName
                        + "' which is not registered. Request is being ignored.";
                handleError(msg);
                return;
            }
            logger.info("Handling cmd-channel request (sequence="
                    + request.getSequenceId() + ", receiver=" + receiverName
                    + ")");
            WebSocketResponse response = receiver.receive(request);
            sendResponse(response);
            logger.info("Handling request (sequence=" + request.getSequenceId()
                    + ", receiver=" + receiverName + ") completed.");
        }

        private void handleError(String msg) {
            logger.warning(msg);
            sendResponse(new WebSocketResponse(request.getSequenceId(),
                    ResponseType.ERROR));
        }

        private void sendResponse(WebSocketResponse response) {
            try {
                synchronized (session) {
                    RemoteEndpoint endpoint = session.getRemote();
                    endpoint.sendString(gson.toJson(response));
                }
                if (sentLatch != null) {
                    sentLatch.countDown(); // synchronizes for tests
                }
            } catch (IOException e) {
                logger.warning(
                        "Failed to send response to microservice endpoint. Reason: "
                                + e.getMessage());
                logger.log(Level.FINE,
                        "Failed to send response to microservice endpoint.", e);
            }
        }
    }

}

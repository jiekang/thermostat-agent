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

package com.redhat.thermostat.commands.agent.internal;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.osgi.framework.BundleContext;

import com.redhat.thermostat.backend.Backend;
import com.redhat.thermostat.backend.BaseBackend;
import com.redhat.thermostat.commands.agent.internal.socket.AgentSocketOnMessageCallback;
import com.redhat.thermostat.commands.agent.internal.socket.CmdChannelAgentSocket;
import com.redhat.thermostat.commands.agent.receiver.ReceiverRegistry;
import com.redhat.thermostat.common.Ordered;
import com.redhat.thermostat.common.Version;
import com.redhat.thermostat.common.config.experimental.ConfigurationInfoSource;
import com.redhat.thermostat.common.plugin.PluginConfiguration;
import com.redhat.thermostat.common.utils.LoggingUtils;
import com.redhat.thermostat.shared.config.CommonPaths;
import com.redhat.thermostat.storage.config.FileStorageCredentials;
import com.redhat.thermostat.storage.core.StorageCredentials;
import com.redhat.thermostat.storage.core.WriterID;

@Component
@Service(value = Backend.class)
public class CommandsBackend extends BaseBackend {

    private static final Logger logger = LoggingUtils.getLogger(CommandsBackend.class);
    private static final String NAME = "Commands (cmd-channel) Backend";
    private static final String DESCRIPTION = "Establishes web-socket connections to the microservice endpoint so as to be able to receive command requests";
    private static final String VENDOR = "Red Hat Inc.";
    private static final String PLUGIN_ID = "commands";
    private static final String ENDPOINT_FORMAT = "%s/systems/%s/agents/%s";
    private static final String UNKNOWN_CREDS = "UNKNOWN:UNKNOWN";

    private final WsClientCreator wsClientCreator;
    private final CredentialsCreator credsCreator;
    private final ConfigCreator configCreator;
    private final CountDownLatch socketConnectLatch;
    private WebSocketClientFacade wsClient;
    private boolean isActive;
    private StorageCredentials creds;
    private PluginConfiguration config;
    private ReceiverRegistry receiverReg;

    @Reference
    private WriterID agentId;

    @Reference
    private CommonPaths paths;

    @Reference
    private ConfigurationInfoSource commandInfo;

    public CommandsBackend() {
        this(new WsClientCreator(), new CredentialsCreator(), new ConfigCreator(), new CountDownLatch(1));
    }
    
    // For testing purposes
    CommandsBackend(WsClientCreator creator,
                    CredentialsCreator credsCreator,
                    ConfigCreator configCreator,
                    CountDownLatch socketConnectLatch) {
        super(NAME, DESCRIPTION, VENDOR);
        this.wsClientCreator = creator;
        this.credsCreator = credsCreator;
        this.configCreator = configCreator;
        this.socketConnectLatch = socketConnectLatch;
    }

    @Override
    public boolean activate() {
        if (!isActive) {
            // sets wsSocket and socket instance variables
            isActive = connectWsClient();
        }
        return isActive;
    }

    @Override
    public boolean deactivate() {
        if (!isActive) {
            // nothing to do
            return true;
        }
        if (wsClient != null) {
            wsClient.stop();
        }
        isActive = false;
        return true;
    }

    @Override
    public boolean isActive() {
        return isActive;
    }

    @Override
    public int getOrderValue() {
        return Ordered.ORDER_FIRST + 32;
    }

    @Activate
    protected void componentActivated(BundleContext ctx) {
        Version version = new Version(ctx.getBundle());
        super.setVersion(version.getVersionInfo());
        creds = credsCreator.create(paths);
        config = configCreator.createConfig(commandInfo);
        try {
            wsClient = wsClientCreator.createClient();
            wsClient.start();
        } catch (Exception e) {
            logger.log(Level.WARNING,
                    "Failed to start websocket client. Reason: "
                            + e.getMessage());
        }
        receiverReg = new ReceiverRegistry(ctx);
    }

    @Deactivate
    protected void noOp() {
        /*
         * Map unused DS deactivate method to this NOOP method to prevent it
         * from trying to use Backend.activate/deactivate and giving an error
         * about them being incompatible.
         */
    }

    /**
     * 
     * @return {@code true} if and only if connection was successfully made
     */
    private boolean connectWsClient() {
        boolean expired = false;
        try {
            String microserviceURL = config.getGatewayURL();
            // String agent = agentId.getWriterID();
            // FIXME: Use reasonable agent/system name to register, not
            // hard-coded one
            // Unfortunately, the microservice is currently set up only for
            // a hard-coded one.
            String agent = "testAgent";
            URI agentUri = new URI(String.format(ENDPOINT_FORMAT, microserviceURL, "ignoreMe", agent));
            AgentSocketOnMessageCallback onMsgCallback = new AgentSocketOnMessageCallback(receiverReg);
            CmdChannelAgentSocket agentSocket = new CmdChannelAgentSocket(
                    onMsgCallback, socketConnectLatch, agent);
            ClientUpgradeRequest agentRequest = new ClientUpgradeRequest();
            agentRequest.setHeader(HttpHeader.AUTHORIZATION.asString(),
                    getBasicAuthHeaderValue());
            wsClient.connect(agentSocket, agentUri, agentRequest);
            logger.fine("WebSocket connect initiated.");
            expired = !socketConnectLatch.await(10, TimeUnit.SECONDS);
        } catch (IOException | URISyntaxException | InterruptedException e) {
            logger.warning("Failed to connect to endpoint. Reason: " + e.getMessage());
            logger.log(Level.FINE, "Failed to connect to endpoint", e);
            return false;
        }
        if (expired) {
            logger.warning("Did not receive connect event from endpoint");
            return false;
        } else {
            logger.fine("Successfully connected agent web socket");
            return true;
        }
    }

    String getBasicAuthHeaderValue() {
        String username = creds.getUsername();
        char[] pwdChar = creds.getPassword();
        String userpassword;
        if (username == null || username.isEmpty() || pwdChar == null) {
            logger.warning("No credentials specified in " + paths.getUserAgentAuthConfigFile() + ". The connection will fail.");
            userpassword = UNKNOWN_CREDS;
        } else {
            String pwd = new String(pwdChar);
            userpassword = username + ":" + pwd;
        }
        
        @SuppressWarnings("restriction")
        String encodedAuthorization = new sun.misc.BASE64Encoder()
                .encode(userpassword.getBytes());
        return "Basic " + encodedAuthorization;
    }
    
    protected void bindPaths(CommonPaths paths) {
        this.paths = paths;
    }
    
    static class WsClientCreator {
        WebSocketClientFacade createClient() {
            return new WebSocketClientFacadeImpl();
        }
    }
    
    static class CredentialsCreator {
        StorageCredentials create(CommonPaths paths) {
            return new FileStorageCredentials(paths.getUserAgentAuthConfigFile());
        }
    }
    
    static class ConfigCreator {
        PluginConfiguration createConfig(ConfigurationInfoSource source) {
            return new PluginConfiguration(source, PLUGIN_ID);
        }
    }
}

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.CountDownLatch;

import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Version;

import com.redhat.thermostat.commands.agent.internal.CommandsBackend.ConfigCreator;
import com.redhat.thermostat.commands.agent.internal.CommandsBackend.CredentialsCreator;
import com.redhat.thermostat.commands.agent.internal.CommandsBackend.WsClientCreator;
import com.redhat.thermostat.commands.agent.internal.socket.CmdChannelAgentSocket;
import com.redhat.thermostat.common.config.experimental.ConfigurationInfoSource;
import com.redhat.thermostat.common.plugins.PluginConfiguration;
import com.redhat.thermostat.shared.config.CommonPaths;
import com.redhat.thermostat.storage.core.StorageCredentials;

public class CommandsBackendTest {

    private static final String GW_URL = "ws://example.com/commands/v1";
    private CommandsBackend backend;
    private StorageCredentials creds;
    private BundleContext bundleContext;
    private WebSocketClientFacade client;
    private CountDownLatch socketConnect;
    
    @Before
    public void setup() throws IOException {
        socketConnect = new CountDownLatch(1);
        creds = mock(StorageCredentials.class);
        CredentialsCreator credsCreator = mock(CredentialsCreator.class);
        when(credsCreator.create(any(CommonPaths.class))).thenReturn(creds);
        WsClientCreator creator = mock(WsClientCreator.class);
        client = mock(WebSocketClientFacade.class);
        when(creator.createClient()).thenReturn(client);
        ConfigCreator configCreator = mock(ConfigCreator.class);
        PluginConfiguration config = mock(PluginConfiguration.class);
        when(config.getGatewayURL()).thenReturn(GW_URL);
        when(configCreator.createConfig(any(ConfigurationInfoSource.class))).thenReturn(config);
        backend = new CommandsBackend(creator, credsCreator, configCreator, socketConnect);
        backend.bindPaths(mock(CommonPaths.class));
        bundleContext = mock(BundleContext.class);
        Bundle bundle = mock(Bundle.class);
        when(bundle.getVersion()).thenReturn(mock(Version.class));
        when(bundleContext.getBundle()).thenReturn(bundle);
        backend.componentActivated(bundleContext);
    }
    
    @Test
    public void testGetBasicAuthHeaderValueNoCreds() {
        doUnknownCredsTest(backend);
    }
    
    @Test
    public void testGetBasicAuthHeaderValueWithNoUsername() {
        when(creds.getPassword()).thenReturn(new char[] { 'a', 'b', 'c' });
        doUnknownCredsTest(backend);
    }
    
    @Test
    public void testGetBasicAuthHeaderValueWithNoPassword() {
        when(creds.getUsername()).thenReturn("foo-user");
        doUnknownCredsTest(backend);
    }
    
    @Test
    public void canGetBasicAuthHeaderValueWithUsernamePwd() {
        String password = "foo";
        String username = "foo-user";
        when(creds.getPassword()).thenReturn(password.toCharArray());
        when(creds.getUsername()).thenReturn(username);
        String expected = base64EncodedHeader(username + ":" + password);
        assertEquals(expected, backend.getBasicAuthHeaderValue());
    }
    
    @Test
    public void testComponentActivated() {
        // setup invokes it. only do verification here
        verify(client).start();
    }
    
    @Test
    public void testActivateSuccess() throws IOException {
        String password = "foo";
        String username = "foo-user";
        when(creds.getPassword()).thenReturn(password.toCharArray());
        when(creds.getUsername()).thenReturn(username);
        ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
        ArgumentCaptor<ClientUpgradeRequest> reqCaptor = ArgumentCaptor.forClass(ClientUpgradeRequest.class);
        // release connect latch
        socketConnect.countDown();
        boolean success = backend.activate();
        verify(client).connect(any(CmdChannelAgentSocket.class), uriCaptor.capture(), reqCaptor.capture());
        assertTrue("Expected successful activation", success);
        URI uri = uriCaptor.getValue();
        String expectedURI = GW_URL + "/systems/ignoreMe/agents/testAgent";
        assertEquals(expectedURI, uri.toString());
        ClientUpgradeRequest req = reqCaptor.getValue();
        String expectedHeader = base64EncodedHeader(username + ":" + password);
        String actualHeader = req.getHeader(HttpHeader.AUTHORIZATION.asString());
        assertEquals(expectedHeader, actualHeader);
        assertTrue("Expected backend to be active", backend.isActive());
    }
    
    @Test
    public void testActivateFail() throws IOException {
        // set up for failure
        doThrow(IOException.class).when(client).connect(any(CmdChannelAgentSocket.class), any(URI.class), any(ClientUpgradeRequest.class));
        
        boolean success = backend.activate();
        assertFalse("Expected unsuccessful activation", success);
        assertFalse(backend.isActive());
    }
    
    @Test
    public void testDeactivate() throws IOException {
        // release connect latch
        socketConnect.countDown();
        boolean success = backend.activate();
        assertTrue(success);
        success = backend.deactivate();
        verify(client).stop();
        assertTrue(success);
        assertFalse(backend.isActive());
    }

    private void doUnknownCredsTest(CommandsBackend backend) {
        String unknown = "UNKNOWN:UNKNOWN";
        String expected = base64EncodedHeader(unknown);
        String actual = backend.getBasicAuthHeaderValue();
        assertEquals(expected, actual);
    }

    private String base64EncodedHeader(String usernamePassword) {
        @SuppressWarnings("restriction")
        String expectedCreds = new sun.misc.BASE64Encoder().encode(usernamePassword.getBytes());
        return "Basic " + expectedCreds;
    }
}

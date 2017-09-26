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

package com.redhat.thermostat.agent.internal.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.redhat.thermostat.agent.config.AgentStartupConfiguration;
import com.redhat.thermostat.agent.http.HttpRequestService;
import com.redhat.thermostat.agent.http.RequestFailedException;
import com.redhat.thermostat.agent.internal.http.BasicHttpService.ConfigCreator;
import com.redhat.thermostat.agent.internal.http.BasicHttpService.CredentialsCreator;
import com.redhat.thermostat.agent.internal.http.BasicHttpService.HttpClientCreator;
import com.redhat.thermostat.agent.keycloak.KeycloakAccessToken;
import com.redhat.thermostat.agent.keycloak.KeycloakAccessTokenService;
import com.redhat.thermostat.shared.config.CommonPaths;
import com.redhat.thermostat.shared.config.SSLConfiguration;
import com.redhat.thermostat.storage.core.StorageCredentials;

public class HttpRequestServiceImplTest {
    private static final URI GATEWAY_URI = URI.create("http://127.0.0.1:30000/test/");
    private static final URI GET_URI = GATEWAY_URI.resolve("?q=foo&l=3");
    private static final String payload = "{}";
    private static final char[] PASSWORD = new char[] { 'p', 'a', 's', 's' };
    private static final String USERNAME = "testing";
    private static final String KEYCLOAK_ACCESS_TOKEN_STRING = "keycloak-bearer-token-value";

    private HttpClientCreator clientCreator;
    private ConfigCreator configCreator;
    private CredentialsCreator credsCreator;
    private HttpClientFacade client;
    private Request httpRequest;
    private KeycloakAccessTokenService tokenService;
    
    @Before
    public void setup() throws InterruptedException, ExecutionException, TimeoutException, RequestFailedException {
        client = mock(HttpClientFacade.class);
        httpRequest = mock(Request.class);
        when(client.newRequest(eq(GATEWAY_URI))).thenReturn(httpRequest);
        ContentResponse response = mock(ContentResponse.class);
        when(response.getStatus()).thenReturn(HttpStatus.OK_200);
        when(httpRequest.send()).thenReturn(response);
        clientCreator = mock(HttpClientCreator.class);
        when(clientCreator.create(any(SSLConfiguration.class))).thenReturn(client);
        configCreator = mock(ConfigCreator.class);
        credsCreator = mock(CredentialsCreator.class);
        StorageCredentials creds = mock(StorageCredentials.class);
        when(creds.getPassword()).thenReturn(PASSWORD);
        when(creds.getUsername()).thenReturn(USERNAME);
        when(credsCreator.create(any(CommonPaths.class))).thenReturn(creds);
        tokenService = mock(KeycloakAccessTokenService.class);
        KeycloakAccessToken keycloakAccessToken = mock(KeycloakAccessToken.class);
        when(keycloakAccessToken.getAccessToken()).thenReturn(KEYCLOAK_ACCESS_TOKEN_STRING);
        when(tokenService.getAccessToken()).thenReturn(keycloakAccessToken);
    }

    @Test
    public void testRequestWithBasicAuth() throws Exception {
        AgentStartupConfiguration configuration = createBasicAuthConfig();

        HttpRequestService service = createAndActivateRequestService(configuration);

        service.sendHttpRequest(payload, GATEWAY_URI, com.redhat.thermostat.agent.http.HttpRequestService.Method.POST);

        verify(configuration).isKeycloakEnabled();
        verifyHttpPostRequest(httpRequest);
        verify(tokenService, times(0)).getAccessToken();
    }

    @Test
    public void testRequestWithKeycloakAuth() throws Exception {
        AgentStartupConfiguration configuration = mock(AgentStartupConfiguration.class);
        when(configuration.isKeycloakEnabled()).thenReturn(true);

        HttpRequestService service = createAndActivateRequestService(configuration);

        service.sendHttpRequest(payload, GATEWAY_URI, com.redhat.thermostat.agent.http.HttpRequestService.Method.POST);

        verify(configuration).isKeycloakEnabled();
        verifyHttpPostRequest(httpRequest);

        verify(httpRequest).header(eq("Authorization"), eq("Bearer " + KEYCLOAK_ACCESS_TOKEN_STRING));
        verify(tokenService).getAccessToken();
    }

    @Test
    public void testRequestWithNullPayload() throws Exception {
        AgentStartupConfiguration configuration = createBasicAuthConfig();

        HttpRequestService service = createAndActivateRequestService(configuration);

        String response = service.sendHttpRequest(null, GATEWAY_URI, com.redhat.thermostat.agent.http.HttpRequestService.Method.POST);
        assertNull(response);

        verify(client).newRequest(GATEWAY_URI);
        verify(configuration).isKeycloakEnabled();

        verify(httpRequest, times(0)).content(any(StringContentProvider.class), anyString());
        verify(httpRequest).method(eq(HttpMethod.POST));
        verify(httpRequest).send();
    }
    
    @Test
    public void verifyBasicAuthConfig() throws Exception {
        AgentStartupConfiguration configuration = createBasicAuthConfig();

        HttpRequestService service = createAndActivateRequestService(configuration);

        service.sendHttpRequest(null, GATEWAY_URI, com.redhat.thermostat.agent.http.HttpRequestService.Method.GET);

        verify(client).newRequest(GATEWAY_URI);
        verify(configuration).isKeycloakEnabled();

        ArgumentCaptor<String> authValueCaptor = ArgumentCaptor.forClass(String.class);
        verify(httpRequest).header(eq(HttpHeader.AUTHORIZATION.asString()), authValueCaptor.capture());
        String authValueEncoded = authValueCaptor.getValue();
        assertTrue(authValueEncoded.startsWith("Basic "));
        String userPassEncoded = authValueEncoded.substring("Basic ".length());
        String decodedUserPass = getDecodedUserPass(userPassEncoded);
        String expectedCreds = USERNAME + ":" + new String(PASSWORD);
        assertEquals(expectedCreds, decodedUserPass);
        verify(httpRequest).method(eq(HttpMethod.GET));
        verify(httpRequest).send();
    }
    
    /**
     * If no authentication settings are done, no authorization headers should
     * get added.
     * 
     * @throws Exception
     */
    @Test
    public void verifyNoAuthConfig() throws Exception {
        AgentStartupConfiguration configuration = createNoAuthConfig();

        HttpRequestService service = createAndActivateRequestService(configuration);

        service.sendHttpRequest(null, GATEWAY_URI, com.redhat.thermostat.agent.http.HttpRequestService.Method.GET);

        verify(client).newRequest(GATEWAY_URI);
        verify(configuration).isKeycloakEnabled();
        verify(configuration).isBasicAuthEnabled();

        verify(httpRequest, times(0)).header(eq(HttpHeader.AUTHORIZATION.asString()), anyString());
        verify(httpRequest).method(eq(HttpMethod.GET));
        verify(httpRequest).send();
    }
    
    private String getDecodedUserPass(String userPassEncoded) throws IOException {
        @SuppressWarnings("restriction")
        byte[] decodedBytes = new sun.misc.BASE64Decoder().decodeBuffer(userPassEncoded);
        return new String(decodedBytes);
    }

    private HttpRequestService createAndActivateRequestService(AgentStartupConfiguration configuration) throws Exception {
        when(configCreator.create(any(CommonPaths.class))).thenReturn(configuration);
        HttpRequestServiceImpl service = new HttpRequestServiceImpl(clientCreator, configCreator, credsCreator);
        service.bindTokenService(tokenService);
        service.activate();
        verify(client).start();
        return service;
    }

    @Test
    public void testGetRequestWithResponse() throws Exception {
        String getContent = "foo bar";
        HttpClientCreator creator = mock(HttpClientCreator.class);
        HttpClientFacade getClient = setupHttpClient(creator, getContent);
        
        AgentStartupConfiguration configuration = createBasicAuthConfig();
        ConfigCreator configCreator = mock(ConfigCreator.class);
        when(configCreator.create(any(CommonPaths.class))).thenReturn(configuration);
        HttpRequestServiceImpl service = new HttpRequestServiceImpl(creator, configCreator, credsCreator);
        service.activate();
        String content = service.sendHttpRequest(null, GET_URI, com.redhat.thermostat.agent.http.HttpRequestService.Method.GET);
        verify(getClient).newRequest(GET_URI);
        assertEquals(getContent, content);
    }
    
    @Test
    public void testGetRequestNormalizesURI() throws Exception {
        String getContent = "foo bar";
        HttpClientCreator creator = mock(HttpClientCreator.class);
        HttpClientFacade getClient = setupHttpClient(creator, getContent);
        
        AgentStartupConfiguration configuration = createBasicAuthConfig();
        ConfigCreator configCreator = mock(ConfigCreator.class);
        when(configCreator.create(any(CommonPaths.class))).thenReturn(configuration);
        HttpRequestServiceImpl service = new HttpRequestServiceImpl(creator, configCreator, credsCreator);
        service.activate();
        
        // Add extra slashes to URI
        URI uri = URI.create("http://127.0.0.1:30000//test//?q=bar&l=5");
        URI normalized = URI.create("http://127.0.0.1:30000/test/?q=bar&l=5");
        String content = service.sendHttpRequest(null, uri, com.redhat.thermostat.agent.http.HttpRequestService.Method.GET);
        verify(getClient).newRequest(normalized);
        assertEquals(getContent, content);
    }

    private HttpClientFacade setupHttpClient(HttpClientCreator creator, String getContent) throws Exception {
        Request request = mock(Request.class);
        ContentResponse contentResponse = mock(ContentResponse.class);
        when(contentResponse.getStatus()).thenReturn(HttpStatus.OK_200);
        when(contentResponse.getContentAsString()).thenReturn(getContent);
        when(request.send()).thenReturn(contentResponse);
        HttpClientFacade getClient = mock(HttpClientFacade.class);
        when(getClient.newRequest(any(URI.class))).thenReturn(request);
        when(creator.create(any(SSLConfiguration.class))).thenReturn(getClient);
        return getClient;
    }
    
    @Test(expected = RequestFailedException.class)
    public void failureThrowsRequestFailedException() throws Exception {
        Request request = mock(Request.class);
        when(client.newRequest(any(URI.class))).thenReturn(request);
        AgentStartupConfiguration configuration = createBasicAuthConfig();
        doThrow(RequestFailedException.class).when(request).send();
        HttpRequestService service = createAndActivateRequestService(configuration);
        service.sendHttpRequest("foo", GATEWAY_URI, com.redhat.thermostat.agent.http.HttpRequestService.Method.DELETE /*any valid method*/);
    }

    private AgentStartupConfiguration createBasicAuthConfig() {
        return createAuthConfig(true, false);
    }
    
    private AgentStartupConfiguration createNoAuthConfig() {
        return createAuthConfig(false, false);
    }
    
    private AgentStartupConfiguration createAuthConfig(boolean isBasicAuthEnabled, boolean isKeycloakEnabled) {
        AgentStartupConfiguration configuration = mock(AgentStartupConfiguration.class);
        when(configuration.isKeycloakEnabled()).thenReturn(isKeycloakEnabled);
        when(configuration.isBasicAuthEnabled()).thenReturn(isBasicAuthEnabled);
        return configuration;
    }

    private void verifyHttpPostRequest(Request httpRequest) throws InterruptedException, ExecutionException, TimeoutException {
        verify(client).newRequest(GATEWAY_URI);
        verify(httpRequest).content(any(StringContentProvider.class), eq("application/json"));
        verify(httpRequest).method(eq(HttpMethod.POST));
        verify(httpRequest).send();
    }
}

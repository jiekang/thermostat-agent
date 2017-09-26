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
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.redhat.thermostat.agent.config.AgentStartupConfiguration;
import com.redhat.thermostat.agent.http.RequestFailedException;
import com.redhat.thermostat.agent.internal.http.BasicHttpService.ConfigCreator;
import com.redhat.thermostat.agent.internal.http.BasicHttpService.CredentialsCreator;
import com.redhat.thermostat.agent.internal.http.BasicHttpService.HttpClientCreator;
import com.redhat.thermostat.agent.keycloak.KeycloakAccessToken;
import com.redhat.thermostat.agent.keycloak.KeycloakAccessTokenService;
import com.redhat.thermostat.shared.config.CommonPaths;
import com.redhat.thermostat.shared.config.SSLConfiguration;
import com.redhat.thermostat.storage.core.StorageCredentials;

public class KeycloakAccessTokenServiceImplTest {

    private static final String KEYCLOAK_URL = "http://127.0.0.1:31000/keycloak";
    private static final String KEYCLOAK_CLIENT = "client";
    private static final String KEYCLOAK_REALM = "realm";
    private static final char[] PASSWORD = new char[] { 'p', 'a', 's', 's' };
    private static final String USERNAME = "testing";

    private HttpClientCreator clientCreator;
    private ConfigCreator configCreator;
    private CredentialsCreator credsCreator;
    private HttpClientFacade client;
    private AgentStartupConfiguration configuration;
    private Request keycloakRequest = mock(Request.class);
    
    @Before
    public void setup() throws InterruptedException, ExecutionException, TimeoutException, RequestFailedException {
        client = mock(HttpClientFacade.class);
        clientCreator = mock(HttpClientCreator.class);
        when(clientCreator.create(any(SSLConfiguration.class))).thenReturn(client);
        configCreator = mock(ConfigCreator.class);
        credsCreator = mock(CredentialsCreator.class);
        StorageCredentials creds = mock(StorageCredentials.class);
        when(creds.getPassword()).thenReturn(PASSWORD);
        when(creds.getUsername()).thenReturn(USERNAME);
        when(credsCreator.create(any(CommonPaths.class))).thenReturn(creds);
        configuration = mock(AgentStartupConfiguration.class);
        setupKeycloakConfig(configuration);
        keycloakRequest = mock(Request.class);
        setupKeycloakRequest(keycloakRequest);
    }

    @Test
    public void verifyGetAccessToken() throws Exception {
        KeycloakAccessTokenService service = createAndActivateRequestService();

        service.getAccessToken();

        verifyKeycloakAcquire();
    }
    
    @Test
    public void verifyKeycloakAccessTokenRefresh() throws Exception {
        String refreshTokenValue = "refresh";
        KeycloakAccessToken expiredToken = mock(KeycloakAccessToken.class);
        when(expiredToken.isKeycloakTokenExpired()).thenReturn(true);
        when(expiredToken.getRefreshToken()).thenReturn(refreshTokenValue);
        KeycloakAccessTokenService service = createAndActivateRequestService(expiredToken);
    
        service.getAccessToken();
    
        verifyKeycloakRefresh(refreshTokenValue);
    }

    private KeycloakAccessTokenService createAndActivateRequestService() throws Exception {
        return createAndActivateRequestService(null);
    }

    private KeycloakAccessTokenService createAndActivateRequestService(KeycloakAccessToken accessTokenInitial) throws Exception {
        when(configCreator.create(any(CommonPaths.class))).thenReturn(configuration);
        KeycloakAccessTokenServiceImpl service = new KeycloakAccessTokenServiceImpl(clientCreator, configCreator, credsCreator);
        service.setKeycloakAccessToken(accessTokenInitial);
        service.activate();
        verify(client).start();
        return service;
    }

    private void setupKeycloakConfig(AgentStartupConfiguration configuration) {
        when(configuration.isKeycloakEnabled()).thenReturn(true);
        when(configuration.getKeycloakUrl()).thenReturn(KEYCLOAK_URL);
        when(configuration.getKeycloakClient()).thenReturn(KEYCLOAK_CLIENT);
        when(configuration.getKeycloakRealm()).thenReturn(KEYCLOAK_REALM);
    }

    private void setupKeycloakRequest(Request keycloakRequest) throws InterruptedException, ExecutionException, TimeoutException {
        when(client.newRequest(KEYCLOAK_URL + "/auth/realms/" + KEYCLOAK_REALM + "/protocol/openid-connect/token")).thenReturn(keycloakRequest);

        ContentResponse response = mock(ContentResponse.class);
        when(response.getStatus()).thenReturn(HttpStatus.OK_200);

        String keycloakToken = "{" +
                "\"access_token\": \"access\"," +
                "\"expires_in\": 0," +
                "\"refresh_expires_in\": 2," +
                "\"refresh_token\": \"refresh\"," +
                "\"token_type\": \"bearer\"," +
                "\"id_token\": \"id\"," +
                "\"not-before-policy\": 3," +
                "\"session_state\": \"state\"" +
                "}";

        when(response.getContentAsString()).thenReturn(keycloakToken);
        when(keycloakRequest.send()).thenReturn(response);
    }

    private void verifyKeycloakAcquire() throws InterruptedException, ExecutionException, TimeoutException {
        String expected = "grant_type=password&client_id=" + KEYCLOAK_CLIENT + "&username=" + USERNAME + "&password=" + new String(PASSWORD);
        doKeycloakVerification(expected);
    }
    
    private void verifyKeycloakRefresh(String refreshTokenValue) throws InterruptedException, TimeoutException, ExecutionException {
        String expected = "grant_type=refresh_token&client_id=" + KEYCLOAK_CLIENT + "&refresh_token=" + refreshTokenValue;
        doKeycloakVerification(expected);
    }
    
    private void doKeycloakVerification(final String expectedPayload) throws InterruptedException, TimeoutException, ExecutionException {
        ArgumentCaptor<StringContentProvider> payloadCaptor = ArgumentCaptor.forClass(StringContentProvider.class);
        verify(keycloakRequest).content(payloadCaptor.capture(), eq("application/x-www-form-urlencoded"));
        verify(keycloakRequest).method(eq(HttpMethod.POST));
        verify(keycloakRequest).send();
    
        StringContentProvider provider = payloadCaptor.getValue();
        for (ByteBuffer buffer : provider) {
            byte[] bytes = buffer.array();
            String content = new String(bytes, StandardCharsets.UTF_8);
            assertEquals(expectedPayload, content);
        }
    }
}

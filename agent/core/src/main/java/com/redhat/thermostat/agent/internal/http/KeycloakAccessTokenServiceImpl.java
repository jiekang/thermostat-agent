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

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.redhat.thermostat.agent.http.RequestFailedException;
import com.redhat.thermostat.agent.keycloak.KeycloakAccessToken;
import com.redhat.thermostat.agent.keycloak.KeycloakAccessTokenService;
import com.redhat.thermostat.common.utils.LoggingUtils;
import com.redhat.thermostat.shared.config.CommonPaths;
import com.redhat.thermostat.shared.config.SSLConfiguration;

@Component
@Service(value = KeycloakAccessTokenService.class)
public class KeycloakAccessTokenServiceImpl extends BasicHttpService implements KeycloakAccessTokenService {

    private static final Logger logger = LoggingUtils.getLogger(KeycloakAccessTokenServiceImpl.class);
    private static final String KEYCLOAK_TOKEN_SERVICE = "/auth/realms/__REALM__/protocol/openid-connect/token";
    private static final String KEYCLOAK_CONTENT_TYPE = "application/x-www-form-urlencoded";
    private final Object accessTokenLock = new Object();
    private final Gson gson = new GsonBuilder().create();
    private KeycloakAccessToken keycloakAccessToken;

    @Reference
    private SSLConfiguration sslConfig;
    @Reference
    private CommonPaths commonPaths;

    public KeycloakAccessTokenServiceImpl() {
        this(new HttpClientCreator(), new ConfigCreator(), new CredentialsCreator());
    }

    // For testing purposes
    KeycloakAccessTokenServiceImpl(HttpClientCreator clientCreator, ConfigCreator configCreator, CredentialsCreator credsCreator) {
        super(clientCreator, configCreator, credsCreator);
    }

    @Activate
    public void activate() {
        super.doActivate(commonPaths, sslConfig, KeycloakAccessTokenService.class.getSimpleName());
    }

    @Override
    public KeycloakAccessToken getAccessToken() throws RequestFailedException {
        synchronized(accessTokenLock) {
            if (keycloakAccessToken == null) {
                keycloakAccessToken = acquireKeycloakToken();
            } else if (keycloakAccessToken.isKeycloakTokenExpired()) {
                logger.log(Level.FINE, "Keycloak Token expired attempting to reacquire via refresh_token");
                keycloakAccessToken = refreshKeycloakToken();

                if (keycloakAccessToken == null) {
                    logger.log(Level.WARNING, "Unable to refresh Keycloak token, attempting to acquire new token");
                    keycloakAccessToken = acquireKeycloakToken();
                }

                if (keycloakAccessToken == null) {
                    logger.log(Level.SEVERE, "Unable to reacquire KeycloakToken.");
                    throw new RequestFailedException("Keycloak token expired and attempt to refresh and reacquire Keycloak token failed.");
                }
            }

        }
        return keycloakAccessToken;
    }

    private KeycloakAccessToken acquireKeycloakToken() {
        return requestKeycloakToken(getKeycloakAccessPayload());
    }

    private KeycloakAccessToken refreshKeycloakToken() {
        return requestKeycloakToken(getKeycloakRefreshPayload());
    }

    private KeycloakAccessToken requestKeycloakToken(String payload) {
        String url = agentStartupConfiguration.getKeycloakUrl() + KEYCLOAK_TOKEN_SERVICE.replace("__REALM__", agentStartupConfiguration.getKeycloakRealm());
        Request request = client.newRequest(url);
        request.content(new StringContentProvider(payload), KEYCLOAK_CONTENT_TYPE);
        request.method(HttpMethod.POST);

        try {
            ContentResponse response = request.send();
            if (response.getStatus() == HttpStatus.OK_200) {

                String content = response.getContentAsString();

                keycloakAccessToken = gson.fromJson(content, KeycloakAccessToken.class);
                keycloakAccessToken.setAcquireTime(System.nanoTime());

                logger.log(Level.FINE, "Keycloak Token acquired");
                return keycloakAccessToken;
            } else {
                logger.log(Level.WARNING, "Failed to acquire Keycloak token: " + response.getStatus() + " " + response.getReason());
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to acquire Keycloak token", e);
        }
        return null;
    }

    private String getKeycloakAccessPayload() {
        String username = creds.getUsername();
        String password = new String(creds.getPassword());
        return "grant_type=password&client_id=" + agentStartupConfiguration.getKeycloakClient() +
                "&username=" + username +
                "&password=" + password;
    }

    private String getKeycloakRefreshPayload() {
        return "grant_type=refresh_token&client_id=" + agentStartupConfiguration.getKeycloakClient() +
                "&refresh_token=" + keycloakAccessToken.getRefreshToken();
    }

    // For testing purpuses
    void setKeycloakAccessToken(KeycloakAccessToken token) {
        synchronized (accessTokenLock) {
            this.keycloakAccessToken = token;
        }
    }
}

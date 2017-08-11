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

package com.redhat.thermostat.agent.http;


import java.io.IOException;
import java.net.URI;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
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
import com.redhat.thermostat.agent.config.AgentConfigsUtils;
import com.redhat.thermostat.agent.config.AgentStartupConfiguration;
import com.redhat.thermostat.agent.http.internal.keycloak.KeycloakAccessToken;
import com.redhat.thermostat.common.utils.LoggingUtils;
import com.redhat.thermostat.shared.config.CommonPaths;
import com.redhat.thermostat.shared.config.SSLConfiguration;

@Component
@Service(value = HttpRequestService.class)
public class HttpRequestService {
    
    private static final Logger logger = LoggingUtils.getLogger(HttpRequestService.class);

    private static final String KEYCLOAK_TOKEN_SERVICE = "/auth/realms/__REALM__/protocol/openid-connect/token";
    private static final String KEYCLOAK_CONTENT_TYPE = "application/x-www-form-urlencoded";

    private final HttpClientCreator httpClientCreator;
    private final ConfigCreator configCreator;
    private Gson gson = new GsonBuilder().create();
    private HttpClientFacade client;
    private AgentStartupConfiguration agentStartupConfiguration;

    private KeycloakAccessToken keycloakAccessToken;
    @Reference
    private SSLConfiguration sslConfig;
    @Reference
    private CommonPaths commonPaths;

    public HttpRequestService() {
        this(new HttpClientCreator(), new ConfigCreator());
    }

    HttpRequestService(HttpClientCreator clientCreator, ConfigCreator configCreator) {
        this.httpClientCreator = clientCreator;
        this.configCreator = configCreator;
    }

    @Activate
    public void activate() {
        try {
            agentStartupConfiguration = configCreator.create(commonPaths);
            client = httpClientCreator.create(sslConfig);
            client.start();
            logger.log(Level.FINE, "HttpRequestService activated");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "HttpRequestService failed to start correctly. Behaviour undefined.", e);
        }
    }

    /**
     * Send a HTTP request
     * @param jsonPayload The payload to send, or null if no payload
     * @param uri The complete URI to send to
     * @param requestMethod The HTTP request type: GET, PUT, POST or DELETE
     * @return The returned body for GET requests. {@code null} otherwise.
     */
    public String sendHttpRequest(String jsonPayload, URI uri, Method requestMethod) throws RequestFailedException {
        // Normalize URI to ensure any duplicate slashes are removed
        uri = uri.normalize();
        Request request = client.newRequest(uri);
        if (jsonPayload != null) {
            request.content(new StringContentProvider(jsonPayload), "application/json");
        }
        request.method(requestMethod.getHttpMethod());

        try {
            if (agentStartupConfiguration.isKeycloakEnabled()) {
                request.header("Authorization", "Bearer " + getAccessToken());
            }
            ContentResponse response =  request.send();
            int status = response.getStatus();
            if (status != HttpStatus.OK_200) {
                throw new RequestFailedException(status, "Request to gateway failed. Reason: " + response.getReason());
            }
            if (requestMethod == Method.GET) {
                return response.getContentAsString();
            } else {
                return null;
            }
        } catch (InterruptedException | TimeoutException | IOException | ExecutionException e) {
            throw new RequestFailedException(e);
        }
    }

    private String getAccessToken() throws IOException {
        if (keycloakAccessToken == null) {
            keycloakAccessToken = acquireKeycloakToken();
        } else if (isKeycloakTokenExpired()) {
            logger.log(Level.FINE, "Keycloak Token expired attempting to reacquire via refresh_token");
            keycloakAccessToken = refreshKeycloakToken();

            if (keycloakAccessToken == null) {
                logger.log(Level.WARNING, "Unable to refresh Keycloak token, attempting to acquire new token");
                keycloakAccessToken = acquireKeycloakToken();
            }

            if (keycloakAccessToken == null) {
                logger.log(Level.SEVERE, "Unable to reacquire KeycloakToken.");
                throw new IOException("Keycloak token expired and attempt to refresh and reacquire Keycloak token failed.");
            }
        }

        return keycloakAccessToken.getAccessToken();
    }

    private boolean isKeycloakTokenExpired() {
        return System.nanoTime() > TimeUnit.NANOSECONDS.convert(keycloakAccessToken.getExpiresIn(), TimeUnit.SECONDS) + keycloakAccessToken.getAcquireTime();
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
        return "grant_type=password&client_id=" + agentStartupConfiguration.getKeycloakClient() +
                "&username=" + agentStartupConfiguration.getKeycloakUsername() +
                "&password=" + agentStartupConfiguration.getKeycloakPassword();
    }

    private String getKeycloakRefreshPayload() {
        return "grant_type=refresh_token&client_id=" + agentStartupConfiguration.getKeycloakClient() +
                "&refresh_token=" + keycloakAccessToken.getRefreshToken();
    }

    // Package-private for testing
   void setConfiguration(AgentStartupConfiguration configuration) {
        this.agentStartupConfiguration = configuration;
    }

    static class HttpClientCreator {
    
        HttpClientFacade create(SSLConfiguration config) {
            return new HttpClientFacade(config);
        }

    }

    static class ConfigCreator {
        AgentStartupConfiguration create(CommonPaths commonPaths) {
            AgentConfigsUtils.setConfigFiles(commonPaths.getSystemAgentConfigurationFile(),
                    commonPaths.getUserAgentConfigurationFile());
            return AgentConfigsUtils.createAgentConfigs();
        }
    }
    
    @SuppressWarnings("serial")
    public static class RequestFailedException extends Exception {
        
        public static final int UNKNOWN_RESPONSE_CODE = -1;
        private final int responseCode;
        private final String reasonStr;
        
        private RequestFailedException(Throwable e) {
            this(UNKNOWN_RESPONSE_CODE, e.getMessage(), e);
        }
        
        private RequestFailedException(int responseCode, String reason) {
            this(responseCode, reason, null);
        }
        
        private RequestFailedException(int responseCode, String reason, Throwable cause) {
            super(reason, cause);
            this.reasonStr = reason;
            this.responseCode = responseCode;
        }
        
        public int getResponseCode() {
            return responseCode;
        }
        
        public String getReason() {
            return reasonStr;
        }
    }

    /**
     * HTTP methods for microservice requests.
     * @author mzezulka
     */
    public static enum Method {
        POST(HttpMethod.POST), 
        GET(HttpMethod.GET), 
        DELETE(HttpMethod.DELETE), 
        PUT(HttpMethod.PUT);
        
        private final HttpMethod httpMethod;

        Method(HttpMethod method) {
            this.httpMethod = method;
        }

        public HttpMethod getHttpMethod() {
            return httpMethod;
        }
    }
}

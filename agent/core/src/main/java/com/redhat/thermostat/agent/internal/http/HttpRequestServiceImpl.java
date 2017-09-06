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


import java.net.URI;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpStatus;

import com.redhat.thermostat.agent.http.HttpRequestService;
import com.redhat.thermostat.agent.http.RequestFailedException;
import com.redhat.thermostat.agent.keycloak.KeycloakAccessToken;
import com.redhat.thermostat.agent.keycloak.KeycloakAccessTokenService;
import com.redhat.thermostat.common.utils.LoggingUtils;
import com.redhat.thermostat.shared.config.CommonPaths;
import com.redhat.thermostat.shared.config.SSLConfiguration;

@Component
@Service(value = HttpRequestService.class)
public class HttpRequestServiceImpl extends BasicHttpService implements HttpRequestService {
    
    private static final Logger logger = LoggingUtils.getLogger(HttpRequestServiceImpl.class);

    private static final String UNKNOWN_CREDS = "UNKNOWN:UNKNOWN";

    @Reference
    private SSLConfiguration sslConfig;
    @Reference
    private CommonPaths commonPaths;
    @Reference
    private KeycloakAccessTokenService tokenService;

    public HttpRequestServiceImpl() {
        this(new HttpClientCreator(), new ConfigCreator(), new CredentialsCreator());
    }

    // For testing purposes
    HttpRequestServiceImpl(HttpClientCreator clientCreator, ConfigCreator configCreator, CredentialsCreator credsCreator) {
        super(clientCreator, configCreator, credsCreator);
    }

    @Activate
    public void activate() {
        super.doActivate(commonPaths, sslConfig);
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
                KeycloakAccessToken accessToken = tokenService.getAccessToken();
                request.header(HttpHeader.AUTHORIZATION.asString(), "Bearer " + accessToken.getAccessToken());
            } else if (agentStartupConfiguration.isBasicAuthEnabled()) {
                request.header(HttpHeader.AUTHORIZATION.asString(),
                               getBasicAuthHeaderValue());
            } else {
                logger.warning("Neither KEYCLOAK_ENABLED=true nor BASIC_AUTH_ENABLED=true. Requests will probably fail.");
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
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            throw new RequestFailedException(e);
        }
    }
    
    private String getBasicAuthHeaderValue() {
        String username = creds.getUsername();
        char[] pwdChar = creds.getPassword();
        String userpassword;
        if (username == null || username.isEmpty() || pwdChar == null) {
            logger.warning("No credentials specified in " + commonPaths.getUserAgentAuthConfigFile() + ". The connection will fail.");
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
    
    // DS bind methods
    
    protected void bindTokenService(KeycloakAccessTokenService tokenService) {
        this.tokenService = tokenService;
    }
}

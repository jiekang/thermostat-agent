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

package com.redhat.thermostat.agent.internal;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.redhat.thermostat.agent.dao.AgentInfoDAO;
import com.redhat.thermostat.agent.internal.AgentInformationTypeAdapter.AgentInformationUpdateTypeAdapter;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpContentResponse;
import org.eclipse.jetty.client.HttpRequest;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;

import com.redhat.thermostat.common.utils.LoggingUtils;
import com.redhat.thermostat.storage.core.AgentId;
import com.redhat.thermostat.storage.model.AgentInformation;

@Component
@Service(value = AgentInfoDAO.class)
public class AgentInfoDAOImpl implements AgentInfoDAO {

    private static final Logger logger = LoggingUtils.getLogger(AgentInfoDAOImpl.class);
    
    private static final String GATEWAY_URL = "http://localhost:26000/api/v100"; // TODO configurable
    private static final String GATEWAY_PATH = "/agent-config/systems/*/agents/";
    private static final String CONTENT_TYPE = "application/json";
    
    private final HttpHelper httpHelper;
    private final JsonHelper jsonHelper;

    public AgentInfoDAOImpl() throws Exception {
        this(new HttpHelper(new HttpClient()), new JsonHelper(new AgentInformationTypeAdapter(),
                                                              new AgentInformationUpdateTypeAdapter()));
    }

    AgentInfoDAOImpl(HttpHelper httpHelper, JsonHelper jsonHelper) throws Exception {
        this.httpHelper = httpHelper;
        this.jsonHelper = jsonHelper;
        
        this.httpHelper.startClient();
    }

    @Override
    public List<AgentInformation> getAllAgentInformation() {
        return Collections.emptyList(); // TODO Remove once Agent Id completer is removed
    }

    @Override
    public AgentInformation getAgentInformation(final AgentId agentId) {
        return null; // TODO Remove once VM Id completer is removed
    }

    @Override
    public Set<AgentId> getAgentIds() {
        return Collections.emptySet(); // TODO Remove once VM Id completer is removed
    }

    @Override
    public void addAgentInformation(final AgentInformation agentInfo) {
        try {
            // Encode as JSON and send as POST request
            String json = jsonHelper.toJson(Arrays.asList(agentInfo));
            StringContentProvider provider = httpHelper.createContentProvider(json);
            
            String url = getURL(agentInfo.getAgentId());
            Request httpRequest = httpHelper.newRequest(url);
            httpRequest.method(HttpMethod.POST);
            httpRequest.content(provider, CONTENT_TYPE);
            sendRequest(httpRequest);
        } catch (IOException | InterruptedException | TimeoutException | ExecutionException e) {
           logger.log(Level.WARNING, "Failed to send agent information to web gateway", e);
        }
    }

    @Override
    public void removeAgentInformation(final AgentInformation agentInfo) {
        try {
            // Delete AgentInformation with matching Agent ID
            String url = getURL(agentInfo.getAgentId());
            Request httpRequest = httpHelper.newRequest(url);
            httpRequest.method(HttpMethod.DELETE);
            sendRequest(httpRequest);
        } catch (IOException | InterruptedException | TimeoutException | ExecutionException e) {
           logger.log(Level.WARNING, "Failed to delete agent information from web gateway", e);
        }
    }

    @Override
    public void updateAgentInformation(final AgentInformation agentInfo) {
        try {
            // Encode as JSON and send as PUT request
            AgentInformationUpdate update = new AgentInformationUpdate(agentInfo);
            String json = jsonHelper.toJson(update);
            StringContentProvider provider = httpHelper.createContentProvider(json);
            
            String url = getURL(agentInfo.getAgentId());
            Request httpRequest = httpHelper.newRequest(url);
            httpRequest.method(HttpMethod.PUT);
            httpRequest.content(provider, CONTENT_TYPE);
            sendRequest(httpRequest);
        } catch (IOException | InterruptedException | TimeoutException | ExecutionException e) {
           logger.log(Level.WARNING, "Failed to send agent information update to web gateway", e);
        }
    }

    private void sendRequest(Request httpRequest)
            throws InterruptedException, TimeoutException, ExecutionException, IOException {
        ContentResponse resp = httpRequest.send();
        int status = resp.getStatus();
        if (status != HttpStatus.OK_200) {
            throw new IOException("Gateway returned HTTP status " + String.valueOf(status) + " - " + resp.getReason());
        }
    }
    
    private String getURL(String agentId) {
        StringBuilder builder = new StringBuilder();
        builder.append(GATEWAY_URL);
        builder.append(GATEWAY_PATH);
        builder.append(agentId);
        return builder.toString();
    }
    
    static class AgentInformationUpdate {
        
        private final AgentInformation info;
        
        AgentInformationUpdate(AgentInformation info) {
            this.info = info;
        }
        
        AgentInformation getInfo() {
            return info;
        }
        
    }
    
    // For testing purposes
    static class JsonHelper {
        
        private final AgentInformationTypeAdapter typeAdapter;
        private final AgentInformationUpdateTypeAdapter updateTypeAdapter;
        
        public JsonHelper(AgentInformationTypeAdapter typeAdapter, AgentInformationUpdateTypeAdapter updateTypeAdapter) {
            this.typeAdapter = typeAdapter;
            this.updateTypeAdapter = updateTypeAdapter;
        }
        
        String toJson(List<AgentInformation> infos) throws IOException {
            return typeAdapter.toJson(infos);
        }
        
        String toJson(AgentInformationUpdate update) throws IOException {
            return updateTypeAdapter.toJson(update);
        }
        
    }
    
    // For testing purposes
    static class HttpHelper {
        
        private final HttpClient httpClient;

        HttpHelper(HttpClient httpClient) {
            this.httpClient = httpClient;
        }
        
        void startClient() throws Exception {
            httpClient.start();
        }
        
        StringContentProvider createContentProvider(String content) {
            return new StringContentProvider(content);
        }
        
        Request newRequest(String url) {
            return new MockRequest(httpClient, URI.create(url));
        }
        
    }
    
    // FIXME This class should be removed when the web gateway has a microservice for this DAO
    private static class MockRequest extends HttpRequest {

        MockRequest(HttpClient client, URI uri) {
            super(client, uri);
        }
        
        @Override
        public ContentResponse send() throws InterruptedException, TimeoutException, ExecutionException {
            return new MockResponse();
        }
        
    }
    
    // FIXME This class should be removed when the web gateway has a microservice for this DAO
    private static class MockResponse extends HttpContentResponse {

        MockResponse() {
            super(null, null, null);
        }
        
        @Override
        public int getStatus() {
            return HttpStatus.OK_200;
        }
        
    }
    
}


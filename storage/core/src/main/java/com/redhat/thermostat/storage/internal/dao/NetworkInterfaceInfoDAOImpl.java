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

package com.redhat.thermostat.storage.internal.dao;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpContentResponse;
import org.eclipse.jetty.client.HttpRequest;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;

import com.redhat.thermostat.common.utils.LoggingUtils;
import com.redhat.thermostat.storage.dao.NetworkInterfaceInfoDAO;
import com.redhat.thermostat.storage.internal.dao.NetworkInterfaceInfoTypeAdapter.NetworkInterfaceInfoUpdateTypeAdapter;
import com.redhat.thermostat.storage.model.NetworkInterfaceInfo;

public class NetworkInterfaceInfoDAOImpl implements NetworkInterfaceInfoDAO {

    private static final Logger logger = LoggingUtils.getLogger(NetworkInterfaceInfoDAOImpl.class);

    private static final String GATEWAY_URL = "http://localhost:26000/api/v100"; // TODO configurable
    private static final String GATEWAY_PATH = "/network-info/systems/*/agents/";
    private static final String CONTENT_TYPE = "application/json";
    private static final String GATEWAY_QUERY = "?q=";
    private static final String QUERY_INTERFACE_PARAM = ifaceKey.getName() + "==";
    
    private final HttpHelper httpHelper;
    private final JsonHelper jsonHelper;

    public NetworkInterfaceInfoDAOImpl() throws Exception {
        this(new HttpHelper(new HttpClient()), new JsonHelper(new NetworkInterfaceInfoTypeAdapter(), 
                new NetworkInterfaceInfoUpdateTypeAdapter()));
    }

    NetworkInterfaceInfoDAOImpl(HttpHelper httpHelper, JsonHelper jsonHelper) throws Exception {
        this.httpHelper = httpHelper;
        this.jsonHelper = jsonHelper;
        
        this.httpHelper.startClient();
    }

    @Override
    public void putNetworkInterfaceInfo(final NetworkInterfaceInfo info) {
        try {
            // Check if there is an existing entry for this interface
            NetworkInterfaceInfo existing = getExistingInfo(info.getAgentId(), info.getInterfaceName());
            if (existing == null) {
                // Add a new network interface info record
                addNetworkInterfaceInfo(info);
            } else if (!existing.equals(info)) { // Check if update necessary
                // Update existing record
                updateNetworkInterfaceInfo(info);
            }
        } catch (IOException | InterruptedException | TimeoutException | ExecutionException e) {
            logger.log(Level.WARNING, "Failed to query network interface information from web gateway", e);
        }
    }
    
    private NetworkInterfaceInfo getExistingInfo(String agentId, String interfaceName) throws InterruptedException, TimeoutException, ExecutionException, IOException {
        NetworkInterfaceInfo result = null;
        // Query NetworkInterfaceInfo with matching interface name
        String url = getURLWithQueryString(agentId, interfaceName);
        Request httpRequest = httpHelper.newRequest(url);
        httpRequest.method(HttpMethod.GET);
        ContentResponse response = sendRequest(httpRequest);
        String json = response.getContentAsString();

        // Return the first item, or null if there was no match
        List<NetworkInterfaceInfo> infos = jsonHelper.fromJson(json);
        if (!infos.isEmpty()) {
            result = infos.get(0);
        }
        return result;
    }
    
    private void addNetworkInterfaceInfo(NetworkInterfaceInfo info) {
        try {
            // Encode as JSON and send as POST request
            String json = jsonHelper.toJson(Arrays.asList(info));
            StringContentProvider provider = httpHelper.createContentProvider(json);
            
            String url = getURL(info.getAgentId());
            Request httpRequest = httpHelper.newRequest(url);
            httpRequest.method(HttpMethod.POST);
            httpRequest.content(provider, CONTENT_TYPE);
            sendRequest(httpRequest);
        } catch (IOException | InterruptedException | TimeoutException | ExecutionException e) {
           logger.log(Level.WARNING, "Failed to send network interface information to web gateway", e);
        }
    }
    
    private void updateNetworkInterfaceInfo(NetworkInterfaceInfo info) {
        try {
            // Encode as JSON and send as PUT request
            NetworkInterfaceInfoUpdate update = new NetworkInterfaceInfoUpdate(info.getIp4Addr(), info.getIp6Addr());
            String json = jsonHelper.toJson(update);
            StringContentProvider provider = httpHelper.createContentProvider(json);
            
            String url = getURLWithQueryString(info.getAgentId(), info.getInterfaceName());
            Request httpRequest = httpHelper.newRequest(url);
            httpRequest.method(HttpMethod.PUT);
            httpRequest.content(provider, CONTENT_TYPE);
            sendRequest(httpRequest);
        } catch (IOException | InterruptedException | TimeoutException | ExecutionException e) {
           logger.log(Level.WARNING, "Failed to send network interface information update to web gateway", e);
        }
    }

    private ContentResponse sendRequest(Request httpRequest)
            throws InterruptedException, TimeoutException, ExecutionException, IOException {
        ContentResponse resp = httpRequest.send();
        int status = resp.getStatus();
        if (status != HttpStatus.OK_200) {
            throw new IOException("Gateway returned HTTP status " + String.valueOf(status) + " - " + resp.getReason());
        }
        return resp;
    }
    
    private String getURL(String agentId) {
        StringBuilder builder = buildURL(agentId);
        return builder.toString();
    }
    
    private String getURLWithQueryString(String agentId, String interfaceName) throws UnsupportedEncodingException {
        StringBuilder builder = buildURL(agentId);
        builder.append(GATEWAY_QUERY);
        String query = QUERY_INTERFACE_PARAM.concat(interfaceName);
        String encodedQuery = URLEncoder.encode(query, "UTF-8");
        builder.append(encodedQuery);
        return builder.toString();
    }

    private StringBuilder buildURL(String agentId) {
        StringBuilder builder = new StringBuilder();
        builder.append(GATEWAY_URL);
        builder.append(GATEWAY_PATH);
        builder.append(agentId);
        return builder;
    }
    
    static class NetworkInterfaceInfoUpdate {
        
        private final String ipv4Addr;
        private final String ipv6Addr;
        
        public NetworkInterfaceInfoUpdate(String ipv4Addr, String ipv6Addr) {
            this.ipv4Addr = ipv4Addr;
            this.ipv6Addr = ipv6Addr;
        }
        
        String getIPv4Addr() {
            return ipv4Addr;
        }
        
        String getIPv6Addr() {
            return ipv6Addr;
        }
        
    }
    
    // For testing purposes
    static class JsonHelper {
        
        private final NetworkInterfaceInfoTypeAdapter typeAdapter;
        private final NetworkInterfaceInfoUpdateTypeAdapter updateTypeAdapter;
        
        public JsonHelper(NetworkInterfaceInfoTypeAdapter typeAdapter, NetworkInterfaceInfoUpdateTypeAdapter updateTypeAdapter) {
            this.typeAdapter = typeAdapter;
            this.updateTypeAdapter = updateTypeAdapter;
        }
        
        List<NetworkInterfaceInfo> fromJson(String json) throws IOException {
            return typeAdapter.fromJson(json);
        }
        
        String toJson(List<NetworkInterfaceInfo> infos) throws IOException {
            return typeAdapter.toJson(infos);
        }
        
        String toJson(NetworkInterfaceInfoUpdate update) throws IOException {
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
        
        @Override
        public String getContentAsString() {
            // Simulate empty response
            return "{\"response\" : [], \"time\" : \"0\"}";
        }
        
    }
}


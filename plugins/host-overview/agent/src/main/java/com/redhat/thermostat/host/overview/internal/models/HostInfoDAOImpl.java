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

package com.redhat.thermostat.host.overview.internal.models;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import com.redhat.thermostat.host.overview.internal.HostInfoTypeAdapter;
import com.redhat.thermostat.host.overview.internal.common.PluginConfiguration;
import com.redhat.thermostat.host.overview.internal.common.PluginDAOBase;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpContentResponse;
import org.eclipse.jetty.client.HttpRequest;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpStatus;

import com.redhat.thermostat.common.utils.LoggingUtils;
import com.redhat.thermostat.host.overview.model.HostInfo;

public class HostInfoDAOImpl extends PluginDAOBase<HostInfo, HostInfoDAOImpl> implements HostInfoDAO {
    
    private static final Logger logger = LoggingUtils.getLogger(HostInfoDAOImpl.class);

    public static final String PLUGIN_ID = "host-overview";

    private final JsonHelper jsonHelper;
    private final HttpHelper httpHelper;

    public HostInfoDAOImpl(PluginConfiguration config) throws Exception {
        this(config, new HttpClient());
    }

    public HostInfoDAOImpl(PluginConfiguration config, HttpClient client) throws Exception {
        this(config, client, new JsonHelper(new HostInfoTypeAdapter()), new HttpHelper(client));
    }

    HostInfoDAOImpl(PluginConfiguration config, HttpClient client, JsonHelper jsonHelper, HttpHelper httpHelper) throws Exception {
        super(config, client);
        this.jsonHelper = jsonHelper;
        this.httpHelper = httpHelper;
        this.httpHelper.startClient(this.httpClient);
    }

    public String getPluginId() {
        return PLUGIN_ID;
    }

    public Logger getLogger() {
        return logger;
    }

    @Override
    protected String toJsonString(HostInfo obj) throws IOException {
        return jsonHelper.toJson(Arrays.asList(obj));
    }

    // For testing purposes
    static class JsonHelper {
        
        private final HostInfoTypeAdapter typeAdapter;
        
        JsonHelper(HostInfoTypeAdapter typeAdapter) {
            this.typeAdapter = typeAdapter;
        }
        
        String toJson(List<HostInfo> infos) throws IOException {
            return typeAdapter.toJson(infos);
        }
        
    }
    
    // For testing purposes
    static class HttpHelper {
        
        private final HttpClient httpClient;

        HttpHelper(HttpClient httpClient) {
            this.httpClient = httpClient;
        }
        
        void startClient(HttpClient httpClient) throws Exception {
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


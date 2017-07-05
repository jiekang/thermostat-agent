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

package com.redhat.thermostat.vm.memory.agent.internal.models;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

import com.redhat.thermostat.common.config.experimental.ConfigurationInfoSource;
import com.redhat.thermostat.common.plugin.PluginConfiguration;
import com.redhat.thermostat.common.utils.LoggingUtils;
import com.redhat.thermostat.vm.memory.agent.model.VmTlabStat;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpStatus;

@Component
@Service(value = VmTlabStatDAO.class)
public class VmTlabStatDAOImpl implements VmTlabStatDAO {

    private static final Logger logger = LoggingUtils.getLogger(VmTlabStatDAOImpl.class);
    private static final String PLUGIN_ID = "vm-memory";
    private static final String CONTENT_TYPE = "application/json";

    private final HttpClient client;
    private final HttpHelper httpHelper;
    private final JsonHelper jsonHelper;
    private final ConfigurationCreator configurationCreator;

    private String gatewayURL;

    @Reference
    private ConfigurationInfoSource configInfoSource;

    public VmTlabStatDAOImpl() throws Exception {
        this(new HttpClient(), new HttpHelper(), new JsonHelper(new VmTlabStatTypeAdapter()), new ConfigurationCreator(), null);
    }

    VmTlabStatDAOImpl(HttpClient client, HttpHelper httpHelper, JsonHelper jsonHelper,
            ConfigurationCreator configurationCreator, ConfigurationInfoSource configInfoSource) throws Exception {
        this.client = client;
        this.httpHelper = httpHelper;
        this.jsonHelper = jsonHelper;
        this.configurationCreator = configurationCreator;
        this.configInfoSource = configInfoSource;
    }

    @Activate
    void activate() throws Exception {
        PluginConfiguration config = configurationCreator.create(configInfoSource);
        this.gatewayURL = config.getGatewayURL();

        this.httpHelper.startClient(this.client);
    }

    @Override
    public void putStat(final VmTlabStat stat) {
//      TODO: Re-enable when web-gateway service for TLAB stats is available
//      Also see VmTlabStatDAOTest for disabled tests
        return;
//        try {
//            String json = jsonHelper.toJson(Arrays.asList(stat));
//            StringContentProvider provider = httpHelper.createContentProvider(json);
//
//            Request httpRequest = client.newRequest(gatewayURL);
//            httpRequest.method(HttpMethod.POST);
//            httpRequest.content(provider, CONTENT_TYPE);
//            sendRequest(httpRequest);
//        } catch (Exception e) {
//            logger.log(Level.WARNING, "Failed to send VmTlabStat to Web Gateway", e);
//        }
    }

    private void sendRequest(Request httpRequest)
            throws InterruptedException, TimeoutException, ExecutionException, IOException {
        ContentResponse resp = httpRequest.send();
        int status = resp.getStatus();
        if (status != HttpStatus.OK_200) {
            throw new IOException("Gateway returned HTTP status " + String.valueOf(status) + " - " + resp.getReason());
        }
    }

    protected Logger getLogger() {
        return logger;
    }

    static class JsonHelper {

        private final VmTlabStatTypeAdapter adapter;

        public JsonHelper(VmTlabStatTypeAdapter adapter) {
            this.adapter = adapter;
        }

        String toJson(List<VmTlabStat> stats) throws IOException {
            return adapter.toJson(stats);
        }
    }

    // For testing purposes
    static class HttpHelper {

        void startClient(HttpClient httpClient) throws Exception {
            httpClient.start();
        }

        StringContentProvider createContentProvider(String content) {
            return new StringContentProvider(content);
        }

    }

    // For testing purposes
    static class ConfigurationCreator {

        PluginConfiguration create(ConfigurationInfoSource source) {
            return new PluginConfiguration(source, PLUGIN_ID);
        }
    }
}


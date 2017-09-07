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

package com.redhat.thermostat.host.network.internal;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;

import com.redhat.thermostat.agent.http.HttpRequestService;
import com.redhat.thermostat.common.config.experimental.ConfigurationInfoSource;
import com.redhat.thermostat.common.plugin.PluginConfiguration;
import com.redhat.thermostat.common.plugin.PluginDAOBase;
import com.redhat.thermostat.common.plugin.SystemID;
import com.redhat.thermostat.common.utils.LoggingUtils;
import com.redhat.thermostat.host.network.model.NetworkInfoList;
import com.redhat.thermostat.host.network.model.NetworkInfoListTypeAdapter;

@Component
@Service(value = NetworkInfoListDAO.class)
public class NetworkInfoListDAOImpl extends PluginDAOBase<NetworkInfoList> implements NetworkInfoListDAO {

    private static final Logger logger = LoggingUtils.getLogger(NetworkInfoListDAOImpl.class);

    static final String PLUGIN_ID = "host-network";

    private final ConfigurationCreator configCreator;
    private PluginConfiguration config;

    private final JsonHelper jsonHelper;

    @Reference
    private SystemID systemID;

    @Reference
    private HttpRequestService httpRequestService;

    @Reference
    private ConfigurationInfoSource configurationInfoSource;

    public NetworkInfoListDAOImpl() throws Exception {
        this(new JsonHelper(new NetworkInfoListTypeAdapter()), new ConfigurationCreator());
    }

    @Activate
    void activate() throws Exception {
        config = configCreator.create(configurationInfoSource);
    }

    NetworkInfoListDAOImpl(JsonHelper jsonHelper, ConfigurationCreator creator) throws Exception {
        this.jsonHelper = jsonHelper;
        this.configCreator = creator;
    }

    @Override
    protected String toJsonString(NetworkInfoList obj) throws IOException {
        return jsonHelper.toJson(Arrays.asList(obj));
    }

    @Override
    protected HttpRequestService getHttpRequestService() {
        return httpRequestService;
    }

    @Override
    protected PluginConfiguration getConfig() {
        return config;
    }

    @Override
    protected URI getPostURI(URI basepath, NetworkInfoList obj) {
        return basepath.resolve("systems/" + systemID.getSystemID());
    }

    @Override
    protected Logger getLogger() {
        return logger;
    }

    // DS bind method
    void bindSystemID(SystemID systemid) {
        this.systemID = systemid;
    }

    void bindConfigurationInfoSource(ConfigurationInfoSource cfg) {
        this.configurationInfoSource = cfg;
    }

    void bindHttpRequestService(HttpRequestService httpRequestService) {
        this.httpRequestService = httpRequestService;
    }

    // For testing purposes
    static class JsonHelper {
        
        private final NetworkInfoListTypeAdapter typeAdapter;
        
        JsonHelper(NetworkInfoListTypeAdapter typeAdapter) {
            this.typeAdapter = typeAdapter;
        }

        String toJson(List<NetworkInfoList> infos) throws IOException {
            return typeAdapter.toJson(infos);
        }
    }

    // For Testing purposes
    static class ConfigurationCreator {

        PluginConfiguration create(ConfigurationInfoSource source) {
            return new PluginConfiguration(source, PLUGIN_ID);
        }
    }
}


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
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.redhat.thermostat.common.plugin.SystemID;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;

import com.redhat.thermostat.agent.http.HttpRequestService;
import com.redhat.thermostat.agent.http.HttpRequestService.RequestFailedException;
import com.redhat.thermostat.common.config.experimental.ConfigurationInfoSource;
import com.redhat.thermostat.common.plugin.PluginConfiguration;
import com.redhat.thermostat.common.utils.LoggingUtils;
import com.redhat.thermostat.vm.memory.agent.model.VmMemoryStat;

@Component
@Service(value = VmMemoryStatDAO.class)
public class VmMemoryStatDAOImpl implements VmMemoryStatDAO {

    private static final Logger logger = LoggingUtils.getLogger(VmMemoryStatDAOImpl.class);
    private static final String PLUGIN_ID = "vm-memory";

    private final JsonHelper jsonHelper;
    private final ConfigurationCreator configCreator;

    private URI gatewayURL;

    @Reference
    private ConfigurationInfoSource configInfoSource;
    
    @Reference
    private HttpRequestService httpRequestService;

    @Reference
    private SystemID systemID;

    public VmMemoryStatDAOImpl() {
        this(new JsonHelper(new VmMemoryStatTypeAdapter()), new ConfigurationCreator(), null);
    }

    VmMemoryStatDAOImpl(JsonHelper jh, ConfigurationCreator creator, ConfigurationInfoSource source) {
        this.jsonHelper = jh;
        this.configCreator = creator;
        this.configInfoSource = source;
    }

    @Activate
    void activate() throws Exception {
        PluginConfiguration config = configCreator.create(configInfoSource);
        this.gatewayURL = config.getGatewayURL();
    }

    void bindSystemID(final SystemID id) {
        this.systemID = id;
    }

    @Override
    public void putVmMemoryStat(final VmMemoryStat stat) {
        try {
            String json = jsonHelper.toJson(Arrays.asList(stat));
            httpRequestService.sendHttpRequest(json, getPostURI(stat.getJvmId()), HttpRequestService.Method.POST);
        } catch (RequestFailedException | IOException e) {
            logger.log(Level.WARNING, "Failed to send VmMemoryStat to Web Gateway", e);
        }
    }

    protected URI getPostURI(final String jvmID) {
        return gatewayURL.resolve("systems/" + systemID.getSystemID() + "/jvms/" + jvmID);
    }

    protected Logger getLogger() {
        return logger;
    }
    
    protected void bindHttpRequestService(HttpRequestService httpRequestService) {
        this.httpRequestService = httpRequestService;
    }

    protected void unbindHttpRequestService(HttpRequestService httpRequestService) {
        this.httpRequestService = null;
        logger.log(Level.INFO, "Unbound HTTP service. Further attempts to store data will fail until bound again.");
    }

    // For testing purposes
    static class JsonHelper {

        private final VmMemoryStatTypeAdapter adapter;

        public JsonHelper(VmMemoryStatTypeAdapter adapter) {
            this.adapter = adapter;
        }

        String toJson(List<VmMemoryStat> stats) throws IOException {
            return adapter.toJson(stats);
        }
    }

    // For Testing purposes
    static class ConfigurationCreator {

        PluginConfiguration create(ConfigurationInfoSource source) {
            return new PluginConfiguration(source, PLUGIN_ID);
        }

    }
}


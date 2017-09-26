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

package com.redhat.thermostat.vm.byteman.agent.internal;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.redhat.thermostat.agent.http.HttpRequestService;
import com.redhat.thermostat.common.plugin.PluginConfiguration;
import com.redhat.thermostat.common.plugin.PluginDAOBase;
import com.redhat.thermostat.common.plugin.SystemID;
import com.redhat.thermostat.common.utils.LoggingUtils;
import com.redhat.thermostat.vm.byteman.agent.BytemanMetric;

class VmBytemanMetricsStore extends PluginDAOBase<BytemanMetric> {

    private static final String METRICS_PATH = "metrics";
    private static final Logger LOGGER = LoggingUtils.getLogger(VmBytemanMetricsStore.class);
    private final HttpRequestService httpRequestService;
    private final PluginConfiguration pluginConfig;
    private final SystemID systemId;
    private final Gson gson;

    VmBytemanMetricsStore(HttpRequestService httpRequestService,
                          PluginConfiguration pluginConfig,
                          SystemID systemId,
                          Gson gson) {
        this.httpRequestService = httpRequestService;
        this.pluginConfig = pluginConfig;
        this.systemId = systemId;
        this.gson = gson;
    }

    @Override
    protected String toJsonString(BytemanMetric obj) throws IOException {
        List<BytemanMetric> bytemanMetrics = Arrays.asList(obj);
        return gson.toJson(bytemanMetrics);
    }

    @Override
    protected HttpRequestService getHttpRequestService() {
        return httpRequestService;
    }

    @Override
    protected PluginConfiguration getConfig() {
        return pluginConfig;
    }

    @Override
    protected URI getPostURI(URI basepath, BytemanMetric obj) {
        return basepath.resolve(METRICS_PATH + "/systems/" + systemId.getSystemID() + "/jvms/" + obj.getJvmId());
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

}

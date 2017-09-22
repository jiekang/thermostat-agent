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

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;

import com.google.gson.Gson;
import com.redhat.thermostat.agent.http.HttpRequestService;
import com.redhat.thermostat.common.config.experimental.ConfigurationInfoSource;
import com.redhat.thermostat.common.plugin.PluginConfiguration;
import com.redhat.thermostat.common.plugin.SystemID;
import com.redhat.thermostat.vm.byteman.agent.BytemanMetric;
import com.redhat.thermostat.vm.byteman.agent.VmBytemanDAO;
import com.redhat.thermostat.vm.byteman.agent.VmBytemanStatus;
import com.redhat.thermostat.vm.byteman.agent.internal.typeadapters.GsonCreator;

@Component
@Service(value = VmBytemanDAO.class)
public class VmBytemanDAOImpl implements VmBytemanDAO {

    private static final String PLUGIN_ID = "vm-byteman";

    private final ConfigurationCreator configCreator;
    private final GsonCreator gsonCreator;

    @Reference
    private HttpRequestService httpRequestService;
    @Reference
    private ConfigurationInfoSource configInfoSource;
    @Reference
    private SystemID systemId;

    private VmBytemanMetricsStore metricsStore;

    private VmBytemanStatusStore statusStore;

    public VmBytemanDAOImpl() {
        this(new ConfigurationCreator(), new GsonCreator());
    }

    VmBytemanDAOImpl(ConfigurationCreator configCreator, GsonCreator creator) {
        this.configCreator = configCreator;
        this.gsonCreator = creator;
    }

    @Activate
    private void activate() {
        PluginConfiguration config = configCreator.create(configInfoSource);
        Gson gson = gsonCreator.create();
        metricsStore = new VmBytemanMetricsStore(httpRequestService, config, systemId, gson);
        statusStore = new VmBytemanStatusStore(httpRequestService, config, systemId, gson);
    }

    @Override
    public void addMetric(final BytemanMetric metric) {
        metricsStore.put(metric);
    }

    @Override
    public void addBytemanStatus(final VmBytemanStatus status) {
        statusStore.put(status);
    }

    // For Testing purposes
    static class ConfigurationCreator {

        PluginConfiguration create(ConfigurationInfoSource source) {
            return new PluginConfiguration(source, PLUGIN_ID);
        }

    }

}

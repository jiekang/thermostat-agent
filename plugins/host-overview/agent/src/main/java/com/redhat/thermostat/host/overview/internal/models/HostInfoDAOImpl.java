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
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.util.StringContentProvider;

import com.redhat.thermostat.common.config.experimental.ConfigurationInfoSource;
import com.redhat.thermostat.common.utils.LoggingUtils;
import com.redhat.thermostat.host.overview.internal.HostInfoTypeAdapter;
import com.redhat.thermostat.host.overview.internal.common.PluginConfiguration;
import com.redhat.thermostat.host.overview.internal.common.PluginDAOBase;
import com.redhat.thermostat.host.overview.model.HostInfo;

@Component
@Service(value = HostInfoDAO.class)
public class HostInfoDAOImpl extends PluginDAOBase<HostInfo, HostInfoDAOImpl> implements HostInfoDAO {
    
    private static final Logger logger = LoggingUtils.getLogger(HostInfoDAOImpl.class);
    
    public static final String PLUGIN_ID = "host-overview";

    private final JsonHelper jsonHelper;
    private final HttpHelper httpHelper;

    private final ConfigurationCreator configCreator;
    
    @Reference
    private ConfigurationInfoSource configInfoSource;
    private PluginConfiguration config;

    public HostInfoDAOImpl() {
        this(new HttpClient(), new JsonHelper(new HostInfoTypeAdapter()), new HttpHelper(), 
                new ConfigurationCreator(), null);
    }

    HostInfoDAOImpl(HttpClient client, JsonHelper jh, HttpHelper hh, ConfigurationCreator creator, 
            ConfigurationInfoSource source) {
        super(client);
        this.jsonHelper = jh;
        this.httpHelper = hh;
        this.configCreator = creator;
        this.configInfoSource = source;
    }

    @Activate
    void activate() throws Exception {
        this.config = configCreator.create(configInfoSource);
        httpHelper.startClient(httpClient);
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
    
    @Override
    protected PluginConfiguration getConfig() {
    	return config;
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
        
        void startClient(HttpClient httpClient) throws Exception {
            httpClient.start();
        }
        
        StringContentProvider createContentProvider(String content) {
            return new StringContentProvider(content);
        }
        
    }
    
    // For Testing purposes
    static class ConfigurationCreator {
        
        PluginConfiguration create(ConfigurationInfoSource source) {
            return new PluginConfiguration(source, PLUGIN_ID);
        }
        
    }
    
}


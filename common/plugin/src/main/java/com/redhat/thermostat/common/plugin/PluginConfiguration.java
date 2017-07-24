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

package com.redhat.thermostat.common.plugin;

import com.redhat.thermostat.common.config.experimental.ConfigurationInfoSource;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

public class PluginConfiguration {

    private static final String CONFIG_FILE = "gateway.properties";
    private static final String URL_PROP = "gatewayURL";

    private final ConfigurationInfoSource source;
    private final String pluginId;

    public PluginConfiguration(ConfigurationInfoSource source, final String pluginId) {
        this.source = source;
        this.pluginId = pluginId;
    }

    /**
     * Returns the microservice URL defined by the "gatewayURL" property in the gateway.properties file
     * for this plugin, expressed as a {@link URI}. If the provided URL does not end in a '/' character, 
     * one is appended by this method.
     * <p>
     * Since the microservice path is already included, appending to this URI should be done using 
     * the {@link URI#resolve(String)} method with a relative path.
     * @return a URI to the web gateway microservice used by this plugin
     * @throws IOException if the gatewayURL property is missing or invalid
     */
    public URI getGatewayURL() throws IOException {
        Map<String, String> props = source.getConfiguration(pluginId, CONFIG_FILE);
        String url = props.get(URL_PROP);
        if (url == null) {
            throw new IOException("No gateway URL found for " + pluginId + " in " + getConfigFilePath());
        }
        try {
            // Ensure the URI ends with a '/' so relative paths resolve under the microservice path segment
            if (!url.endsWith("/")) {
                url = url.concat("/");
            }
            URI gatewayURI = new URI(url);
            return gatewayURI;
        } catch (URISyntaxException e) {
            throw new IOException("Invalid URL found for " + pluginId + ": " + url, e);
        }
    }

    private String getConfigFilePath() {
        StringBuilder builder = new StringBuilder();
        builder.append("$THERMOSTAT_HOME").append(File.separator).append("etc").append(File.separator)
                .append("plugins.d").append(File.separator).append(pluginId).append(File.separator)
                .append(CONFIG_FILE);
        return builder.toString();
    }
}
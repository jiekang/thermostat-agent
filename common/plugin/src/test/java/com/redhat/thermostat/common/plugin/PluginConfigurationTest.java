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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.redhat.thermostat.common.config.experimental.ConfigurationInfoSource;

public class PluginConfigurationTest {

    private static final String PLUGIN_ID = "vm-gc";
    private static final String CONFIG_FILE = "gateway.properties";
    private static final String URL_PROP = "gatewayURL";

    @Test
    public void testGetGatewayURL() throws Exception {
        ConfigurationInfoSource source = mock(ConfigurationInfoSource.class);
        Map<String, String> props = new HashMap<>();
        props.put(URL_PROP, "urlToGateway/");
        when(source.getConfiguration(PLUGIN_ID, CONFIG_FILE)).thenReturn(props);
        PluginConfiguration config = new PluginConfiguration(source, PLUGIN_ID);

        assertEquals(URI.create("urlToGateway/"), config.getGatewayURL());
    }
    
    @Test
    public void testGetGatewayURLNoSlash() throws Exception {
        ConfigurationInfoSource source = mock(ConfigurationInfoSource.class);
        Map<String, String> props = new HashMap<>();
        props.put(URL_PROP, "urlToGateway");
        when(source.getConfiguration(PLUGIN_ID, CONFIG_FILE)).thenReturn(props);
        PluginConfiguration config = new PluginConfiguration(source, PLUGIN_ID);

        assertEquals(URI.create("urlToGateway/"), config.getGatewayURL());
    }

    @Test(expected=IOException.class)
    public void testGetGatewayURLMissing() throws Exception {
        ConfigurationInfoSource source = mock(ConfigurationInfoSource.class);
        Map<String, String> props = new HashMap<>();
        when(source.getConfiguration(PLUGIN_ID, CONFIG_FILE)).thenReturn(props);
        PluginConfiguration config = new PluginConfiguration(source, PLUGIN_ID);
        config.getGatewayURL();
    }
}

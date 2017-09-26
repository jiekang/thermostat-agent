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

package com.redhat.thermostat.agent.internal.http;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.redhat.thermostat.agent.config.AgentConfigsUtils;
import com.redhat.thermostat.agent.config.AgentStartupConfiguration;
import com.redhat.thermostat.common.utils.LoggingUtils;
import com.redhat.thermostat.shared.config.CommonPaths;
import com.redhat.thermostat.shared.config.SSLConfiguration;
import com.redhat.thermostat.storage.config.FileStorageCredentials;
import com.redhat.thermostat.storage.core.StorageCredentials;

class BasicHttpService {

    private static final Logger logger = LoggingUtils.getLogger(BasicHttpService.class);
    protected final CredentialsCreator credsCreator;
    protected final HttpClientCreator httpClientCreator;
    protected final ConfigCreator configCreator;
    protected HttpClientFacade client;
    protected AgentStartupConfiguration agentStartupConfiguration;
    protected StorageCredentials creds;

    BasicHttpService() {
        this(new HttpClientCreator(), new ConfigCreator(), new CredentialsCreator());
    }

    BasicHttpService(HttpClientCreator clientCreator, ConfigCreator configCreator, CredentialsCreator credsCreator) {
        this.httpClientCreator = clientCreator;
        this.configCreator = configCreator;
        this.credsCreator = credsCreator;
    }

    protected void doActivate(CommonPaths commonPaths, SSLConfiguration sslConfig, final String serviceName) {
        try {
            agentStartupConfiguration = configCreator.create(commonPaths);
            client = httpClientCreator.create(sslConfig);
            creds = credsCreator.create(commonPaths);
            client.start();
            logger.log(Level.FINE, serviceName + " activated");
        } catch (Exception e) {
            logger.log(Level.SEVERE, serviceName + " failed to start correctly. Behaviour undefined.", e);
        }
    }

    static class ConfigCreator {
        AgentStartupConfiguration create(CommonPaths commonPaths) {
            AgentConfigsUtils.setConfigFiles(commonPaths.getSystemAgentConfigurationFile(),
                    commonPaths.getUserAgentConfigurationFile());
            return AgentConfigsUtils.createAgentConfigs();
        }
    }

    static class CredentialsCreator {
        StorageCredentials create(CommonPaths paths) {
            return new FileStorageCredentials(paths.getUserAgentAuthConfigFile());
        }
    }

    static class HttpClientCreator {

        HttpClientFacade create(SSLConfiguration config) {
            return HttpClientFacadeFactory.getInstance(config);
        }

    }
}

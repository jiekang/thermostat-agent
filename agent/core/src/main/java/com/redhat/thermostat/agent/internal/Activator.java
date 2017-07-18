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

package com.redhat.thermostat.agent.internal;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import com.redhat.thermostat.agent.config.AgentConfigsUtils;
import com.redhat.thermostat.agent.dao.AgentInfoDAO;
import com.redhat.thermostat.agent.dao.BackendInfoDAO;
import com.redhat.thermostat.common.utils.LoggingUtils;
import com.redhat.thermostat.shared.config.CommonPaths;
import com.redhat.thermostat.shared.config.InvalidConfigurationException;

public class Activator implements BundleActivator {
    
    private static final Logger logger = LoggingUtils.getLogger(Activator.class);
    
    private final AgentConfigSetter configSetter;
    private ServiceTracker<CommonPaths, CommonPaths> commonPathsTracker;
    
    public Activator() {
        this(new AgentConfigSetter());
    }
    
    Activator(AgentConfigSetter configSetter) {
        this.configSetter = configSetter;
    }

    @Override
    public void start(final BundleContext context) throws Exception {
        AgentInfoDAO agentInfoDAO = new AgentInfoDAOImpl();
        context.registerService(AgentInfoDAO.class, agentInfoDAO, null);

        BackendInfoDAO backendInfoDAO = new BackendInfoDAOImpl();
        context.registerService(BackendInfoDAO.class, backendInfoDAO, null);

        // Track common paths separately and register storage credentials quickly
        // We need to do this since otherwise no storage credentials will be
        // available by the time they're used in DbService
        commonPathsTracker = new ServiceTracker<>(context, CommonPaths.class, new ServiceTrackerCustomizer<CommonPaths, CommonPaths>() {

            @Override
            public CommonPaths addingService(ServiceReference<CommonPaths> ref) {
                CommonPaths paths = context.getService(ref);
                try {
                    configSetter.setConfigFiles(paths.getSystemAgentConfigurationFile(), paths.getUserAgentConfigurationFile());
                } catch (InvalidConfigurationException e) {
                    logger.log(Level.SEVERE, "Failed to start agent services", e);
                }
                return paths;
            }

            @Override
            public void modifiedService(ServiceReference<CommonPaths> arg0, CommonPaths arg1) {
                // nothing
            }

            @Override
            public void removedService(ServiceReference<CommonPaths> ref, CommonPaths service) {
                context.ungetService(ref);
            }
        });
        
        commonPathsTracker.open();
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        commonPathsTracker.close();
    }
    
    // For testing purposes
    static class AgentConfigSetter {
        void setConfigFiles(File systemConfigFile, File userConfigFile) {
            AgentConfigsUtils.setConfigFiles(systemConfigFile, userConfigFile);
        }
    }

}


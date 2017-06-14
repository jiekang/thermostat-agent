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

package com.redhat.thermostat.vm.memory.common.internal;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;

import com.redhat.thermostat.common.config.experimental.ConfigurationInfoSource;
import com.redhat.thermostat.common.plugins.PluginConfiguration;
import com.redhat.thermostat.common.utils.LoggingUtils;
import com.redhat.thermostat.vm.memory.common.VmMemoryStatDAO;
import com.redhat.thermostat.vm.memory.common.VmTlabStatDAO;

public class Activator implements BundleActivator {
    
    private static final Logger logger = LoggingUtils.getLogger(Activator.class);
    private static final String PLUGIN_ID = "vm-memory";
    
    private final DAOCreator creator;
    private ServiceTracker tracker;
    private ServiceRegistration memoryReg;
    private ServiceRegistration tlabReg;
    
    public Activator() {
        this(new DAOCreator());
    }
    
    Activator(DAOCreator creator) {
        this.creator = creator;
    }

    @Override
    public void start(BundleContext context) throws Exception {
        tracker = new ServiceTracker(context, ConfigurationInfoSource.class.getName(), null) {
            @Override
            public Object addingService(ServiceReference reference) {
                ConfigurationInfoSource source = (ConfigurationInfoSource) super.addingService(reference);
                PluginConfiguration config = new PluginConfiguration(source, PLUGIN_ID);
                try {
                    VmMemoryStatDAO vmMemoryStatDao = creator.createMemoryStatDAO(config);
                    memoryReg = context.registerService(VmMemoryStatDAO.class.getName(), vmMemoryStatDao, null);
                    VmTlabStatDAO vmTlabStatDao = creator.createTlabStatDAO(config);
                    tlabReg = context.registerService(VmTlabStatDAO.class.getName(), vmTlabStatDao, null);
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Failed to create DAOs", e);
                }
                return source;
            }
            @Override
            public void removedService(ServiceReference reference, Object service) {
                if (memoryReg != null) {
                    memoryReg.unregister();
                }
                if (tlabReg != null) {
                    tlabReg.unregister();
                }
                super.removedService(reference, service);
            }
        };
        tracker.open();
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        tracker.close();
    }
    
    static class DAOCreator {
        VmMemoryStatDAO createMemoryStatDAO(PluginConfiguration config) throws Exception {
            return new VmMemoryStatDAOImpl(config);
        }
        VmTlabStatDAO createTlabStatDAO(PluginConfiguration config) throws Exception {
            return new VmTlabStatDAOImpl(config);
        }
    }

}


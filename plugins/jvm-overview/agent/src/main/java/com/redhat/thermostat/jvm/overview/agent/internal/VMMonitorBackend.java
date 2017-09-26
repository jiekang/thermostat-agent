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

package com.redhat.thermostat.jvm.overview.agent.internal;

import com.redhat.thermostat.backend.Backend;
import com.redhat.thermostat.backend.BaseBackend;
import com.redhat.thermostat.common.portability.ProcessUserInfoBuilder;
import com.redhat.thermostat.common.portability.ProcessUserInfoBuilderFactory;
import com.redhat.thermostat.common.utils.LoggingUtils;
import com.redhat.thermostat.jvm.overview.agent.VmBlacklist;
import com.redhat.thermostat.jvm.overview.agent.internal.model.VmInfoDAO;
import com.redhat.thermostat.jvm.overview.agent.internal.model.VmMapperServiceImpl;
import com.redhat.thermostat.jvm.overview.agent.model.VmMapperService;
import com.redhat.thermostat.storage.core.WriterID;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import sun.jvmstat.monitor.HostIdentifier;
import sun.jvmstat.monitor.MonitorException;
import sun.jvmstat.monitor.MonitoredHost;

import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 */
@Component
@Service(value = Backend.class)
public class VMMonitorBackend extends BaseBackend {
    private static final Logger logger = LoggingUtils.getLogger(VMMonitorBackend.class);

    @Reference
    private VmInfoDAO vmInfoDAO;

    private VmStatusChangeNotifier notifier;

    @Reference
    private WriterID writerId;

    @Reference
    private VmBlacklist blacklist;

    private volatile boolean active;

    private MonitoredHost host;
    private JvmStatHostListener hostListener;
    private VmMapperServiceImpl vmMapperService;
    private ServiceRegistration<VmMapperService> mapperRegistration;

    public VMMonitorBackend() {
        super("VM Basic Monitor Backend",
              "Monitor the system for JVM processes",
              "Red Hat, Inc.",
              "0.1");
    }

    @Activate
    private void _activate_(BundleContext context) {

        vmMapperService = new VmMapperServiceImpl();
        mapperRegistration = context.registerService(VmMapperService.class, vmMapperService, null);

        notifier = new VmStatusChangeNotifier(context, vmMapperService);
        notifier.start();
    }

    @Deactivate
    private void _deactivate_() {
        notifier.stop();
        mapperRegistration.unregister();
    }

    @Override
    public synchronized boolean activate() {
        try {
            ProcessUserInfoBuilder userInfoBuilder = ProcessUserInfoBuilderFactory.createBuilder();
            hostListener = new JvmStatHostListener(vmInfoDAO, notifier,
                                                   userInfoBuilder, writerId, blacklist);
            HostIdentifier hostId = new HostIdentifier((String) null);
            host = MonitoredHost.getMonitoredHost(hostId);
            host.addHostListener(hostListener);
            active = true;

        } catch (MonitorException | URISyntaxException me) {
            logger.log(Level.WARNING, "problems with connecting jvmstat to local machine", me);
        }

        return active;
    }

    @Override
    public synchronized boolean deactivate() {
        try {
            host.removeHostListener(hostListener);
            active = false;
        } catch (MonitorException me) {
            logger.log(Level.INFO, "something went wrong in jvmstat's listening to this host");
        }

        return !active;
    }

    @Override
    public synchronized boolean isActive() {
        return active;
    }
}

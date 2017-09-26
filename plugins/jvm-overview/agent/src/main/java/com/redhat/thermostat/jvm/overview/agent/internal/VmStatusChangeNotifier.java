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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import com.redhat.thermostat.jvm.overview.agent.VmStatusListener;
import com.redhat.thermostat.jvm.overview.agent.VmStatusListener.Status;
import com.redhat.thermostat.jvm.overview.agent.internal.model.VmMapperServiceImpl;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

/**
 * Notifies any and all {@link com.redhat.thermostat.jvm.overview.agent.VmStatusListener} registered as OSGi Services
 * about VM status changes: {@link com.redhat.thermostat.jvm.overview.agent.VmStatusListener.Status#VM_STARTED} and
 * {@link com.redhat.thermostat.jvm.overview.agent.VmStatusListener.Status#VM_STOPPED}.
 * <p>
 * Any listeners registered after a {@link com.redhat.thermostat.jvm.overview.agent.VmStatusListener.Status#VM_STARTED}
 * was delivered receive a {@link com.redhat.thermostat.jvm.overview.agent.VmStatusListener.Status#VM_ACTIVE} event
 * instead as an indication that a VM was started at some unknown point
 * previously.
 */
public class VmStatusChangeNotifier {

    private final Object listenerLock = new Object();
    private final Map<Integer, String> activePids;
    private final Map<VmStatusListener, Set<Integer>> listeners = new ConcurrentHashMap<>();

    private final ServiceTracker tracker;
    private VmMapperServiceImpl vmMapperService;

    public VmStatusChangeNotifier(BundleContext bundleContext, VmMapperServiceImpl vmMapperService) {

        this.vmMapperService = vmMapperService;

        this.activePids = new HashMap<>();

        tracker = new ServiceTracker(bundleContext, VmStatusListener.class, null) {
            @Override
            public VmStatusListener addingService(ServiceReference reference) {
                VmStatusListener listener = (VmStatusListener) super.addingService(reference);

                synchronized (listenerLock) {
                    Set<Integer> notifiedAbout = new TreeSet<>();
                    for (Entry<Integer, String> entry : activePids.entrySet()) {
                        Integer pid = entry.getKey();
                        listener.vmStatusChanged(Status.VM_ACTIVE, entry.getValue(), pid);
                        notifiedAbout.add(pid);
                    }

                    listeners.put(listener, notifiedAbout);
                }

                return listener;
            }

            @Override
            public void removedService(ServiceReference reference,
                    Object service) {
                VmStatusListener listener = (VmStatusListener) service;
                listeners.remove(listener);
                super.removedService(reference, service);
            }
        };
    }

    public void start() {
        tracker.open();
    }

    public void stop() {
        tracker.close();
    }

    /**
     * Notify all registered listeners about a Vm status change.
     *
     * @param newStatus either {@link VmStatusListener.Status#VM_STARTED} or
     * {@link VmStatusListener.Status#VM_STOPPED}
     * @param vmId unique identifier for the VM
     * @param pid process ID for the VM
     */
    public void notifyVmStatusChange(VmStatusListener.Status newStatus, String vmId, int pid) {
        if (newStatus == Status.VM_ACTIVE) {
            throw new IllegalArgumentException("Dont pass in " + Status.VM_ACTIVE + ", that will be handled automatically");
        }

        synchronized (listenerLock) {
            for (Entry<VmStatusListener, Set<Integer>> entry : listeners.entrySet()) {
                entry.getKey().vmStatusChanged(newStatus, vmId, pid);
                entry.getValue().add(pid);
            }

            if (newStatus == Status.VM_STARTED) {
                activePids.put(pid, vmId);
                vmMapperService.cache(vmId, pid);
            } else {
                activePids.remove(pid);
            }
        }
    }

}


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

package com.redhat.thermostat.jvm.overview.agent;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.redhat.thermostat.backend.BaseBackend;
import com.redhat.thermostat.backend.BackendException;
import com.redhat.thermostat.common.utils.LoggingUtils;
import com.redhat.thermostat.storage.core.WriterID;

/**
 * This class is a convenient subclass of {@link com.redhat.thermostat.backend.Backend} (via {@link BaseBackend}) for those
 * that need to attach {@link VmUpdateListener} in response to starting and stopping of JVMs on a
 * host.
 * 
 * @see package com.redhat.thermostat.jvm.overview.agent.VmStatusListener
 * @see com.redhat.thermostat.backend.Backend
 * @see BaseBackend
 */
public abstract class VmListenerBackend extends BaseBackend implements VmStatusListener {
    
    private static final Logger logger = LoggingUtils.getLogger(VmListenerBackend.class);
    
    private WriterID writerId;
    private VmStatusListenerRegistrar registrar;
    private VmMonitor monitor;
    private boolean started;
    private boolean initialized;

    public VmListenerBackend(String backendName, String description, String vendor, boolean observeNewJvm) {
        this(backendName, description, vendor, "1.0", observeNewJvm);
    }

    public VmListenerBackend(String backendName, String description, String vendor) {
        this(backendName, description, vendor, "1.0", false);
    }

    public VmListenerBackend(String backendName, String description,
            String vendor, String version, boolean observeNewJvm) {
        super(backendName, description, vendor, version, observeNewJvm);
        try {
            this.monitor = new VmMonitor();
        } catch (BackendException e) {
            logger.log(Level.SEVERE, "Unable to create backend", e);
        }
    }
    
    /**
     * {@inheritDoc}
     * 
     * <p>
     * Registers a VmUpdateListener to begin receiving VM lifecycle events.
     * Subclasses should call <code>super.activate()</code> when overriding this method.
     * </p>
     * <p>
     * This {@link VmListenerBackend} should be initialized via 
     * {@link #initialize(WriterID, VmStatusListenerRegistrar)} before calling this method.
     * </p>
     */
    @Override
    public boolean activate() {
        if (!initialized) {
            logger.warning("Backend not started, initialize must be called before activate");
        } else if (!started && monitor != null) {
            registrar.register(this);
            started = true;
        }
        return started;
    }
    
    /**
     * {@inheritDoc}
     * 
     * <p>
     * Unregisters the VmUpdateListener to stop receiving VM lifecycle events.
     * Subclasses should call <code>super.deactivate()</code> when overriding this method.
     * </p>
     */
    @Override
    public boolean deactivate() {
        if (started && monitor != null) {
            registrar.unregister(this);
            monitor.removeVmListeners();
            started = false;
        }
        return !started;
    }
    
    @Override
    public boolean isActive() {
        return started;
    }

    public void vmStatusChanged(Status newStatus, String vmId, int pid) {
        switch (newStatus) {
        case VM_STARTED:
            /* fall-through */
        case VM_ACTIVE:
            if (getObserveNewJvm()) {
                String wId = writerId.getWriterID();
                VmUpdateListener listener = null;
                try {
                    listener = createVmListener(wId, vmId, pid);
                } catch (Throwable t) {
                    logger.log(Level.INFO, "Creating the VM listener for a VmListenerBackend threw an exception. Going to ignore the backend!", t);
                }
                if (listener != null) {
                    monitor.handleNewVm(listener, pid);
                }
            } else {
                logger.log(Level.FINE, "skipping new vm " + pid);
            }
            break;
        case VM_STOPPED:
            monitor.handleStoppedVm(pid);
            break;
        default:
            break;
        }
    }

    /**
     * Initializes this {@link VmListenerBackend} with necessary services. This method
     * must be called before {@link #activate()}.
     * @param writerId service uniquely identifying this agent
     * @param registrar responsible for registering and unregistering instances of {@link VmStatusListener}
     * @param version version number of this backend
     */
    protected void initialize(WriterID writerId, VmStatusListenerRegistrar registrar, String version) {
        this.writerId = writerId;
        this.registrar = registrar;
        setVersion(version);
        this.initialized = true;
    }
    
    /**
     * Creates a new {@link VmUpdateListener} for the virtual machine
     * specified by the pid. This method is called when a new
     * JVM is started or for JVMs already active when this Backend
     * was activated.
     * @param vmId unique identifier of the JVM
     * @param pid the process ID of the JVM
     * @return a new listener for the VM specified by pid
     */
    protected abstract VmUpdateListener createVmListener(String writerId, String vmId, int pid);
    
    /*
     * For testing purposes only.
     */
    void setMonitor(VmMonitor monitor) {
        this.monitor = monitor;
    }
}


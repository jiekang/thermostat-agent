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

package com.redhat.thermostat.vm.io.agent.internal;

import com.redhat.thermostat.backend.Backend;
import com.redhat.thermostat.common.SystemClock;
import com.redhat.thermostat.common.Version;
import com.redhat.thermostat.jvm.overview.agent.VmListenerBackend;
import com.redhat.thermostat.jvm.overview.agent.VmStatusListenerRegistrar;
import com.redhat.thermostat.jvm.overview.agent.VmUpdate;
import com.redhat.thermostat.jvm.overview.agent.VmUpdateListener;
import com.redhat.thermostat.storage.core.WriterID;
import com.redhat.thermostat.vm.io.model.VmIoStat;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.framework.BundleContext;

@Component
@Service(value = Backend.class)
public class VmIoBackend extends VmListenerBackend {
    
    private final ListenerCreator listenerCreator;

    @Reference
    private VmIoStatDAO vmIoStatDAO;

    @Reference
    private WriterID writerId;

    private VmIoStatBuilder builder;

    public VmIoBackend() {

        this(new ListenerCreator());
    }

    VmIoBackend(ListenerCreator listenerCreator) {
        super("VM IO Backend",
              "Gathers IO statistics about a JVM",
              "Red Hat, Inc.",
              true);
        this.listenerCreator = listenerCreator;
    }

    @Activate
    protected void componentActivated(BundleContext context) {
        VmStatusListenerRegistrar registrar = new VmStatusListenerRegistrar(context);
        Version version = new Version(context.getBundle());
        this.builder = new VmIoStatBuilderImpl(new SystemClock(), writerId);
        initialize(writerId, registrar, version.getVersionNumber());
    }

    @Deactivate
    protected void componentDeactivated() {
        if (isActive()) {
            deactivate();
        }
    }

    // DS bind method
    void bindVVmIoStatDAO(VmIoStatDAO dao) {
        this.vmIoStatDAO = dao;
    }

    // DS bind method
    void bindWriterId(WriterID id) {
        this.writerId = id;
    }

    @Override
    protected VmUpdateListener createVmListener(String writerId, String jvmId, int pid) {
        return listenerCreator.create(vmIoStatDAO, builder, jvmId, pid);
    }

    private static class VmIoBackendListener implements VmUpdateListener {
        private VmIoStatDAO vmIoStatDAO;
        private VmIoStatBuilder builder;
        private String jvmId;
        private int pid;

        VmIoBackendListener(VmIoStatDAO vmIoStatDAO, VmIoStatBuilder builder, String jvmId, int pid) {
            this.vmIoStatDAO = vmIoStatDAO;
            this.builder = builder;
            this.jvmId = jvmId;
            this.pid = pid;
        }

        @Override
        public void countersUpdated(VmUpdate update) {
            VmIoStat dataBuilt = builder.build(jvmId, pid);
            if (dataBuilt != null) {
                vmIoStatDAO.put(dataBuilt);
            }
        }
    }
    
    // For testing purposes
    static class ListenerCreator {
        VmIoBackendListener create(VmIoStatDAO dao, VmIoStatBuilder builder,  String jvmId, int pid) {
            return new VmIoBackendListener(dao, builder, jvmId, pid);
        }
    }
}

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

package com.redhat.thermostat.host.cpu.agent.internal;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import com.redhat.thermostat.backend.Backend;
import com.redhat.thermostat.backend.HostPollingAction;
import com.redhat.thermostat.backend.HostPollingBackend;
import com.redhat.thermostat.common.Version;
import com.redhat.thermostat.host.cpu.model.CpuStat;
import com.redhat.thermostat.storage.core.WriterID;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.framework.BundleContext;

@Component
@Service(value = Backend.class)
public class HostCpuBackend extends HostPollingBackend {

    @Reference
    private CpuStatDAO cpuStatDAO;

    @Reference
    private WriterID writerID;

    public HostCpuBackend() {
        this(Executors.newSingleThreadScheduledExecutor());
    }

    public HostCpuBackend(ScheduledExecutorService executor) {
        this("Host CPU Backend", "Gathers CPU statistics about a host", "Red Hat, Inc.", new Version(), executor);
    }

    public HostCpuBackend(String name, String descr, String vendor, Version version, ScheduledExecutorService executor) {
        super(name, descr, vendor, version, executor);
    }

    @Activate
    protected void componentActivated(BundleContext context) {
        Version version = new Version(context.getBundle());
        setVersion(version.getVersionNumber());
        registerAction(new CpuProcBackendAction(writerID, cpuStatDAO));
    }

    @Deactivate
    protected void componentDeactivated() {
        if (isActive()) {
            deactivate();
        }
    }

    private static class CpuProcBackendAction implements HostPollingAction {

        private final CpuStatBuilder builder;
        private final CpuStatDAO dao;

        CpuProcBackendAction(final WriterID id, final CpuStatDAO dao) {
            this.builder = new CpuStatBuilderFactory().build(id);
            this.dao = dao;
        }

        @Override
        public void run() {
            if (!builder.isInitialized()) {
                builder.initialize();
            } else {
                final CpuStat info = builder.build();
                dao.put(info);
            }
        }
    }

    void bindCpuStatDAO(CpuStatDAO dao) {
        this.cpuStatDAO = dao;
    }

    void bindWriterID(WriterID id) {
        this.writerID = id;
    }

    @Override
    public int getOrderValue() {
        return ORDER_CPU_GROUP;
    }

}


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

package com.redhat.thermostat.host.overview.internal;

import com.redhat.thermostat.host.overview.internal.models.HostInfoBuilderImpl;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.framework.BundleContext;

import com.redhat.thermostat.backend.Backend;
import com.redhat.thermostat.backend.BaseBackend;
import com.redhat.thermostat.common.Version;
import com.redhat.thermostat.host.overview.internal.models.HostInfoBuilder;
import com.redhat.thermostat.host.overview.internal.models.HostInfoDAO;
import com.redhat.thermostat.host.overview.model.HostInfo;
import com.redhat.thermostat.storage.core.WriterID;

@Component
@Service(value = Backend.class)
public class HostOverviewBackend extends BaseBackend {
    
    private final HostInfoBuilderCreator builderCreator;
    
    @Reference
    private HostInfoDAO hostInfoDAO;
    @Reference
    private WriterID writerID;
    
    private boolean started;

    public HostOverviewBackend() {
        this(new HostInfoBuilderCreator());
    }

    HostOverviewBackend(HostInfoBuilderCreator builderCreator) {
        super("Host Overview Backend", "Gathers general information about a host", "Red Hat, Inc.");
        this.builderCreator = builderCreator;
    }

    @Override
    public boolean activate() {
        HostInfoBuilder builder = builderCreator.create(writerID);
        HostInfo hostInfo = builder.build();
        hostInfoDAO.put(hostInfo.getHostname(), hostInfo);
        started = true;
        return true;
    }

    @Override
    public boolean deactivate() {
        started = false;
        return true;
    }

    @Override
    public boolean isActive() {
        return started;
    }

    @Override
    public int getOrderValue() {
        return ORDER_DEFAULT_GROUP;
    }
    
    // For testing purposes
    static class HostInfoBuilderCreator {
        HostInfoBuilder create(WriterID writerID) {
            return new HostInfoBuilderImpl(writerID);
        }
    }
    
    @Activate
    protected void componentActivated(BundleContext context) {
        Version version = new Version(context.getBundle());
        setVersion(version.getVersionNumber());
    }
    
    @Deactivate
    protected void componentDeactivated() {
        if (isActive()) {
            deactivate();
        }
    }
    
    // DS bind method
    protected void bindHostInfoDAO(HostInfoDAO dao) {
        this.hostInfoDAO = dao;
    }
    
    // DS bind method
    protected void bindWriterID(WriterID id) {
        this.writerID = id;
    }

}


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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Version;

import com.redhat.thermostat.host.overview.internal.models.HostInfoBuilder;
import com.redhat.thermostat.host.overview.internal.models.HostInfoDAO;
import com.redhat.thermostat.host.overview.model.HostInfo;
import com.redhat.thermostat.storage.core.WriterID;

public class HostOverviewBackendTest {
    
    private HostOverviewBackend backend;
    private HostInfoDAO hostInfoDAO;
    private WriterID writerID;
    private HostInfo info;
    private HostInfoBuilder builder;
    private HostOverviewBackend.HostInfoBuilderCreator builderCreator;

    @Before
    public void setup() {
        hostInfoDAO = mock(HostInfoDAO.class);
        writerID = mock(WriterID.class);
        
        info = mock(HostInfo.class);
        builder = mock(HostInfoBuilder.class);
        when(builder.build()).thenReturn(info);
        builderCreator = mock(HostOverviewBackend.HostInfoBuilderCreator.class);
        when(builderCreator.create(writerID)).thenReturn(builder);
        
        backend = new HostOverviewBackend(builderCreator);
        backend.bindHostInfoDAO(hostInfoDAO);
        backend.bindWriterID(writerID);
    }

    @Test
    public void testActivate() {
        backend.activate();
        assertTrue(backend.isActive());
        
        verify(builderCreator).create(writerID);
        verify(builder).build();
        verify(hostInfoDAO).put(info.getHostname(), info);
    }
    
    @Test
    public void testDeactivate() {
        backend.activate();
        backend.deactivate();
        assertFalse(backend.isActive());
    }
    
    @Test
    public void testComponentActivated() {
        BundleContext context = mock(BundleContext.class);
        Bundle bundle = mock(Bundle.class);
        Version version = new Version(1, 2, 3);
        when(bundle.getVersion()).thenReturn(version);
        when(context.getBundle()).thenReturn(bundle);
        
        backend.componentActivated(context);
        
        assertEquals("1.2.3", backend.getVersion());
    }
    
    @Test
    public void testComponentDeactivated() {
        backend.activate();
        assertTrue(backend.isActive());
        backend.componentDeactivated();
        assertFalse(backend.isActive());
    }
}


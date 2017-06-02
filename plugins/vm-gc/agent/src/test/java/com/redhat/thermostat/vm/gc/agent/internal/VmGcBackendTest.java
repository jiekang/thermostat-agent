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

package com.redhat.thermostat.vm.gc.agent.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Version;

import com.redhat.thermostat.agent.VmStatusListenerRegistrar;
import com.redhat.thermostat.common.Ordered;
import com.redhat.thermostat.storage.core.WriterID;
import com.redhat.thermostat.vm.gc.agent.internal.VmGcBackend.ListenerCreator;
import com.redhat.thermostat.vm.gc.common.VmGcStatDAO;

public class VmGcBackendTest {

    private TestVmGcBackend backend;
    private ListenerCreator listenerCreator;

    @Before
    public void setup() {
        listenerCreator = mock(ListenerCreator.class);
        backend = new TestVmGcBackend(listenerCreator);
    }
    
    @Test
    public void testComponentActivated() {
        BundleContext context = mock(BundleContext.class);
        Bundle bundle = mock(Bundle.class);
        Version version = new Version(1, 2, 3);
        when(bundle.getVersion()).thenReturn(version);
        when(context.getBundle()).thenReturn(bundle);
        
        WriterID id = mock(WriterID.class);
        backend.bindWriterId(id);
        backend.componentActivated(context);
        
        assertEquals(id, backend.writerId);
        assertEquals("1.2.3", backend.version);
        assertNotNull(backend.registrar);
    }
    
    @Test
    public void testComponentDeactivated() {
        // Begin with backend appearing active for this test
        backend.active = true;
        
        assertTrue(backend.isActive());
        backend.componentDeactivated();
        assertFalse(backend.isActive());
    }
    
    @Test
    public void testCreateVmListener() {
        final String writerId = "myAgent";
        final String vmId = "myJVM";
        final int pid = 1234;
        
        VmGcStatDAO dao = mock(VmGcStatDAO.class);
        backend.bindVmGcStats(dao);
        backend.createVmListener(writerId, vmId, pid);
        
        verify(listenerCreator).create(writerId, dao, vmId);
    }
    
    @Test
    public void testOrderValue() {
        int order = backend.getOrderValue();
        assertTrue(order >= Ordered.ORDER_MEMORY_GROUP);
        assertTrue(order < Ordered.ORDER_NETWORK_GROUP);
    }
    
    static class TestVmGcBackend extends VmGcBackend {
        WriterID writerId;
        VmStatusListenerRegistrar registrar;
        String version;
        boolean active;
        
        TestVmGcBackend(ListenerCreator creator) {
            super(creator);
        }
        
        // Override to capture values
        @Override
        protected void initialize(WriterID writerId, VmStatusListenerRegistrar registrar, String version) {
            this.writerId = writerId;
            this.registrar = registrar;
            this.version = version;
        }
        
        // Override the following to test backend is deactivated when dependencies are lost
        @Override
        public boolean isActive() {
            return active;
        }
        @Override
        public boolean deactivate() {
            active = false;
            return true;
        }
    }
}


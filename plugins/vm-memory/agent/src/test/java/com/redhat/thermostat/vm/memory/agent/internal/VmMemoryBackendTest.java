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

package com.redhat.thermostat.vm.memory.agent.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.redhat.thermostat.common.Ordered;
import com.redhat.thermostat.jvm.overview.agent.VmStatusListenerRegistrar;
import com.redhat.thermostat.storage.core.WriterID;
import com.redhat.thermostat.vm.memory.agent.internal.VmMemoryBackend.ListenerCreator;
import com.redhat.thermostat.vm.memory.common.VmMemoryStatDAO;
import com.redhat.thermostat.vm.memory.common.VmTlabStatDAO;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Version;

public class VmMemoryBackendTest {

    private static ListenerCreator listenerCreator;
    private static WriterID id;
    private static Version version;

    @BeforeClass
    public static void setup() {
        listenerCreator = mock(ListenerCreator.class);
        id = mock(WriterID.class);
        version = new Version(1, 2, 3);
    }

    @Test
    public void testComponentActivated() {
        TestVmMemoryBackend backend = new TestVmMemoryBackend(listenerCreator);

        BundleContext context = mock(BundleContext.class);
        Bundle bundle = mock(Bundle.class);
        when(bundle.getVersion()).thenReturn(version);
        when(context.getBundle()).thenReturn(bundle);
        backend.bindWriterId(id);

        assertFalse(backend.wasInitializeCalled);
        backend.componentActivated(context);
        assertTrue(backend.wasInitializeCalled);
        verify(context).getBundle();

        assertEquals(version.toString(), backend.getVersion());
        assertEquals(id, backend.writerId);
        assertNotNull(backend.registrar);
    }

    @Test
    public void testComponentActivateAndDeactivate() {
        TestVmMemoryBackend backend = new TestVmMemoryBackend(listenerCreator);
        VmStatusListenerRegistrar registrar = mock(VmStatusListenerRegistrar.class);
        backend.initialize(id, registrar, version.toString());

        assertFalse(backend.isActive());
        verify(registrar, times(0)).register(backend);

        assertTrue(backend.activate());

        assertTrue(backend.isActive());
        verify(registrar).register(backend);
        verify(registrar, times(0)).unregister(backend);

        backend.componentDeactivated();

        assertFalse(backend.isActive());
        verify(registrar).unregister(backend);
    }

    @Test
    public void testCreateVmListener() {
        final String writerId = "myAgent";
        final String vmId = "myJVM";
        final int pid = 1234;

        TestVmMemoryBackend backend = new TestVmMemoryBackend(listenerCreator);
        VmMemoryStatDAO dao = mock(VmMemoryStatDAO.class);
        VmTlabStatDAO tlabDao = mock(VmTlabStatDAO.class);
        backend.bindVmMemoryStatDAO(dao);
        backend.bindVmTlabStatDAO(tlabDao);
        backend.createVmListener(writerId, vmId, pid);

        verify(listenerCreator).create(writerId, dao, tlabDao, vmId);
    }

    @Test
    public void testOrderValue() {
        TestVmMemoryBackend backend = new TestVmMemoryBackend(listenerCreator);
        int orderValue = backend.getOrderValue();
        assertTrue(orderValue >= Ordered.ORDER_MEMORY_GROUP);
        assertTrue(orderValue < Ordered.ORDER_NETWORK_GROUP);
    }

    static class TestVmMemoryBackend extends VmMemoryBackend {

        VmStatusListenerRegistrar registrar;
        WriterID writerId;
        boolean wasInitializeCalled = false;

        TestVmMemoryBackend(ListenerCreator creator) {
            super(creator);
        }

        @Override
        protected void initialize(WriterID id, VmStatusListenerRegistrar registrar, String version) {
            super.initialize(id, registrar, version);
            this.wasInitializeCalled = true;
            this.registrar = registrar;
            this.writerId = id;
        }
    }
}

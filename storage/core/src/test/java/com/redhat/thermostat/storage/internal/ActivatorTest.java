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

package com.redhat.thermostat.storage.internal;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

import com.redhat.thermostat.common.ApplicationService;
import com.redhat.thermostat.common.Timer;
import com.redhat.thermostat.common.TimerFactory;
import com.redhat.thermostat.shared.config.CommonPaths;
import com.redhat.thermostat.storage.core.WriterID;
import com.redhat.thermostat.storage.dao.AgentInfoDAO;
import com.redhat.thermostat.storage.dao.BackendInfoDAO;
import com.redhat.thermostat.storage.dao.NetworkInterfaceInfoDAO;
import com.redhat.thermostat.storage.dao.VmInfoDAO;
import com.redhat.thermostat.storage.internal.dao.AgentInfoDAOImpl;
import com.redhat.thermostat.storage.internal.dao.BackendInfoDAOImpl;
import com.redhat.thermostat.storage.internal.dao.NetworkInterfaceInfoDAOImpl;
import com.redhat.thermostat.storage.internal.dao.VmInfoDAOImpl;
import com.redhat.thermostat.storage.internal.dao.VmInfoDAOTest;
import com.redhat.thermostat.testutils.StubBundleContext;

import static com.redhat.thermostat.storage.internal.Activator.DAOCreator;

public class ActivatorTest {
    
    @Test
    public void verifyActivatorRegistersServices() throws Exception {
        StubBundleContext context = new StubBundleContext();
        DAOCreator creator = mock(DAOCreator.class);
        VmInfoDAOImpl dao = mock(VmInfoDAOImpl.class);
        when(creator.createVmInfoDAO(any(StorageCoreConfiguration.class))).thenReturn(dao);

        CommonPaths paths = mock(CommonPaths.class);
        context.registerService(CommonPaths.class, paths, null);

        Activator activator = new Activator(creator);

        activator.start(context);

        assertTrue(context.isServiceRegistered(WriterID.class.getName(), WriterIDImpl.class));
        assertTrue(context.isServiceRegistered(NetworkInterfaceInfoDAO.class.getName(), NetworkInterfaceInfoDAOImpl.class));
        assertTrue(context.isServiceRegistered(VmInfoDAO.class.getName(), dao.getClass()));
        assertTrue(context.isServiceRegistered(AgentInfoDAO.class.getName(), AgentInfoDAOImpl.class));
        assertTrue(context.isServiceRegistered(BackendInfoDAO.class.getName(), BackendInfoDAOImpl.class));

        activator.stop(context);

        assertEquals(0, context.getServiceListeners().size());

        assertEquals(1, context.getAllServices().size());
    }

    @Test
    public void verifyActivatorUnregistersServices() throws Exception {
        StubBundleContext context = new StubBundleContext();
        DAOCreator creator = mock(DAOCreator.class);
        VmInfoDAOImpl dao = mock(VmInfoDAOImpl.class);
        when(creator.createVmInfoDAO(any(StorageCoreConfiguration.class))).thenReturn(dao);

        CommonPaths paths = mock(CommonPaths.class);
        context.registerService(CommonPaths.class, paths, null);

        Activator activator = new Activator(creator);

        activator.start(context);

        activator.stop(context);
        
        assertFalse(context.isServiceRegistered(NetworkInterfaceInfoDAO.class.getName(), NetworkInterfaceInfoDAOImpl.class));
        assertFalse(context.isServiceRegistered(VmInfoDAO.class.getName(), VmInfoDAOImpl.class));
        assertFalse(context.isServiceRegistered(AgentInfoDAO.class.getName(), AgentInfoDAOImpl.class));
        assertFalse(context.isServiceRegistered(BackendInfoDAO.class.getName(), BackendInfoDAOImpl.class));
        assertFalse(context.isServiceRegistered(WriterID.class.getName(), WriterIDImpl.class));
        
        assertEquals(0, context.getServiceListeners().size());
        assertEquals(1, context.getAllServices().size());
    }
    
    @Test
    public void verifyActivatorRegistersServicesMultipleTimes() throws Exception {
        StubBundleContext context = new StubBundleContext();
        ApplicationService appService = mock(ApplicationService.class);
        TimerFactory timerFactory = mock(TimerFactory.class);
        when(appService.getTimerFactory()).thenReturn(timerFactory);        
        Timer timer = mock(Timer.class);
        when(timerFactory.createTimer()).thenReturn(timer);
        context.registerService(ApplicationService.class, appService, null);

        DAOCreator creator = mock(DAOCreator.class);
        VmInfoDAOImpl dao = mock(VmInfoDAOImpl.class);
        when(creator.createVmInfoDAO(any(StorageCoreConfiguration.class))).thenReturn(dao);

        CommonPaths paths = mock(CommonPaths.class);
        context.registerService(CommonPaths.class, paths, null);

        Activator activator = new Activator(creator);

        activator.start(context);

        assertTrue(context.isServiceRegistered(NetworkInterfaceInfoDAO.class.getName(), NetworkInterfaceInfoDAOImpl.class));
        assertTrue(context.isServiceRegistered(VmInfoDAO.class.getName(), dao.getClass()));
        assertTrue(context.isServiceRegistered(AgentInfoDAO.class.getName(), AgentInfoDAOImpl.class));
        assertTrue(context.isServiceRegistered(BackendInfoDAO.class.getName(), BackendInfoDAOImpl.class));
        assertTrue(context.isServiceRegistered(WriterID.class.getName(), WriterIDImpl.class));

        activator.stop(context);
        
        assertEquals(0, context.getServiceListeners().size());
        assertEquals(2, context.getAllServices().size());
        
        activator.start(context);

        assertTrue(context.isServiceRegistered(NetworkInterfaceInfoDAO.class.getName(), NetworkInterfaceInfoDAOImpl.class));
        assertTrue(context.isServiceRegistered(VmInfoDAO.class.getName(), dao.getClass()));
        assertTrue(context.isServiceRegistered(AgentInfoDAO.class.getName(), AgentInfoDAOImpl.class));
        assertTrue(context.isServiceRegistered(BackendInfoDAO.class.getName(), BackendInfoDAOImpl.class));
        assertTrue(context.isServiceRegistered(WriterID.class.getName(), WriterIDImpl.class));

        activator.stop(context);

        assertEquals(0, context.getServiceListeners().size());
        assertEquals(2, context.getAllServices().size());
        
    }
}


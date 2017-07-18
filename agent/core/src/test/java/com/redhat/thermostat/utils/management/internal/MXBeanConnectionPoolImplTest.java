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

package com.redhat.thermostat.utils.management.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import com.redhat.thermostat.agent.utils.management.MXBeanConnection;
import com.redhat.thermostat.agent.utils.management.MXBeanConnectionException;
import com.redhat.thermostat.common.portability.ProcessUserInfo;
import com.redhat.thermostat.common.portability.ProcessUserInfoBuilder;
import com.redhat.thermostat.utils.management.internal.MXBeanConnectionPoolImpl.ManagementAgentHelper;

public class MXBeanConnectionPoolImplTest {
    
    private static final String JMX_URL = "jmxUrl://hello";
    
    private ManagementAgentAttacher attacher;
    private MXBeanConnectionPoolImpl pool;
    private MXBeanConnectionImpl connection;
    private ManagementAgentHelper helper;
    private MXBeanConnector connector;

    private ProcessUserInfoBuilder builder;
    
    @Before
    public void setup() throws Exception {
        connection = mock(MXBeanConnectionImpl.class);
        connector = mock(MXBeanConnector.class);
        helper = mock(ManagementAgentHelper.class);

        attacher = mock(ManagementAgentAttacher.class);
        when(attacher.getConnectorAddress()).thenReturn(JMX_URL);
        when(helper.createAttacher(8000)).thenReturn(attacher);
        when(helper.createConnector(JMX_URL)).thenReturn(connector);
        when(connector.connect()).thenReturn(connection);

        builder = mock(ProcessUserInfoBuilder.class);
        ProcessUserInfo info = new ProcessUserInfo(8000, "Test");
        when(builder.build(8000)).thenReturn(info);
        
        pool = new MXBeanConnectionPoolImpl(helper);
    }
    
    @Test
    public void testAcquire() throws Exception {
        MXBeanConnection result = pool.acquire(8000);
        
        verify(helper).createAttacher(8000);
        verify(attacher).attach();
        verify(attacher).getConnectorAddress();
        verify(helper).createConnector(JMX_URL);

        assertNotNull(result);
        assertEquals(result, connection);

        verify(connector).connect();
    }

    @Test
    public void testAcquireTwice() throws Exception {
        MXBeanConnection connection1 = pool.acquire(8000);
    
        verify(connector).connect();
    
        MXBeanConnection connection2 = pool.acquire(8000);
    
        // Should only be invoked once
        verify(helper).createAttacher(8000);
        verify(attacher).attach();
        verify(attacher).getConnectorAddress();
        verify(helper).createConnector(JMX_URL);
        
        assertEquals(connection1, connection);
        assertEquals(connection2, connection);
    
        verifyNoMoreInteractions(connector);
    }
    
    @Test
    public void testRelease() throws Exception {
        MXBeanConnection result = pool.acquire(8000);
    
        verify(connection, never()).close();
    
        pool.release(8000, result);
    
        verify(connection).close();
    }

    @Test
    public void testReleaseTwice() throws Exception {
        // connection1 == connection1 == actualConnection
        MXBeanConnection connection1 = pool.acquire(8000);
        MXBeanConnection connection2 = pool.acquire(8000);
    
        pool.release(8000, connection1);
    
        verify(connection, never()).close();
    
        pool.release(8000, connection2);
    
        verify(connection).close();
    }
    
    @Test(expected=MXBeanConnectionException.class)
    public void testReleaseNotRunning() throws Exception {
        pool.release(8000, connection);
    }

}


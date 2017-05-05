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

package com.redhat.thermostat.host.overview.common.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.NoSuchElementException;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import com.redhat.thermostat.host.overview.common.HostInfoDAO;
import com.redhat.thermostat.host.overview.common.model.HostInfo;
import com.redhat.thermostat.storage.core.AgentId;
import com.redhat.thermostat.storage.core.Cursor;
import com.redhat.thermostat.storage.core.DescriptorParsingException;
import com.redhat.thermostat.storage.core.Key;
import com.redhat.thermostat.storage.core.PreparedStatement;
import com.redhat.thermostat.storage.core.StatementDescriptor;
import com.redhat.thermostat.storage.core.StatementExecutionException;
import com.redhat.thermostat.storage.core.Storage;
import com.redhat.thermostat.storage.model.AggregateCount;

public class HostInfoDAOTest {

    static class Triple<S, T, U> {
        final S first;
        final T second;
        final U third;

        public Triple(S first, T second, U third) {
            this.first = first;
            this.second = second;
            this.third = third;
        }
    }

    private static final String HOST_NAME = "a host name";
    private static final String OS_NAME = "some os";
    private static final String OS_KERNEL = "some kernel";
    private static final String CPU_MODEL = "some cpu that runs fast";
    private static final int CPU_NUM = -1;
    private static final long MEMORY_TOTAL = 0xCAFEBABEl;

    @Test
    public void preparedQueryDescriptorsAreSane() {
        String expectedHostInfo = "QUERY host-info WHERE 'agentId' = ?s LIMIT 1";
        assertEquals(expectedHostInfo, HostInfoDAOImpl.QUERY_HOST_INFO);
        String expectedAllHosts = "QUERY host-info";
        assertEquals(expectedAllHosts, HostInfoDAOImpl.QUERY_ALL_HOSTS);
        String aggregateAllHosts = "QUERY-COUNT host-info";
        assertEquals(aggregateAllHosts, HostInfoDAOImpl.AGGREGATE_COUNT_ALL_HOSTS);
        String addHostInfo = "ADD host-info SET 'agentId' = ?s , " +
                                                  "'hostname' = ?s , " +
                                                  "'osName' = ?s , " +
                                                  "'osKernel' = ?s , " +
                                                  "'cpuModel' = ?s , " +
                                                  "'cpuCount' = ?i , " +
                                                  "'totalMemory' = ?l";
        assertEquals(addHostInfo, HostInfoDAOImpl.DESC_ADD_HOST_INFO);
    }
    
    @Test
    public void testCategory() {
        assertEquals("host-info", HostInfoDAO.hostInfoCategory.getName());
        Collection<Key<?>> keys = HostInfoDAO.hostInfoCategory.getKeys();
        assertTrue(keys.contains(new Key<>("agentId")));
        assertTrue(keys.contains(new Key<String>("hostname")));
        assertTrue(keys.contains(new Key<String>("osName")));
        assertTrue(keys.contains(new Key<String>("osKernel")));
        assertTrue(keys.contains(new Key<String>("cpuModel")));
        assertTrue(keys.contains(new Key<Integer>("cpuCount")));
        assertTrue(keys.contains(new Key<Long>("totalMemory")));
        assertEquals(7, keys.size());
    }

    @Test
    public void testGetHostInfoUsingAgentId() throws DescriptorParsingException, StatementExecutionException {
        Storage storage = mock(Storage.class);
        @SuppressWarnings("unchecked")
        PreparedStatement<HostInfo> prepared = (PreparedStatement<HostInfo>) mock(PreparedStatement.class);
        when(storage.prepareStatement(anyDescriptor())).thenReturn(prepared);

        HostInfo info = new HostInfo("foo-agent", HOST_NAME, OS_NAME, OS_KERNEL, CPU_MODEL, CPU_NUM, MEMORY_TOTAL);
        @SuppressWarnings("unchecked")
        Cursor<HostInfo> cursor = (Cursor<HostInfo>) mock(Cursor.class);
        when(cursor.hasNext()).thenReturn(true).thenReturn(false);
        when(cursor.next()).thenReturn(info).thenReturn(null);
        when(prepared.executeQuery()).thenReturn(cursor);

        HostInfo result = new HostInfoDAOImpl(storage).getHostInfo(new AgentId("some uid"));

        verify(storage).prepareStatement(anyDescriptor());
        verify(prepared).setString(0, "some uid");
        verify(prepared).executeQuery();
        assertSame(result, info);
    }

    @SuppressWarnings("unchecked")
    private StatementDescriptor<HostInfo> anyDescriptor() {
        return (StatementDescriptor<HostInfo>) any(StatementDescriptor.class);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testPutHostInfo() throws DescriptorParsingException,
            StatementExecutionException {
        Storage storage = mock(Storage.class);
        PreparedStatement<HostInfo> add = mock(PreparedStatement.class);
        when(storage.prepareStatement(any(StatementDescriptor.class))).thenReturn(add);

        HostInfo info = new HostInfo("foo-agent", HOST_NAME, OS_NAME, OS_KERNEL, CPU_MODEL, CPU_NUM, MEMORY_TOTAL);
        HostInfoDAO dao = new HostInfoDAOImpl(storage);
        dao.putHostInfo(info);
        
        @SuppressWarnings("rawtypes")
        ArgumentCaptor<StatementDescriptor> captor = ArgumentCaptor.forClass(StatementDescriptor.class);

        verify(storage).prepareStatement(captor.capture());
        StatementDescriptor<?> desc = captor.getValue();
        assertEquals(HostInfoDAOImpl.DESC_ADD_HOST_INFO, desc.getDescriptor());
        
        verify(add).setString(0, info.getAgentId());
        verify(add).setString(1, info.getHostname());
        verify(add).setString(2, info.getOsName());
        verify(add).setString(3, info.getOsKernel());
        verify(add).setString(4, info.getCpuModel());
        verify(add).setInt(5, info.getCpuCount());
        verify(add).setLong(6, info.getTotalMemory());
        verify(add).execute();
        Mockito.verifyNoMoreInteractions(add);
    }

    @Test
    public void testGetCount() throws DescriptorParsingException,
            StatementExecutionException {
        AggregateCount count = new AggregateCount();
        count.setCount(2);

        @SuppressWarnings("unchecked")
        Cursor<AggregateCount> c = (Cursor<AggregateCount>) mock(Cursor.class);
        when(c.hasNext()).thenReturn(true).thenReturn(false);
        when(c.next()).thenReturn(count).thenThrow(new NoSuchElementException());

        Storage storage = mock(Storage.class);
        @SuppressWarnings("unchecked")
        PreparedStatement<AggregateCount> stmt = (PreparedStatement<AggregateCount>) mock(PreparedStatement.class);
        @SuppressWarnings("unchecked")
        StatementDescriptor<AggregateCount> desc = any(StatementDescriptor.class);
        when(storage.prepareStatement(desc)).thenReturn(stmt);
        when(stmt.executeQuery()).thenReturn(c);
        HostInfoDAOImpl dao = new HostInfoDAOImpl(storage);

        assertEquals(2, dao.getCount());
    }
    
}


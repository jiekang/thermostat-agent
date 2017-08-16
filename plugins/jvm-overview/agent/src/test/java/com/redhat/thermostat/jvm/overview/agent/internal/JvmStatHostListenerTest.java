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

package com.redhat.thermostat.jvm.overview.agent.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.AdditionalMatchers.not;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.redhat.thermostat.jvm.overview.agent.VmBlacklist;
import com.redhat.thermostat.jvm.overview.agent.VmStatusListener.Status;
import com.redhat.thermostat.jvm.overview.agent.internal.model.VmInfoDAO;
import com.redhat.thermostat.jvm.overview.agent.model.VmInfo;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.redhat.thermostat.common.portability.ProcessUserInfo;
import com.redhat.thermostat.common.portability.ProcessUserInfoBuilder;
import com.redhat.thermostat.storage.core.WriterID;

import sun.jvmstat.monitor.HostIdentifier;
import sun.jvmstat.monitor.Monitor;
import sun.jvmstat.monitor.MonitorException;
import sun.jvmstat.monitor.MonitoredHost;
import sun.jvmstat.monitor.MonitoredVm;
import sun.jvmstat.monitor.StringMonitor;
import sun.jvmstat.monitor.VmIdentifier;
import sun.jvmstat.monitor.event.VmStatusChangeEvent;

public class JvmStatHostListenerTest {
    
    private static String INFO_CMDLINE = "/path/to/executable command line args";
    private static String INFO_JAVAHOME = "/path/to/java";
    private static String INFO_JAVAVER = "1.9001";
    private static String INFO_MAINCLASS = "MyMainClass";
    private static String INFO_VMARGS = "-Xarg1 -Xarg2";
    private static String INFO_VMINFO = "Info";
    private static String INFO_VMNAME = "MyJVM";
    private static String INFO_VMVER = "90.01";
    private static long INFO_VMUSERID = 2000;
    private static String INFO_VMUSERNAME = "User";
    private static final long INFO_STARTTIME = Long.MIN_VALUE;

    private JvmStatHostListener hostListener;
    private MonitoredHost host;
    private MonitoredVm monitoredVm1;
    private MonitoredVm monitoredVm2;
    private JvmStatDataExtractor extractor;
    private VmInfoDAO vmInfoDAO;
    private VmStatusChangeNotifier notifier;
    private VmBlacklist blacklist;

    @Before
    public void setup() throws MonitorException, URISyntaxException {
        vmInfoDAO = mock(VmInfoDAO.class);
        notifier = mock(VmStatusChangeNotifier.class);
        
        ProcessUserInfoBuilder userInfoBuilder = mock(ProcessUserInfoBuilder.class);
        ProcessUserInfo userInfo = new ProcessUserInfo(INFO_VMUSERID, INFO_VMUSERNAME);
        when(userInfoBuilder.build(any(int.class))).thenReturn(userInfo);

        WriterID id = mock(WriterID.class);
        blacklist = mock(VmBlacklist.class);
        hostListener = new JvmStatHostListener(vmInfoDAO, notifier, userInfoBuilder, id, blacklist);
        
        host = mock(MonitoredHost.class);
        HostIdentifier hostId = mock(HostIdentifier.class);
        monitoredVm1 = mock(MonitoredVm.class);
        monitoredVm2 = mock(MonitoredVm.class);
        StringMonitor monitor = mock(StringMonitor.class);
        Monitor monitor2 = mock(Monitor.class);
        VmIdentifier vmId1 = new VmIdentifier("1");
        VmIdentifier vmId2 = new VmIdentifier("2");

        when(monitor2.getValue()).thenReturn(100l);
        when(host.getHostIdentifier()).thenReturn(hostId);
        when(host.getMonitoredVm(eq(vmId1))).thenReturn(monitoredVm1);
        when(host.getMonitoredVm(eq(vmId2))).thenReturn(monitoredVm2);
        when(hostId.resolve(eq(vmId1))).thenReturn(vmId1);
        when(hostId.resolve(eq(vmId2))).thenReturn(vmId2);
        when(monitoredVm1.findByName("sun.rt.vmInitDoneTime")).thenReturn(monitor2);
        when(monitoredVm2.findByName("sun.rt.vmInitDoneTime")).thenReturn(monitor2);
        when(monitoredVm1.findByName(not(eq("sun.rt.vmInitDoneTime")))).thenReturn(monitor);
        when(monitoredVm2.findByName(not(eq("sun.rt.vmInitDoneTime")))).thenReturn(monitor);
        when(monitor.stringValue()).thenReturn("test");
        when(monitor.getValue()).thenReturn("test");
        extractor = mock(JvmStatDataExtractor.class);
        
        when(extractor.getCommandLine()).thenReturn(INFO_CMDLINE);
        when(extractor.getJavaHome()).thenReturn(INFO_JAVAHOME);
        when(extractor.getJavaVersion()).thenReturn(INFO_JAVAVER);
        when(extractor.getMainClass()).thenReturn(INFO_MAINCLASS);
        when(extractor.getVmArguments()).thenReturn(INFO_VMARGS);
        when(extractor.getVmInfo()).thenReturn(INFO_VMINFO);
        when(extractor.getVmName()).thenReturn(INFO_VMNAME);
        when(extractor.getVmVersion()).thenReturn(INFO_VMVER);
        when(extractor.getVmStartTime()).thenReturn(INFO_STARTTIME);
    }
    
    @Test
    public void testNewVM() throws InterruptedException, MonitorException {
        startVMs();
        
        assertTrue(hostListener.getMonitoredVms().containsKey(1));
        assertTrue(hostListener.getMonitoredVms().containsKey(2));
        assertEquals(monitoredVm1, hostListener.getMonitoredVms().get(1).getSecond());
        assertEquals(monitoredVm2, hostListener.getMonitoredVms().get(2).getSecond());
        
        // Check valid UUIDs
        UUID uuid1 = UUID.fromString(hostListener.getMonitoredVms().get(1).getFirst());
        UUID uuid2 = UUID.fromString(hostListener.getMonitoredVms().get(2).getFirst());
        assertFalse(uuid1.equals(uuid2));
        
        verify(notifier, times(2)).notifyVmStatusChange(eq(Status.VM_STARTED), anyString(), (isA(Integer.class)));
    }
    
    @Test
    public void testNewVMBlackListed() throws InterruptedException, MonitorException {
        when(blacklist.isBlacklisted(anyString())).thenReturn(true).thenReturn(false);
        startVMs();
        
        assertFalse(hostListener.getMonitoredVms().containsKey(1));
        assertTrue(hostListener.getMonitoredVms().containsKey(2));
        assertEquals(monitoredVm2, hostListener.getMonitoredVms().get(2).getSecond());
        
        UUID uuid = UUID.fromString(hostListener.getMonitoredVms().get(2).getFirst());
        verify(notifier).notifyVmStatusChange(eq(Status.VM_STARTED), eq(uuid.toString()), (isA(Integer.class)));
    }
    
    @Test
    public void testStoppedVM() throws InterruptedException, MonitorException {
        final Set<Integer> stopped = new HashSet<>();
        stopped.add(1);
        
        startVMs();
        
        // Trigger a change event
        VmStatusChangeEvent event = mock(VmStatusChangeEvent.class);
        when(event.getMonitoredHost()).thenReturn(host);
        when(event.getStarted()).thenReturn(Collections.<Integer>emptySet());
        when(event.getTerminated()).thenReturn(stopped);
        hostListener.vmStatusChanged(event);
        
        // Ensure only 1 removed
        assertFalse(hostListener.getMonitoredVms().containsKey(1));
        assertTrue(hostListener.getMonitoredVms().containsKey(2));
        assertEquals(monitoredVm2, hostListener.getMonitoredVms().get(2).getSecond());

        verify(notifier).notifyVmStatusChange(eq(Status.VM_STOPPED), anyString(), (isA(Integer.class)));

    }
    
    @Test
    public void testReusedPid() throws MonitorException {
        final Set<Integer> started = new HashSet<>();
        started.add(1);
        // Start VM
        VmStatusChangeEvent event = mock(VmStatusChangeEvent.class);
        when(event.getMonitoredHost()).thenReturn(host);
        when(event.getStarted()).thenReturn(started);
        when(event.getTerminated()).thenReturn(Collections.<Integer>emptySet());
        hostListener.vmStatusChanged(event);
        
        ArgumentCaptor<String> vmIdCaptor = ArgumentCaptor.forClass(String.class);
        
        // Stop VM
        event = mock(VmStatusChangeEvent.class);
        when(event.getMonitoredHost()).thenReturn(host);
        when(event.getStarted()).thenReturn(Collections.<Integer>emptySet());
        when(event.getTerminated()).thenReturn(started);
        hostListener.vmStatusChanged(event);
        
        // Start new VM
        event = mock(VmStatusChangeEvent.class);
        when(event.getMonitoredHost()).thenReturn(host);
        when(event.getStarted()).thenReturn(started);
        when(event.getTerminated()).thenReturn(Collections.<Integer>emptySet());
        hostListener.vmStatusChanged(event);
        
        verify(notifier, times(2)).notifyVmStatusChange(eq(Status.VM_STARTED), vmIdCaptor.capture(), eq(1));
        List<String> vmIds = vmIdCaptor.getAllValues();
        
        assertEquals(2, vmIds.size());
        String vmId1 = vmIds.get(0);
        String vmId2 = vmIds.get(1);
        assertNotNull(vmId1);
        assertNotNull(vmId2);
        assertFalse(vmId1.equals(vmId2));
    }

    private void startVMs() throws InterruptedException, MonitorException {
        final Set<Integer> started = new HashSet<>();
        started.add(1);
        started.add(2);

        // Trigger a change event
        VmStatusChangeEvent event = mock(VmStatusChangeEvent.class);
        when(event.getMonitoredHost()).thenReturn(host);
        when(event.getStarted()).thenReturn(started);
        when(event.getTerminated()).thenReturn(Collections.<Integer>emptySet());
        hostListener.vmStatusChanged(event);
    }

    @Test
    public void testCreateVmInfo() throws MonitorException {
        final String INFO_ID = "vmId";
        final int INFO_PID = 1;
        final long INFO_STOPTIME = Long.MAX_VALUE;
        VmInfo info = hostListener.createVmInfo(INFO_ID, INFO_PID, INFO_STOPTIME, extractor);
        
        assertEquals(INFO_PID, info.getJvmPid());
        assertEquals(INFO_STARTTIME, info.getStartTimeStamp());
        assertEquals(INFO_STOPTIME, info.getStopTimeStamp());
        assertEquals(INFO_CMDLINE, info.getJavaCommandLine());
        assertEquals(INFO_JAVAHOME, info.getJavaHome());
        assertEquals(INFO_JAVAVER, info.getJavaVersion());
        assertEquals(INFO_MAINCLASS, info.getMainClass());
        assertEquals(INFO_VMARGS, info.getJvmArguments());
        assertEquals(INFO_VMINFO, info.getJvmInfo());
        assertEquals(INFO_VMNAME, info.getJvmName());
        assertEquals(INFO_VMVER, info.getJvmVersion());
        assertEquals(INFO_VMUSERID, info.getUid());
        assertEquals(INFO_VMUSERNAME, info.getUsername());
    }
}


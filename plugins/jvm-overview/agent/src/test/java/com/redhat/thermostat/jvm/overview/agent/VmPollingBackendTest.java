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

package com.redhat.thermostat.jvm.overview.agent;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

import com.redhat.thermostat.jvm.overview.agent.VmStatusListener.Status;
import org.junit.Before;
import org.junit.Test;

import com.redhat.thermostat.common.Version;
import com.redhat.thermostat.common.internal.test.Bug;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class VmPollingBackendTest {

    private VmPollingBackend backend;
    private ScheduledExecutorService mockExecutor;
    private VmStatusListenerRegistrar mockRegistrar;

    @Before
    public void setUp() {
        mockExecutor = mock(ScheduledExecutorService.class);
        Version mockVersion = mock(Version.class);
        when(mockVersion.getVersionNumber()).thenReturn("backend-version");
        mockRegistrar = mock(VmStatusListenerRegistrar.class);
        backend = new VmPollingBackend("backend-name", "backend-description",
                  "backend-vendor", mockVersion, mockExecutor, mockRegistrar) {
        };
        if (!backend.getObserveNewJvm()) {
            /* At time of writing, default is true.  This is
             * inherited from parent PollingBackend.  In case
             * default changes:
             */
            backend.setObserveNewJvm(true);
        }
    }

    /**
     * If an action throws exceptions repeatedly, that action shall get
     * disabled/unregistered.
     */
    @Bug(id = "3242",
         summary = "Adverse Backend breaks other Backends badly ",
         url = "http://icedtea.classpath.org/bugzilla/show_bug.cgi?id=3242")
    @Test
    public void testDoScheduledActionsWithExceptions() {
        final int beyondExceptionThreshold = 13; // anything beyond 10 will do
        String vmId1 = "test-vm-id1", vmId2 = "test-vm-id2";
        int pid1 = 123, pid2 = 456;
        backend.vmStatusChanged(Status.VM_ACTIVE, vmId1, pid1);
        backend.vmStatusChanged(Status.VM_ACTIVE, vmId2, pid2);
        BadVmPollingAction badAction = new BadVmPollingAction();
        backend.registerAction(badAction);
        for (int i = 0; i < beyondExceptionThreshold; i++) {
            backend.doScheduledActions();
        }
    
        // The exceptions thrown for one vmID might disable the action
        // for *all* other vmIDs too. So the call count for one of the
        // vmIDs is actually 9, whereas the other one must have reached
        // the threshold count of 10.
        int callCountVm1 = badAction.getCallCount(vmId1);
        int callCountVm2 = badAction.getCallCount(vmId2);
        int minCallCount = Math.min(callCountVm2, callCountVm1);
        int maxCallCount = Math.max(callCountVm1, callCountVm2);
        assertEquals("Must not be called beyond exception threshold",
                10, maxCallCount);
        assertEquals("Other action's exception cancels globally",
                9, minCallCount);
    }

    @Test
    public void verifyCustomActivateRegistersListener() {
        backend.preActivate();
        verify(mockRegistrar).register(backend);
    }

    @Test
    public void verifyCustomDeactivateUnregistersListener() {
        backend.postDeactivate();
        verify(mockRegistrar).unregister(backend);
    }
    
    @Test
    public void verifyRegisteredActionPerformed() {
        String vmId = "test-vm-id";
        int pid = 123;
        backend.vmStatusChanged(Status.VM_ACTIVE, vmId, pid);
        VmPollingAction action = mock(VmPollingAction.class);
        backend.registerAction(action);
        backend.doScheduledActions();

        verify(action).run(eq(vmId), eq(pid));
    }

    @Test
    public void verifyMultipleRegisteredActionsPerformed() {
        String vmId = "test-vm-id";
        int pid = 123;
        backend.vmStatusChanged(Status.VM_ACTIVE, vmId, pid);
        VmPollingAction action1 = mock(VmPollingAction.class);
        VmPollingAction action2 = mock(VmPollingAction.class);
        backend.registerAction(action1);
        backend.registerAction(action2);
        backend.doScheduledActions();

        verify(action1).run(eq(vmId), eq(pid));
        verify(action2).run(eq(vmId), eq(pid));
    }

    @Test
    public void verifyActionsPerformedOnMultipleVms() {
        String vmId1 = "test-vm-id1", vmId2 = "test-vm-id2";
        int pid1 = 123, pid2 = 456;
        backend.vmStatusChanged(Status.VM_ACTIVE, vmId1, pid1);
        backend.vmStatusChanged(Status.VM_ACTIVE, vmId2, pid2);
        VmPollingAction action = mock(VmPollingAction.class);
        backend.registerAction(action);
        backend.doScheduledActions();

        verify(action).run(eq(vmId1), eq(pid1));
        verify(action).run(eq(vmId2), eq(pid2));
    }

    @Test
    public void verifyMultipleRegisteredActionsPerformedOnMultipleVms() {
        String vmId1 = "test-vm-id1", vmId2 = "test-vm-id2";
        int pid1 = 123, pid2 = 456;
        backend.vmStatusChanged(Status.VM_ACTIVE, vmId1, pid1);
        backend.vmStatusChanged(Status.VM_ACTIVE, vmId2, pid2);
        VmPollingAction action1 = mock(VmPollingAction.class);
        VmPollingAction action2 = mock(VmPollingAction.class);
        backend.registerAction(action1);
        backend.registerAction(action2);
        backend.doScheduledActions();

        verify(action1).run(eq(vmId1), eq(pid1));
        verify(action1).run(eq(vmId2), eq(pid2));
        verify(action2).run(eq(vmId1), eq(pid1));
        verify(action2).run(eq(vmId2), eq(pid2));
    }

    @Test
    public void verifyUnregisteredActionNotPerformed() {
        String vmId = "test-vm-id";
        int pid = 123;
        backend.vmStatusChanged(Status.VM_ACTIVE, vmId, pid);
        VmPollingAction action1 = mock(VmPollingAction.class);
        VmPollingAction action2 = mock(VmPollingAction.class);
        backend.registerAction(action1);
        backend.registerAction(action2);
        backend.doScheduledActions(); // Triggers both
        backend.unregisterAction(action1);
        backend.doScheduledActions(); // Triggers only action2

        verify(action1, times(1)).run(eq(vmId), eq(pid));
        verify(action2, times(2)).run(eq(vmId), eq(pid));
    }

    @Test
    public void verifyVmStatusChangedStartedAndActiveResultInPolling() {
        String vmId1 = "test-vm-id1", vmId2 = "test-vm-id2";
        int pid1 = 123, pid2 = 456;
        backend.vmStatusChanged(Status.VM_STARTED, vmId1, pid1);
        backend.vmStatusChanged(Status.VM_ACTIVE, vmId2, pid2);
        VmPollingAction action = mock(VmPollingAction.class);
        backend.registerAction(action);
        backend.doScheduledActions();

        verify(action).run(eq(vmId1), eq(pid1));
        verify(action).run(eq(vmId2), eq(pid2));
    }

    @Test
    public void verifyVmStatusChangedStopsResultsInNoMorePolling() {
        String vmId1 = "test-vm-id1", vmId2 = "test-vm-id2";
        int pid1 = 123, pid2 = 456;
        backend.vmStatusChanged(Status.VM_ACTIVE, vmId1, pid1);
        backend.vmStatusChanged(Status.VM_ACTIVE, vmId2, pid2);
        VmPollingAction action = mock(VmPollingAction.class);
        backend.registerAction(action);
        backend.doScheduledActions(); // Triggers for both vms
        backend.vmStatusChanged(Status.VM_STOPPED, vmId1, pid1);
        backend.doScheduledActions(); // Triggers only for vm2

        verify(action, times(1)).run(eq(vmId1), eq(pid1));
        verify(action, times(2)).run(eq(vmId2), eq(pid2));
    }

    @Test
    public void verifyGetSetObserveNewJvmWorksAsExpected() {
        String vmId1 = "test-vm-id1", vmId2 = "test-vm-id2";
        int pid1 = 123, pid2 = 456;
        backend.vmStatusChanged(Status.VM_ACTIVE, vmId1, pid1);
        backend.setObserveNewJvm(false);
        backend.vmStatusChanged(Status.VM_ACTIVE, vmId2, pid2); // Should be ignored.
        VmPollingAction action = mock(VmPollingAction.class);
        backend.registerAction(action);
        backend.doScheduledActions();

        verify(action).run(eq(vmId1), eq(pid1));
        verify(action, never()).run(eq(vmId2), eq(pid2));
    }
    
    private static class BadVmPollingAction implements VmPollingAction {
        
        private final Map<String, Integer> callCounts = new HashMap<>();
        
        @Override
        public void run(String vmId, int pid) {
            Integer currCount = callCounts.remove(vmId);
            if (currCount == null) {
                currCount = Integer.valueOf(1);
            } else {
                currCount++;
            }
            callCounts.put(vmId, Integer.valueOf(currCount));
            throw new RuntimeException("doScheduledActions() testing!");
        }
        
        private Integer getCallCount(String vmId) {
            return callCounts.get(vmId);
        }
        
    }
}

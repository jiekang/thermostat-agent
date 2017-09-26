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

package com.redhat.thermostat.vm.byteman.agent.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.jboss.byteman.agent.submit.ScriptText;
import org.jboss.byteman.agent.submit.Submit;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.redhat.thermostat.agent.ipc.server.AgentIPCService;
import com.redhat.thermostat.commands.model.AgentRequest;
import com.redhat.thermostat.commands.model.WebSocketResponse;
import com.redhat.thermostat.commands.model.WebSocketResponse.ResponseType;
import com.redhat.thermostat.jvm.overview.agent.model.VmId;
import com.redhat.thermostat.shared.config.CommonPaths;
import com.redhat.thermostat.storage.core.WriterID;
import com.redhat.thermostat.vm.byteman.agent.VmBytemanDAO;
import com.redhat.thermostat.vm.byteman.agent.VmBytemanStatus;
import com.redhat.thermostat.vm.byteman.agent.internal.BytemanRequest.RequestAction;

public class BytemanRequestReceiverTest {

    private static final String SYSTEM_ID = "some-system-id";
    private static final String SOME_VM_ID = "some-vm-id";
    private static final int SOME_LISTENPORT_BTM_AGENT_ATTACHED = 333;
    private static final String SOME_WRITER_ID = "some-writer-id";
    private static final String SOME_RULE = "some-rule";
    private static final String BYTEMAN_ACTION = "byteman";
    private static final int UNSET_JVM_PID = -99;
    private static final String NO_RULE = null;
    private long sequence;

    @Before
    public void setup() {
        sequence = (long)(1000 * Math.random());
    }

    @Test
    public void testLoadRulesAgentAttached() throws Exception {
        Submit submit = mock(Submit.class);
        doBasicLoadRulesTestBtmAgentAttached(submit);
        verify(submit, never()).deleteAllRules();
    }

    @Test
    public void testLoadRulesAgentAttachedWithRulesExisting() throws Exception {
        Submit submit = mock(Submit.class);
        when(submit.getAllScripts()).thenReturn(Arrays.asList(mock(ScriptText.class)));
        doBasicLoadRulesTestBtmAgentAttached(submit);
        verify(submit).deleteAllRules();
    }

    @Test
    public void testUnLoadRulesWithNoExistingRules() throws Exception {
        Submit submit = mock(Submit.class);
        when(submit.getAllScripts()).thenReturn(Collections.<ScriptText>emptyList());
        BytemanRequestReceiver receiver = createReceiver(submit, null, null);
        int someListenPort = 29320;
        int someJvmPid = 3280;
        WebSocketResponse response = receiver.receive(createRequest(RequestAction.UNLOAD_RULES, someListenPort, NO_RULE, someJvmPid));
        assertEquals(ResponseType.OK, response.getResponseType());
        assertEquals(sequence, response.getSequenceId());
        verify(submit, never()).deleteAllRules();
    }

    @Test
    public void testReceiveBadAction() throws Exception {
        AgentRequest request = new AgentRequest(sequence, "wrong-action", SYSTEM_ID, SOME_VM_ID, new TreeMap<String, String>());
        BytemanRequestReceiver receiver = createReceiver(mock(Submit.class), mock(WriterID.class), mock(VmBytemanDAO.class));
        WebSocketResponse response = receiver.receive(request);
        assertEquals(ResponseType.ERROR, response.getResponseType());
        assertEquals(sequence, response.getSequenceId());
    }

    @Test
    public void testUnLoadRulesWithExistingRules() throws Exception {
        Submit submit = mock(Submit.class);
        when(submit.getAllScripts()).thenReturn(Arrays.asList(mock(ScriptText.class)));
        WriterID writerId = mock(WriterID.class);
        String someAgentId = "some-agent-id";
        String someVmId = "some-vm-id";
        int someListenPort = 3333;
        int someJvmPid = 2321;
        VmBytemanDAO bytemanDao = mock(VmBytemanDAO.class);
        when(writerId.getWriterID()).thenReturn(someAgentId);
        BytemanRequestReceiver receiver = createReceiver(submit, writerId, bytemanDao);
        ArgumentCaptor<VmBytemanStatus> statusCaptor = ArgumentCaptor.forClass(VmBytemanStatus.class);
        WebSocketResponse response = receiver.receive(createRequest(RequestAction.UNLOAD_RULES, someListenPort, NO_RULE, someJvmPid));
        assertEquals(ResponseType.OK, response.getResponseType());
        assertEquals(sequence, response.getSequenceId());
        verify(submit).deleteAllRules();
        verify(bytemanDao).addBytemanStatus(statusCaptor.capture());
        VmBytemanStatus status = statusCaptor.getValue();
        assertEquals(someAgentId, status.getAgentId());
        assertEquals(someVmId, status.getJvmId());
        assertEquals(someListenPort, status.getListenPort());
        assertNull(status.getRule());
    }

    @Test
    public void testReceiveWithBadAction() {
        SortedMap<String, String> paramMap = new TreeMap<>();
        paramMap.put(BytemanRequest.ACTION_PARAM_NAME, Integer.toString(-1));
        AgentRequest badRequest = new AgentRequest(sequence, "byteman", "some-system-id", "jvm-id-foo", paramMap);
        BytemanRequestReceiver receiver = new BytemanRequestReceiver();
        WebSocketResponse response = receiver.receive(badRequest);
        assertEquals(ResponseType.ERROR, response.getResponseType());
        assertEquals(sequence, response.getSequenceId());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testLoadRuleWithBytemanAgentNotAttached() throws Exception {
        final int listenPort = 8939;
        final Submit submit = mock(Submit.class);
        VmBytemanDAO bytemanDao = mock(VmBytemanDAO.class);
        BytemanAgentAttachManager attachManager = mock(BytemanAgentAttachManager.class);
        VmBytemanStatus status = new VmBytemanStatus();
        status.setListenPort(listenPort);
        when(attachManager.attachBytemanToVm(any(VmId.class), any(int.class))).thenReturn(status);
        WriterID wid = mock(WriterID.class);
        when(wid.getWriterID()).thenReturn(SOME_WRITER_ID);
        BytemanRequestReceiver receiver = new BytemanRequestReceiver(attachManager) {
            @Override
            protected Submit getSubmit(int port) {
                assertEquals(listenPort, port);
                return submit;
            }
        };
        receiver.bindWriterId(wid);
        receiver.bindVmBytemanDao(bytemanDao);
        int someVmPid = 3023;
        AgentRequest loadRequest = createRequest(RequestAction.LOAD_RULES, BytemanRequest.NOT_ATTACHED_PORT, SOME_RULE, someVmPid);
        receiver.receive(loadRequest);
        VmId vmId = new VmId(SOME_VM_ID);
        verify(attachManager).attachBytemanToVm(eq(vmId), eq(someVmPid));
        verify(submit).addRulesFromResources(any(List.class));
    }

    @Test
    public void testUnloadRuleIllegalPid() {
        int badJvmPid = -1;
        AgentRequest loadRequest = createRequest(RequestAction.UNLOAD_RULES, SOME_LISTENPORT_BTM_AGENT_ATTACHED, NO_RULE, badJvmPid);
        BytemanRequestReceiver receiver = new BytemanRequestReceiver();
        WebSocketResponse response = receiver.receive(loadRequest);
        assertEquals(ResponseType.ERROR, response.getResponseType());
        assertEquals(sequence, response.getSequenceId());
    }

    @Test
    public void testUnloadRuleIllegalPort() {
        int invalidPort = -3;
        int validPid = 2302;
        AgentRequest loadRequest = createRequest(RequestAction.UNLOAD_RULES, invalidPort, NO_RULE, validPid);
        BytemanRequestReceiver receiver = new BytemanRequestReceiver();
        WebSocketResponse response = receiver.receive(loadRequest);
        assertEquals(ResponseType.ERROR, response.getResponseType());
        assertEquals(sequence, response.getSequenceId());
    }

    @Test
    public void testBindWriterId() {
        BytemanAgentAttachManager attachManager = mock(BytemanAgentAttachManager.class);
        BytemanRequestReceiver receiver = new BytemanRequestReceiver(attachManager);
        WriterID wid = mock(WriterID.class);
        receiver.bindWriterId(wid);
        verify(attachManager).setWriterId(wid);
    }

    @Test
    public void testBindVmBytemanDAO() {
        BytemanAgentAttachManager attachManager = mock(BytemanAgentAttachManager.class);
        BytemanRequestReceiver receiver = new BytemanRequestReceiver(attachManager);
        VmBytemanDAO dao = mock(VmBytemanDAO.class);
        receiver.bindVmBytemanDao(dao);
        verify(attachManager).setVmBytemanDao(dao);
    }

    @Test
    public void testBindCommonPaths() {
        BytemanAgentAttachManager attachManager = mock(BytemanAgentAttachManager.class);
        BytemanRequestReceiver receiver = new BytemanRequestReceiver(attachManager);
        CommonPaths paths = mock(CommonPaths.class);
        receiver.bindCommonPaths(paths);
        verify(attachManager).setPaths(paths);
    }

    @Test
    public void testBindAgentIPCService() {
        BytemanAgentAttachManager attachManager = mock(BytemanAgentAttachManager.class);
        BytemanRequestReceiver receiver = new BytemanRequestReceiver(attachManager);
        AgentIPCService ipcService = mock(AgentIPCService.class);
        receiver.bindAgentIpcService(ipcService);
        verify(attachManager).setIpcManager(any(IPCEndpointsManager.class));
        verify(attachManager).setAttacher(any(BytemanAttacher.class));
    }

    @SuppressWarnings("unchecked")
    private void doBasicLoadRulesTestBtmAgentAttached(Submit submit) throws Exception {
        int someValidPid = 3090;
        VmBytemanDAO bytemanDao = mock(VmBytemanDAO.class);
        WriterID wid = mock(WriterID.class);
        when(wid.getWriterID()).thenReturn(SOME_WRITER_ID);
        BytemanRequestReceiver receiver = createReceiver(submit, wid, bytemanDao);
        ArgumentCaptor<VmBytemanStatus> statusCaptor = ArgumentCaptor.forClass(VmBytemanStatus.class);
        WebSocketResponse response = receiver.receive(createRequest(RequestAction.LOAD_RULES, SOME_LISTENPORT_BTM_AGENT_ATTACHED, SOME_RULE, someValidPid));
        verify(bytemanDao).addBytemanStatus(statusCaptor.capture());
        VmBytemanStatus capturedStatus = statusCaptor.getValue();
        assertEquals(SOME_VM_ID, capturedStatus.getJvmId());
        assertEquals(SOME_WRITER_ID, capturedStatus.getAgentId());
        assertEquals(SOME_RULE, capturedStatus.getRule());
        assertEquals(SOME_LISTENPORT_BTM_AGENT_ATTACHED, capturedStatus.getListenPort());
        // verify no helper jars get added on rule submission
        verify(submit, times(0)).addJarsToSystemClassloader(any(List.class));
        verify(submit).addRulesFromResources(any(List.class));
        assertEquals(ResponseType.OK, response.getResponseType());
        assertEquals(sequence, response.getSequenceId());
    }

    private BytemanRequestReceiver createReceiver(final Submit submit, WriterID writerId, VmBytemanDAO dao) {
        BytemanRequestReceiver receiver = new BytemanRequestReceiver() {
            @Override
            protected Submit getSubmit(int port) {
                return submit;
            }

        };
        receiver.bindVmBytemanDao(dao);
        receiver.bindWriterId(writerId);
        return receiver;
    }

    private AgentRequest createRequest(RequestAction action, int listenPort, String someRule, int jvmPid) {
        SortedMap<String, String> params = new TreeMap<>();
        params.put(BytemanRequest.ACTION_PARAM_NAME, Integer.toString(action.getActionId()));
        params.put(BytemanRequest.LISTEN_PORT_PARAM_NAME, Integer.toString(listenPort));
        if (someRule != null) {
            params.put(BytemanRequest.RULE_PARAM_NAME, someRule);
        }
        if (jvmPid != UNSET_JVM_PID) {
            params.put(BytemanRequest.VM_PID_PARAM_NAME, Integer.toString(jvmPid));
        }
        AgentRequest request = new AgentRequest(sequence, BYTEMAN_ACTION, SYSTEM_ID, SOME_VM_ID, params);
        return request;
    }

}

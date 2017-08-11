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

package com.redhat.thermostat.agent.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static com.redhat.thermostat.testutils.Asserts.assertCommandIsRegistered;

import com.redhat.thermostat.agent.Agent;
import com.redhat.thermostat.agent.internal.AgentApplication.ConfigurationCreator;
import com.redhat.thermostat.agent.config.AgentStartupConfiguration;
import com.redhat.thermostat.agent.dao.AgentInfoDAO;
import com.redhat.thermostat.agent.dao.BackendInfoDAO;
import com.redhat.thermostat.backend.BackendRegistry;
import com.redhat.thermostat.common.ExitStatus;
import com.redhat.thermostat.common.LaunchException;
import com.redhat.thermostat.common.Version;
import com.redhat.thermostat.common.cli.Arguments;
import com.redhat.thermostat.common.cli.CommandContext;
import com.redhat.thermostat.common.cli.CommandException;
import com.redhat.thermostat.shared.config.InvalidConfigurationException;
import com.redhat.thermostat.storage.core.WriterID;
import com.redhat.thermostat.testutils.StubBundleContext;

@RunWith(PowerMockRunner.class)
public class AgentApplicationTest {

    private StubBundleContext context;

    private ConfigurationCreator configCreator;
    private ExitStatus exitStatus;
    private WriterID writerId;

    @Before
    public void setUp() throws InvalidConfigurationException {

        context = new StubBundleContext();

        AgentStartupConfiguration config = mock(AgentStartupConfiguration.class);
        when(config.getDBConnectionString()).thenReturn("test string; please ignore");

        configCreator = mock(ConfigurationCreator.class);
        when(configCreator.create()).thenReturn(config);

        AgentInfoDAO agentInfoDAO = mock(AgentInfoDAO.class);
        context.registerService(AgentInfoDAO.class.getName(), agentInfoDAO, null);
        BackendInfoDAO backendInfoDAO = mock(BackendInfoDAO.class);
        context.registerService(BackendInfoDAO.class.getName(), backendInfoDAO, null);
        writerId = mock(WriterID.class);

        exitStatus = mock(ExitStatus.class);
    }

    @After
    public void tearDown() {
        context = null;
        configCreator = null;
        exitStatus = null;
    }

    @PrepareForTest({ FrameworkUtil.class, Agent.class })
    @Test
    public void testAgentStartup() throws CommandException, InterruptedException, InvalidSyntaxException {
        final AgentApplication agent = new AgentApplication(configCreator);
        agent.bindExitStatus(exitStatus);
        agent.bindWriterId(writerId);
        agent.activate(context);
        final CountDownLatch latch = new CountDownLatch(1);
        final CommandException[] ce = new CommandException[1];
        final long timeoutMillis = 5000L;

        Bundle mockBundle = createBundle();
        PowerMockito.mockStatic(FrameworkUtil.class);
        when(FrameworkUtil.getBundle(Agent.class)).thenReturn(mockBundle);
        when(FrameworkUtil.createFilter(any(String.class))).thenReturn(mock(Filter.class));

        startAgentRunThread(timeoutMillis, agent, ce, latch);

        boolean ret = latch.await(timeoutMillis, TimeUnit.MILLISECONDS);
        if (ce[0] != null) {
            throw ce[0];
        }
        if (!ret) {
            fail("Timeout expired!");
        }
    }

    private Bundle createBundle() {
        String qualifier = "201207241700";
        Bundle sysBundle = mock(Bundle.class);
        org.osgi.framework.Version ver = org.osgi.framework.Version
                .parseVersion(String.format(Version.VERSION_NUMBER_FORMAT,
                        1, 2, 3) + "." + qualifier);
        when(sysBundle.getVersion()).thenReturn(ver);
        when(sysBundle.getBundleContext()).thenReturn(context);
        return sysBundle;
    }

    /*
     * Having the PrepareForTest annotation on method level does not seem to
     * deadlock the test, which seems to be more or less reliably reproducible
     * if this annotation is at class level instead. Steps to reproduce the
     * deadlock is:
     * 1. Attach the PrepareForTest annotation to the class (over the test
     *    method)
     * 2. Run the test multiple times. 5-20 times seemed sufficient for me to
     *    make the deadlock show up. This deadlock does not seem to happen
     *    otherwise (can run up to 30 times head-to-head without deadlock).
     *
     */
    @PrepareForTest({ AgentApplication.class })
    @SuppressWarnings("unchecked")
    @Test
    public void verifyBackendRegistryProblemsSetsExitStatus() throws Exception {
        PowerMockito.whenNew(BackendRegistry.class).withParameterTypes(BundleContext.class)
                .withArguments(any(BundleContext.class))
                .thenThrow(InvalidSyntaxException.class);
        final AgentApplication agent = new AgentApplication(configCreator);
        agent.bindExitStatus(exitStatus);
        agent.bindWriterId(writerId);
        agent.activate(context);
        try {
            agent.startAgent(null, null);
        } catch (RuntimeException e) {
            assertEquals(InvalidSyntaxException.class, e.getCause().getClass());
        }
        verify(exitStatus).setExitStatus(ExitStatus.EXIT_ERROR);
    }

    @PrepareForTest({ AgentApplication.class })
    @Test
    public void verifyAgentLaunchExceptionSetsExitStatus() throws Exception {
        PowerMockito.whenNew(BackendRegistry.class).withParameterTypes(BundleContext.class)
                .withArguments(any(BundleContext.class))
                .thenReturn(mock(BackendRegistry.class));
        Agent mockAgent = mock(Agent.class);
        PowerMockito.whenNew(Agent.class).withParameterTypes(BackendRegistry.class,
                AgentStartupConfiguration.class,
                AgentInfoDAO.class, BackendInfoDAO.class, WriterID.class).withArguments(
                any(BackendRegistry.class),
                any(AgentStartupConfiguration.class),
                any(AgentInfoDAO.class), any(BackendInfoDAO.class),
                any(WriterID.class)).thenReturn(mockAgent);
        doThrow(LaunchException.class).when(mockAgent).start();
        final AgentApplication agent = new AgentApplication(configCreator);
        agent.bindExitStatus(exitStatus);
        agent.bindWriterId(writerId);
        agent.activate(context);
        try {
            agent.startAgent(null, null);
        } catch (RuntimeException e) {
            fail("Should not have thrown RuntimeException");
        }
        verify(exitStatus).setExitStatus(ExitStatus.EXIT_ERROR);
    }

    private void startAgentRunThread(final long timoutMillis, final AgentApplication agent, final CommandException[] ce, final CountDownLatch latch) throws InterruptedException {
        Arguments args = mock(Arguments.class);
        final CommandContext commandContext = mock(CommandContext.class);
        when(commandContext.getArguments()).thenReturn(args);

        // Run agent in a new thread so we can timeout on failure
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    latch.countDown();
                    agent.run(commandContext);
                } catch (CommandException e) {
                    ce[0] = e;
                }
            }
        });

        t.start();
    }

    @PrepareForTest({ AgentApplication.class })
    @Test
    public void verifyAgentCommandIsRegistered() {
        final AgentApplication agent = new AgentApplication(configCreator);
        agent.bindExitStatus(exitStatus);
        agent.bindWriterId(writerId);
        agent.activate(context);
        assertCommandIsRegistered(context, "agent", AgentApplication.class);
    }

}
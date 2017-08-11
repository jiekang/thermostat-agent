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

import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.redhat.thermostat.agent.dao.AgentInfoDAO;
import com.redhat.thermostat.agent.dao.BackendInfoDAO;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.framework.BundleContext;

import com.redhat.thermostat.agent.Agent;
import com.redhat.thermostat.agent.config.AgentConfigsUtils;
import com.redhat.thermostat.agent.config.AgentOptionParser;
import com.redhat.thermostat.agent.config.AgentStartupConfiguration;
import com.redhat.thermostat.backend.BackendRegistry;
import com.redhat.thermostat.backend.BackendService;
import com.redhat.thermostat.common.ExitStatus;
import com.redhat.thermostat.common.LaunchException;
import com.redhat.thermostat.common.cli.AbstractStateNotifyingCommand;
import com.redhat.thermostat.common.cli.Arguments;
import com.redhat.thermostat.common.cli.Command;
import com.redhat.thermostat.common.cli.CommandContext;
import com.redhat.thermostat.common.cli.CommandException;
import com.redhat.thermostat.common.cli.CommandRegistry;
import com.redhat.thermostat.common.cli.CommandRegistryImpl;
import com.redhat.thermostat.common.tools.ApplicationState;
import com.redhat.thermostat.common.utils.LoggingUtils;
import com.redhat.thermostat.shared.config.InvalidConfigurationException;
import com.redhat.thermostat.storage.core.WriterID;
import sun.misc.Signal;
import sun.misc.SignalHandler;

@Component(immediate = true)
@Service(value = Command.class)
@SuppressWarnings("restriction")
public final class AgentApplication extends AbstractStateNotifyingCommand {

    /**
     * Property for turning on verbose mode. This is there so as to be able to
     * run integration tests independent of log levels.
     */
    private static final String VERBOSE_MODE_PROPERTY = "thermostat.agent.verbose";
    // Messages printed in verbose mode. Integration tests use this. Be careful
    // when you change those!
    private static final String VERBOSE_MODE_AGENT_STOPPED_MSG = "Agent stopped.";
    private static final String VERBOSE_MODE_AGENT_STARTED_MSG = "Agent started.";

    private static final String SIGINT_NAME = "INT";
    private static final String SIGTERM_NAME = "TERM";

    private static final Logger logger = LoggingUtils.getLogger(AgentApplication.class);

    private final ConfigurationCreator configurationCreator;

    private AgentStartupConfiguration configuration;
    private AgentOptionParser parser;

    private BundleContext context;

    @Reference(bind = "bindExitStatus")
    private ExitStatus exitStatus;
    @Reference(bind = "bindWriterId")
    private WriterID writerId;
    @Reference
    private AgentInfoDAO agentInfoDAO;
    @Reference
    private BackendInfoDAO backendInfoDAO;

    private CommandRegistry reg;

    private CountDownLatch shutdownLatch;

    private CustomSignalHandler handler;

    private AgentApplication instance;

    public AgentApplication() {
        this(new ConfigurationCreator());
    }

    AgentApplication(ConfigurationCreator configurationCreator) {
        this.configurationCreator = configurationCreator;
    }

    @Activate
    public void activate(BundleContext context) {
        this.context = context;
        reg = new CommandRegistryImpl(context);
        instance = this;
        reg.registerCommand("agent", instance);
    }

    @Deactivate
    public void deactivate(BundleContext context) {
        if (instance != null) {
            // Bundle may be shut down *before* deps become available and
            // app is set.
            instance.shutdown(ExitStatus.EXIT_SUCCESS);
        }
        reg.unregisterCommands();
    }

    private void parseArguments(Arguments args) throws InvalidConfigurationException {
        parser = new AgentOptionParser(configuration, args);
        parser.parse();
    }

    private void runAgent(CommandContext ctx) throws CommandException {
        long startTime = System.currentTimeMillis();
        configuration.setStartTime(startTime);

        shutdownLatch = new CountDownLatch(1);
        Agent agent = startAgent(agentInfoDAO, backendInfoDAO);
        handler = new CustomSignalHandler(agent);
        Signal.handle(new Signal(SIGINT_NAME), handler);
        Signal.handle(new Signal(SIGTERM_NAME), handler);

        try {
            // Wait for either SIGINT or SIGTERM
            shutdownLatch.await();
            logger.fine("terminating agent cmd");
        } catch (InterruptedException e) {
            // Ensure proper shutdown if interrupted
            handler.handle(new Signal(SIGINT_NAME));
            return;
        }
    }

    @Override
    public void run(CommandContext ctx) throws CommandException {
        configuration = configurationCreator.create();

        parseArguments(ctx.getArguments());
        if (!parser.isHelp()) {
            runAgent(ctx);
        }
    }

    public void shutdown(int shutDownStatus) {
        // Exit application
        if (shutdownLatch != null) {
            shutdownLatch.countDown();
        }
        this.exitStatus.setExitStatus(shutDownStatus);
        if (shutDownStatus == ExitStatus.EXIT_SUCCESS) {
            getNotifier().fireAction(ApplicationState.STOP);
        } else {
            getNotifier().fireAction(ApplicationState.FAIL);
        }
    }

    private class CustomSignalHandler implements SignalHandler {

        private Agent agent;

        public CustomSignalHandler(Agent agent) {
            this.agent = agent;
        }

        @Override
        public void handle(Signal arg0) {
            try {
                agent.stop();
            } catch (Exception ex) {
                // We don't want any exception to hold back the signal handler, otherwise
                // there will be no way to actually stop Thermostat.
                ex.printStackTrace();
            }
            logger.fine("Agent stopped.");
            // Hook for integration tests. Print a well known message to stdout
            // if verbose mode is turned on via the system property.
            if (Boolean.getBoolean(VERBOSE_MODE_PROPERTY)) {
                System.out.println(VERBOSE_MODE_AGENT_STOPPED_MSG);
            }
            shutdown(ExitStatus.EXIT_SUCCESS);
        }

    }

    Agent startAgent(AgentInfoDAO agentInfoDAO, BackendInfoDAO backendInfoDAO) {
        BackendRegistry backendRegistry = null;
        try {
            backendRegistry = new BackendRegistry(context);

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Could not get BackendRegistry instance.", e);
            shutdown(ExitStatus.EXIT_ERROR);
            // Since this would throw NPE's down the line if we continue in this
            // method, let's fail right and early :)
            throw new RuntimeException(e);
        }

        final Agent agent = new Agent(backendRegistry, configuration, agentInfoDAO, backendInfoDAO, writerId);
        try {
            logger.fine("Starting agent.");
            agent.start();

            context.registerService(BackendService.class, new BackendService(), null);

        } catch (LaunchException le) {
            logger.log(Level.SEVERE,
                    "Agent could not start, probably because a configured backend could not be activated.",
                    le);
            shutdown(ExitStatus.EXIT_ERROR);
        }
        logger.fine("Agent started.");
        // Hook for integration tests. Print a well known message to stdout
        // if verbose mode is turned on via the system property.
        if (Boolean.getBoolean(VERBOSE_MODE_PROPERTY)) {
            System.out.println(VERBOSE_MODE_AGENT_STARTED_MSG);
        }

        logger.info("Agent id: " + agent.getId());
        getNotifier().fireAction(ApplicationState.START, agent.getId());
        return agent;
    }

    static class ConfigurationCreator {
        public AgentStartupConfiguration create() throws InvalidConfigurationException {
            return AgentConfigsUtils.createAgentConfigs();
        }
    }

    @Override
    public boolean isStorageRequired() {
        return false;
    }

    // DS runtime bind methods
    protected void bindExitStatus(ExitStatus status) {
        this.exitStatus = status;
    }

    protected void bindWriterId(WriterID id) {
        this.writerId = id;
    }

}



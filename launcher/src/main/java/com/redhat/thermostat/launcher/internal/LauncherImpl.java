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

package com.redhat.thermostat.launcher.internal;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;

import com.redhat.thermostat.common.ActionListener;
import com.redhat.thermostat.common.ActionNotifier;
import com.redhat.thermostat.common.ExitStatus;
import com.redhat.thermostat.common.Version;
import com.redhat.thermostat.common.cli.AbstractStateNotifyingCommand;
import com.redhat.thermostat.common.cli.Arguments;
import com.redhat.thermostat.common.cli.Command;
import com.redhat.thermostat.common.cli.CommandContext;
import com.redhat.thermostat.common.cli.CommandContextFactory;
import com.redhat.thermostat.common.cli.CommandException;
import com.redhat.thermostat.common.cli.CommandLineArgumentParseException;
import com.redhat.thermostat.common.tools.ApplicationState;
import com.redhat.thermostat.common.utils.LoggingUtils;
import com.redhat.thermostat.launcher.BundleInformation;
import com.redhat.thermostat.launcher.BundleManager;
import com.redhat.thermostat.launcher.Launcher;
import com.redhat.thermostat.shared.config.CommonPaths;
import com.redhat.thermostat.shared.locale.Translate;

/**
 * This class is thread-safe.
 */
public class LauncherImpl implements Launcher {

    private static final Set<String> HELP_SET;
    private static final String HELP_COMMAND_NAME = "help";
    private static final String HELP_OPTION = "--help";
    private static final String INFO_OPTION = "--info";

    private static final Translate<LocaleResources> t = LocaleResources.createLocalizer();
    private static final Logger logger = LoggingUtils.getLogger(LauncherImpl.class);

    static {
        HELP_SET = new HashSet<>();
        HELP_SET.add(HELP_COMMAND_NAME);
        HELP_SET.add(HELP_OPTION);
    }
    private final AtomicInteger usageCount = new AtomicInteger(0);
    private final BundleContext context;
    private final BundleManager registry;
    private final CommandContextFactory cmdCtxFactory;
    private final Version coreVersion;
    private final CommandSource commandSource;
    private final CommandInfoSource commandInfoSource;
    private final CommonPaths paths;
    private final DependencyManager manager;

    public LauncherImpl(BundleContext context, CommandContextFactory cmdCtxFactory, BundleManager registry,
            CommandInfoSource infoSource, CommonPaths paths) {
        this(context, cmdCtxFactory, registry, infoSource, new CommandSource(context),
                new Version(), paths);
    }

    LauncherImpl(BundleContext context, CommandContextFactory cmdCtxFactory, BundleManager registry,
            CommandInfoSource commandInfoSource, CommandSource commandSource,
            Version version, CommonPaths paths) {
        this.context = context;
        this.cmdCtxFactory = cmdCtxFactory;
        this.registry = registry;
        this.coreVersion = version;
        this.commandSource = commandSource;
        this.commandInfoSource = commandInfoSource;
        this.paths = Objects.requireNonNull(paths);
        this.manager = new DependencyManager(paths);

        // We log this in the constructor so as to not log it multiple times when a command invokes
        // run() multiple times. This works since it is a singleton service.
        logger.log(Level.CONFIG, "THERMOSTAT_HOME=" + paths.getSystemThermostatHome().getAbsolutePath());
        logger.log(Level.CONFIG, "USER_THERMOSTAT_HOME=" + paths.getUserThermostatHome().getAbsolutePath());
    }

    @Override
    public void run(String[] args) {
        run(args, null);
    }

    private void help(String[] args, Collection<ActionListener<ApplicationState>> listeners) {
        showVersion();
        runHelpCommandFor("agent");
        runCommandFromArguments(args, listeners);
    }

    @Override
    public void run(String[] args, Collection<ActionListener<ApplicationState>> listeners) {
        usageCount.incrementAndGet();

        try {

            boolean noArgs = hasNoArguments(args);
            if (noArgs) {
                runCommandFromArguments(new String [] {"agent"}, listeners);
                return;
            }

            if (isVersionQuery(args)) {
                showVersion();

            } else if (isInfoQuery(args)) {
                showInfo();

            } else {

                if (args[0].equalsIgnoreCase("help")) {
                    help(args, listeners);

                } else {

                    List<String> realArgs = new ArrayList<>();
                    if (!args[0].equalsIgnoreCase("agent")) {
                        // prepend agent to the command line argument
                        // and execute
                        realArgs.add("agent");
                    }

                    for (String arg : args) {
                        realArgs.add(arg);
                        if (arg.equalsIgnoreCase("--help")) {
                            help(new String[] { "help" }, listeners);
                            return;
                        }
                    }

                    runCommandFromArguments(realArgs.toArray(new String[0]), listeners);
                }
            }
        } catch (NoClassDefFoundError e) {
            // This could mean pom is missing <Private-Package> or <Export-Package> lines.
            // Should be resolved during development, but if we don't catch and print
            // something the error is swallowed and the cause is non-obvious.
            System.err.println("Caught NoClassDefFoundError! Check pom for the missing class: \""
                    + e.getMessage() + "\".  Its package may not be listed.");
            throw e;
        } catch (Throwable e) {
            // Sometimes get exceptions, which get seemingly swallowed, which
            // they really aren't, but the finally block make it seem so.
            e.printStackTrace(System.err);
            throw e;
        } finally {
            args = null;
            boolean isLastLaunch = (usageCount.decrementAndGet() == 0);
            if (isLastLaunch) {
                shutdown();
            }
        }
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void shutdown() throws InternalError {
        try {
            // default to success for exit status
            int exitStatus = ExitStatus.EXIT_SUCCESS;
            if (context != null) {
                ServiceReference exitStatusRef = context.getServiceReference(ExitStatus.class);
                if (exitStatusRef != null) {
                    ExitStatus exitStatusService = (ExitStatus) context.getService(exitStatusRef);
                    exitStatus = exitStatusService.getExitStatus();
                }
            }
            context.getBundle(0).stop();
            System.exit(exitStatus);
        } catch (BundleException e) {
            throw (InternalError) new InternalError().initCause(e);
        }
    }

    private boolean hasNoArguments(String[] args) {
        return args == null || args.length == 0;
    }

    private void runHelpCommandFor(String cmdName) {
        runCommand(HELP_COMMAND_NAME, new String[] { "--", cmdName }, null);
    }

    // package-private for testing
    void runCommandFromArguments(String[] args, Collection<ActionListener<ApplicationState>> listeners) {
        runCommand(args[0], Arrays.copyOfRange(args, 1, args.length), listeners);
    }

    private void runCommand(String cmdName, String[] cmdArgs, Collection<ActionListener<ApplicationState>> listeners) {
        // treat 'foo --help' as 'help foo'
        if (!cmdName.equals(HELP_COMMAND_NAME) && Arrays.asList(cmdArgs).contains(HELP_OPTION)) {
            runCommand(HELP_COMMAND_NAME, new String[] { cmdName } , listeners);
            return;
        }

        try {
            parseArgsAndRunCommand(cmdName, cmdArgs, listeners);
        } catch (CommandException e) {
            cmdCtxFactory.getConsole().getError().println(e.getMessage());
        }
    }

    private void parseArgsAndRunCommand(String cmdName, String[] cmdArgs,
    		Collection<ActionListener<ApplicationState>> listeners) throws CommandException {

        PrintStream out = cmdCtxFactory.getConsole().getOutput();
        PrintStream err = cmdCtxFactory.getConsole().getError();

        CommandInfo cmdInfo;
        try {
            cmdInfo = commandInfoSource.getCommandInfo(cmdName);
        } catch (CommandInfoNotFoundException commandNotFound) {
            runHelpCommandFor(cmdName);
            return;
        }

        try {
            Set<BundleInformation> bundlesToLoad = new HashSet<>(cmdInfo.getBundles());
            logger.log(Level.FINE, "Beginning dependency analysis of " + cmdInfo.getName());
            for (BundleInformation b : cmdInfo.getBundles()) {
                bundlesToLoad.addAll(manager.getDependencies(b));
            }
            if (logger.isLoggable(Level.FINE)) {
                for (BundleInformation b : bundlesToLoad) {
                    logger.log(Level.FINE, "Loading Bundle: " + b);
                }
            }
            registry.loadBundlesByName(new ArrayList<>(bundlesToLoad));
        } catch (BundleException | IOException | IllegalStateException e) {
            // If this happens we definitely need to do something about it, and the
            // trace will be immeasurably helpful in figuring out what is wrong.
            out.println(t.localize(LocaleResources.COMMAND_COULD_NOT_LOAD_BUNDLES, cmdName).getContents());
            e.printStackTrace(out);
            return;
        }

        Command cmd = commandSource.getCommand(cmdName);

        if (cmd == null) {
            err.println(t.localize(LocaleResources.COMMAND_DESCRIBED_BUT_NOT_AVAILALBE, cmdName).getContents());
            return;
        }

        if (listeners != null && cmd instanceof AbstractStateNotifyingCommand) {
            AbstractStateNotifyingCommand basicCmd = (AbstractStateNotifyingCommand) cmd;
            ActionNotifier<ApplicationState> notifier = basicCmd.getNotifier();
            for (ActionListener<ApplicationState> listener : listeners) {
                notifier.addActionListener(listener);
            }
        }
        try {
            Arguments args = parseCommandArguments(cmdArgs, cmdInfo);
            setupLogLevel(args);
            CommandContext ctx = setupCommandContext(cmd, args);
            cmd.run(ctx);
        } catch (CommandLineArgumentParseException e) {
            out.println(e.getMessage());
            runHelpCommandFor(cmdName);
        }
    }

    private Arguments parseCommandArguments(String[] cmdArgs, CommandInfo commandInfo)
            throws CommandLineArgumentParseException {
        CommandLineArgumentsParser cliArgsParser = new CommandLineArgumentsParser();
        cliArgsParser.addOptions(mergeSubcommandOptionsWithParent(commandInfo, cmdArgs));
        cliArgsParser.addSubcommands(flattenSubcommandNames(commandInfo.getSubcommands()));

        return cliArgsParser.parse(cmdArgs);
    }

    // Note: this has the side-effect of adding subcommands' options to the parent command's Options.
    // An Options copy-constructor or Options.remove(Option) method could help us here. The problem with this side-effect
    // is that the subcommand options cannot be removed, only overridden again later, which prevents us from "resetting"
    // the state to cause the Options parser to reject options for subcommands which have not been invoked in the current
    // command line. This means that subcommand-specific non-required options are always accepted when passed to the parent
    // command or any "sibling" subcommand and it is left up to the command implementation to reject or ignore the errant
    // option.
    // See http://icedtea.classpath.org/pipermail/thermostat/2016-October/021198.html
    // package-private for testing only
    Options mergeSubcommandOptionsWithParent(CommandInfo cmdInfo, String[] cmdArgs) {
        Options options = cmdInfo.getOptions();
        PluginConfiguration.Subcommand selectedSubcommand = getSelectedSubcommand(cmdInfo, cmdArgs);

        for (PluginConfiguration.Subcommand subcommand : cmdInfo.getSubcommands()) {
            for (Option option : (Collection<Option>) subcommand.getOptions().getOptions()) {
                Option copy = new Option(option.getOpt(), option.getLongOpt(), option.hasArg(), option.getDescription());
                copy.setRequired(false);
                options.addOption(copy);
            }
        }

        if (selectedSubcommand != null) {
            for (Option option : (Collection<Option>) selectedSubcommand.getOptions().getOptions()) {
                Option copy = new Option(option.getOpt(), option.getLongOpt(), option.hasArg(), option.getDescription());
                copy.setRequired(option.isRequired());
                options.addOption(copy);
            }
        }

        return options;
    }

    // Here we have to take a little bit of a guess about the selected subcommand, if any. We are in the process of
    // setting up all of the required information to hand over to the CommandLineArgumentsParser, which is what returns
    // the CommandLineArguments instance which really does know for sure which subcommand has been selected
    private PluginConfiguration.Subcommand getSelectedSubcommand(CommandInfo cmdInfo, String[] cmdArgs) {
        for (PluginConfiguration.Subcommand subcommand : cmdInfo.getSubcommands()) {
            for (String arg : cmdArgs) {
                if (subcommand.getName().equals(arg)) {
                    return subcommand;
                }
            }
        }
        return null;
    }

    private List<String> flattenSubcommandNames(List<PluginConfiguration.Subcommand> subcommands) {
        List<String> result = new ArrayList<>(subcommands.size());
        for (PluginConfiguration.Subcommand subcommand : subcommands) {
            result.add(subcommand.getName());
        }
        return result;
    }

    private void setupLogLevel(Arguments args) {
        if (args.hasArgument(CommonOptions.LOG_LEVEL_ARG)) {
            String levelOption = args.getArgument(CommonOptions.LOG_LEVEL_ARG);
            setLogLevel(levelOption);
        }
    }

    private void setLogLevel(String levelOption) {
        try {
            Level level = Level.parse(levelOption);
            LoggingUtils.setGlobalLogLevel(level);
        } catch (IllegalArgumentException ex) {
            // Ignore this, use default loglevel.
        }
    }

    private CommandContext setupCommandContext(Command cmd, Arguments args) throws CommandException {
        CommandContext ctx = cmdCtxFactory.createContext(args);
        return ctx;
    }

    private boolean isVersionQuery(String[] args) {
        // don't allow --version in the shell
        return args[0].equals(Version.VERSION_OPTION);
    }

    private void showVersion() {
        // We want to print the version of core
        // thermostat, so we use the no-arg constructor of Version
        cmdCtxFactory.getConsole().getOutput().println(coreVersion.getVersionInfo());
    }

    private boolean isInfoQuery(String[] args) {
        // don't allow --info in the shell
        return args[0].equals(INFO_OPTION);
    }

    private void showInfo() {
        showVersion();

        PrintStream stdOut = cmdCtxFactory.getConsole().getOutput();
        stdOut.println(CommonPaths.THERMOSTAT_HOME + "=" + paths.getSystemThermostatHome().getAbsolutePath());
        stdOut.println(CommonPaths.USER_THERMOSTAT_HOME + "=" + paths.getUserThermostatHome().getAbsolutePath());
    }

}


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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;

import com.redhat.thermostat.common.ActionListener;
import com.redhat.thermostat.common.ExitStatus;
import com.redhat.thermostat.common.Version;
import com.redhat.thermostat.common.cli.Arguments;
import com.redhat.thermostat.common.cli.CommandContext;
import com.redhat.thermostat.common.cli.CommandRegistry;
import com.redhat.thermostat.common.internal.test.TestCommandContextFactory;
import com.redhat.thermostat.common.tools.ApplicationState;
import com.redhat.thermostat.launcher.BundleInformation;
import com.redhat.thermostat.launcher.BundleManager;
import com.redhat.thermostat.launcher.internal.DisallowSystemExitSecurityManager.ExitException;
import com.redhat.thermostat.shared.config.CommonPaths;
import com.redhat.thermostat.testutils.StubBundleContext;

public class LauncherImplTest {

    private static final String name1 = "agent";

    private static SecurityManager secMan;
    private CommandInfo info1;
    private File systemPluginRoot;
    private File systemLibRoot;
    private File userPluginRoot;

    @BeforeClass
    public static void beforeClassSetUp() {
        // Launcher calls System.exit(). This causes issues for unit testing.
        // We work around this by installing a security manager which disallows
        // System.exit() and throws an ExitException instead. This exception in
        // turn is caught by the wrapped launcher call.
        secMan = System.getSecurityManager();
        System.setSecurityManager(new DisallowSystemExitSecurityManager());
    }
    
    @AfterClass
    public static void afterClassTearDown() {
        System.setSecurityManager(secMan);
    }
    
    private static class TestCmd1 implements TestCommand.Handle {

        @Override
        public void run(CommandContext ctx) {
            Arguments args = ctx.getArguments();
            ctx.getConsole().getOutput().print(args.getArgument("arg1") + ", " + args.getArgument("arg2"));
        }
    }

    private TestCommandContextFactory  ctxFactory;
    private StubBundleContext bundleContext;
    private Bundle sysBundle;
    private BundleManager registry;
    private Version version;
    private CommandInfoSource infos;
    private CommandGroupMetadataSource commandGroupMetadataSource;

    private LauncherImpl launcher;

    private CommonPaths paths;

    @Before
    public void setUp() throws CommandInfoNotFoundException, BundleException, IOException {
        setupCommandContextFactory();

        TestCommand cmd1 = new TestCommand(new TestCmd1());
        info1 = mock(CommandInfo.class);
        when(info1.getName()).thenReturn(name1);
        when(info1.getUsage()).thenReturn(" <--arg1 <arg>> [--arg2 <arg>]");
        Options options1 = new Options();
        Option opt1 = new Option(null, "arg1", true, null);
        opt1.setRequired(true);
        options1.addOption(opt1);
        Option opt2 = new Option(null, "arg2", true, null);
        options1.addOption(opt2);
        // cmd1 needs logLevel option since it is used in tests if logLevel
        // option is properly set up
        Option logLevel = new Option("l", "logLevel", true, null);
        options1.addOption(logLevel);
        when(info1.getSummary()).thenReturn("description 1");
        when(info1.getDescription()).thenReturn("description 1");
        when(info1.getOptions()).thenReturn(options1);

        CommandInfo helpCommandInfo = mock(CommandInfo.class);
        when(helpCommandInfo.getName()).thenReturn("help");
        when(helpCommandInfo.getSummary()).thenReturn("print help information");
        when(helpCommandInfo.getBundles()).thenReturn(new ArrayList<BundleInformation>());
        when(helpCommandInfo.getOptions()).thenReturn(new Options());
        when(helpCommandInfo.getUsage()).thenReturn("thermostat help");

        HelpCommand helpCommand = new HelpCommand();

        CommandRegistry reg = ctxFactory.getCommandRegistry();
        reg.registerCommand("help", helpCommand);
        reg.registerCommand(name1, cmd1);

        infos = mock(CommandInfoSource.class);
        bundleContext.registerService(CommandInfoSource.class, infos, null);
        when(infos.getCommandInfo(name1)).thenReturn(info1);
        when(infos.getCommandInfo("help")).thenReturn(helpCommandInfo);

        Collection<CommandInfo> infoList = new ArrayList<CommandInfo>();
        infoList.add(helpCommandInfo);
        infoList.add(info1);
        
        when(infos.getCommandInfos()).thenReturn(infoList);

        helpCommand.setCommandInfoSource(infos);

        commandGroupMetadataSource = mock(CommandGroupMetadataSource.class);
        when(commandGroupMetadataSource.getCommandGroupMetadata()).thenReturn(Collections.<String, PluginConfiguration.CommandGroupMetadata>emptyMap());
        helpCommand.setCommandGroupMetadataSource(commandGroupMetadataSource);

        registry = mock(BundleManager.class);

        version = mock(Version.class);

        paths = mock(CommonPaths.class);
        File userConfigFile = mock(File.class);
        when(userConfigFile.isFile()).thenReturn(false);
        when(paths.getUserClientConfigurationFile()).thenReturn(userConfigFile);
        File setupFile = mock(File.class);
        when(setupFile.exists()).thenReturn(true);
        when(paths.getUserSetupCompleteStampFile()).thenReturn(setupFile);

        userPluginRoot = Files.createTempDirectory("userPluginRoot").toFile();
        systemPluginRoot = Files.createTempDirectory("systemPluginRoot").toFile();
        systemLibRoot = Files.createTempDirectory("systemLibRoot").toFile();
        when(paths.getUserPluginRoot()).thenReturn(userPluginRoot);
        when(paths.getSystemPluginRoot()).thenReturn(systemPluginRoot);
        when(paths.getSystemLibRoot()).thenReturn(systemLibRoot);

        when(paths.getSystemThermostatHome()).thenReturn(mock(File.class));
        when(paths.getUserThermostatHome()).thenReturn(mock(File.class));
        launcher = new LauncherImpl(bundleContext, ctxFactory, registry, infos, new CommandSource(bundleContext),
                version, paths);
    }

    @After
    public void tearDown() {
        try {
            deleteDirectory(systemLibRoot.toPath());
            deleteDirectory(systemPluginRoot.toPath());
            deleteDirectory(userPluginRoot.toPath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupCommandContextFactory() {
        sysBundle = mock(Bundle.class);
        bundleContext = new StubBundleContext();
        bundleContext.setBundle(0, sysBundle);
        ctxFactory = new TestCommandContextFactory(bundleContext);
    }

    @Test
    public void testMain() {
        runAndVerifyCommand(new String[] {name1, "--arg1", "Hello", "--arg2", "World"}, "Hello, World");
    }

    @Test
    public void testMainNoArgs() {
        String expected = "Missing required option: --arg1\n" +
                          "usage: thermostat  <--arg1 <arg>> [--arg2 <arg>]\n" +
                          "                  description 1\n" +
                          "     --arg1 <arg>\n" +
                          "     --arg2 <arg>\n" +
                          "     --help              show usage of command\n" +
                          "  -l,--logLevel <arg>\n";
        runAndVerifyCommand(new String[0], expected);
    }

    @Test
    public void verifySetLogLevel() {
        runAndVerifyCommand(new String[] {name1, "--logLevel", "WARNING", "--arg1", "Hello", "--arg2", "World"}, "Hello, World");
        Logger globalLogger = Logger.getLogger("com.redhat.thermostat");
        assertEquals(Level.WARNING, globalLogger.getLevel());
    }

    @Test
    public void testMainBadCommand1() {
        when(infos.getCommandInfo("--help")).thenThrow(new CommandInfoNotFoundException("--help"));

        String expected = "null\n" +
                "usage: thermostat  <--arg1 <arg>> [--arg2 <arg>]\n" +
                "                  description 1\n" +
                "     --arg1 <arg>\n" +
                "     --arg2 <arg>\n" +
                "     --help              show usage of command\n" +
                "  -l,--logLevel <arg>\n" +
                " --version                display the version of the current thermostat installation\n" +
                " --print-osgi-info        print debug information related to the OSGi framework's boot/shutdown process\n" +
                " --ignore-bundle-versions ignore exact bundle versions and use whatever version is available\n" +
                " --boot-delegation        boot delegation string passed on to the OSGi framework\n\n";
        runAndVerifyCommand(new String[] {"--help"}, expected);
    }

    @Test
    public void testMainBadCommand2() {
        when(infos.getCommandInfo("-help")).thenThrow(new CommandInfoNotFoundException("-help"));

        String expected = "Could not parse options: Unrecognized option: -help\n" +
                "usage: thermostat  <--arg1 <arg>> [--arg2 <arg>]\n" +
                "                  description 1\n" +
                "     --arg1 <arg>\n" +
                "     --arg2 <arg>\n" +
                "     --help              show usage of command\n" +
                "  -l,--logLevel <arg>\n";
        runAndVerifyCommand(new String[] {"-help"}, expected);
    }

    @Test
    public void testMainBadCommand3() {
        when(infos.getCommandInfo("foobarbaz")).thenThrow(new CommandInfoNotFoundException("foobarbaz"));

        String expected = "Missing required option: --arg1\n" +
                "usage: thermostat  <--arg1 <arg>> [--arg2 <arg>]\n" +
                "                  description 1\n" +
                "     --arg1 <arg>\n" +
                "     --arg2 <arg>\n" +
                "     --help              show usage of command\n" +
                "  -l,--logLevel <arg>\n";
        runAndVerifyCommand(new String[] {"foobarbaz"}, expected);
    }

    @Test
    public void testMainBadCommand4() {
        when(infos.getCommandInfo("foo")).thenThrow(new CommandInfoNotFoundException("foo"));

        String expected = "Could not parse options: Unrecognized option: --bar\n" +
                "usage: thermostat  <--arg1 <arg>> [--arg2 <arg>]\n" +
                "                  description 1\n" +
                "     --arg1 <arg>\n" +
                "     --arg2 <arg>\n" +
                "     --help              show usage of command\n" +
                "  -l,--logLevel <arg>\n";
        runAndVerifyCommand(new String[] {"foo",  "--bar", "baz"}, expected);
    }

    @Test
    public void testSubcommandOptionRecognized() {
        PluginConfiguration.Subcommand subInfo = mock(PluginConfiguration.Subcommand.class);
        when(subInfo.getName()).thenReturn("sub");
        when(subInfo.getDescription()).thenReturn("subcommand description");

        Options subOptions = mock(Options.class);
        Option optOption = new Option("o", "opt", false, "mock opt option");
        optOption.setRequired(false);
        when(subOptions.getOptions()).thenReturn(Collections.singleton(optOption));
        when(subInfo.getOptions()).thenReturn(subOptions);

        when(info1.getSubcommands()).thenReturn(Collections.singletonList(subInfo));
        String expected = "foo, bar";
        runAndVerifyCommand(new String[] {"test1", "sub", "--opt", "--arg1", "foo", "--arg2", "bar"}, expected);
    }

    @Test
    public void testSubcommandOptionRequiredAndNotProvided() {
        PluginConfiguration.Subcommand subInfo = mock(PluginConfiguration.Subcommand.class);
        when(subInfo.getName()).thenReturn("sub");
        when(subInfo.getDescription()).thenReturn("subcommand description");

        Options subOptions = mock(Options.class);
        Option optOption = new Option("o", "opt", false, "mock opt option");
        optOption.setRequired(true);
        when(subOptions.getOptions()).thenReturn(Collections.singleton(optOption));
        when(subInfo.getOptions()).thenReturn(subOptions);

        when(info1.getSubcommands()).thenReturn(Collections.singletonList(subInfo));
        String expected = "Missing required option: -o\n" +
                "usage: thermostat  <--arg1 <arg>> [--arg2 <arg>]\n" +
                "                  description 1\n" +
                "     --arg1 <arg>\n" +
                "     --arg2 <arg>\n" +
                "     --help              show usage of command\n" +
                "  -l,--logLevel <arg>\n" +
                "  -o,--opt               mock opt option\n" +
                "\n" +
                "Subcommands:\n" +
                "\n" +
                "sub:\n" +
                "subcommand description\n\n\n";
        runAndVerifyCommand(new String[] {"test1", "sub", "--arg1", "foo", "--arg2", "bar"}, expected);
    }

    @Test
    public void testSubcommandOptionRequired() {
        PluginConfiguration.Subcommand subInfo = mock(PluginConfiguration.Subcommand.class);
        when(subInfo.getName()).thenReturn("sub");
        when(subInfo.getDescription()).thenReturn("subcommand description");

        Options subOptions = mock(Options.class);
        Option optOption = new Option("o", "opt", false, "mock opt option");
        optOption.setRequired(true);
        when(subOptions.getOptions()).thenReturn(Collections.singleton(optOption));
        when(subInfo.getOptions()).thenReturn(subOptions);

        when(info1.getSubcommands()).thenReturn(Collections.singletonList(subInfo));
        String expected = "foo, bar";
        runAndVerifyCommand(new String[] {"test1", "sub", "--opt", "--arg1", "foo", "--arg2", "bar"}, expected);
    }

    @Test
    public void testSubcommandOptionNotRequiredIfSubcommandNotInvoked() {
        PluginConfiguration.Subcommand subInfo = mock(PluginConfiguration.Subcommand.class);
        when(subInfo.getName()).thenReturn("sub");
        when(subInfo.getDescription()).thenReturn("subcommand description");

        Options subOptions = mock(Options.class);
        Option optOption = new Option("o", "opt", false, "mock opt option");
        optOption.setRequired(true);
        when(subOptions.getOptions()).thenReturn(Collections.singleton(optOption));
        when(subInfo.getOptions()).thenReturn(subOptions);

        when(info1.getSubcommands()).thenReturn(Collections.singletonList(subInfo));
        String expected = "foo, bar";
        runAndVerifyCommand(new String[] {"test1", "--arg1", "foo", "--arg2", "bar"}, expected);
    }

    // This tests the case where we have a parent command "test1" with subcommand "sub",
    // which has a subcommand-specific option -o/--opt which is not required. "sub" is not
    // invoked, but --opt is passed anyway.
    // Due to limitations in our options processing, this case cannot be easily rejected as
    // invalid. It is accepted instead and the option passed on to the parent command, which
    // must on its own decide to reject or ignore the errant --opt.
    // See http://icedtea.classpath.org/pipermail/thermostat/2016-October/021198.html
    @Test
    public void testSubcommandOptionStillRecognizedWhenSubcommandNotInvoked() {
        PluginConfiguration.Subcommand subInfo = mock(PluginConfiguration.Subcommand.class);
        when(subInfo.getName()).thenReturn("sub");
        when(subInfo.getDescription()).thenReturn("subcommand description");

        Options subOptions = mock(Options.class);
        Option optOption = new Option("o", "opt", false, "mock opt option");
        optOption.setRequired(true);
        when(subOptions.getOptions()).thenReturn(Collections.singleton(optOption));
        when(subInfo.getOptions()).thenReturn(subOptions);

        when(info1.getSubcommands()).thenReturn(Collections.singletonList(subInfo));
        String expected = "foo, bar";
        runAndVerifyCommand(new String[] {"test1", "--opt", "--arg1", "foo", "--arg2", "bar"}, expected);
    }

    // This tests the case where we have a parent command "test1" with subcommand "sub",
    // which has a subcommand-specific option -o/--opt which is not required. "sub" is not
    // invoked, but --opt is passed anyway.
    // Due to limitations in our options processing, this case cannot be easily rejected as
    // invalid. It is accepted instead and the option passed on to the parent command, which
    // must on its own decide to reject or ignore the errant --opt.
    // See http://icedtea.classpath.org/pipermail/thermostat/2016-October/021198.html
    @Test
    public void testSubcommandOptionOverridesParentOption() {
        PluginConfiguration.Subcommand subInfo = mock(PluginConfiguration.Subcommand.class);
        when(subInfo.getName()).thenReturn("sub");
        when(subInfo.getDescription()).thenReturn("subcommand description");

        Options subOptions = mock(Options.class);
        // parent command also has a --arg1 option which *does* take an argument
        Option optOption = new Option(null, "arg1", false, null);
        optOption.setRequired(true);
        when(subOptions.getOptions()).thenReturn(Collections.singleton(optOption));
        when(subInfo.getOptions()).thenReturn(subOptions);

        when(info1.getSubcommands()).thenReturn(Collections.singletonList(subInfo));
        String expected = "null, bar";
        runAndVerifyCommand(new String[] {"test1", "sub", "--arg1", "foo", "--arg2", "bar"}, expected);
    }

    @Test
    public void testSelectedSubcommandOptionsNotOverriddenByOtherSubcommands() {
        PluginConfiguration.Subcommand subInfo1 = mock(PluginConfiguration.Subcommand.class);
        when(subInfo1.getName()).thenReturn("sub1");
        when(subInfo1.getDescription()).thenReturn("subcommand description");

        PluginConfiguration.Subcommand subInfo2 = mock(PluginConfiguration.Subcommand.class);
        when(subInfo2.getName()).thenReturn("sub2");
        when(subInfo2.getDescription()).thenReturn("subcommand description");

        Options subOptions1 = mock(Options.class);
        Options subOptions2 = mock(Options.class);
        Option requiredOption = new Option(null, "option", false, null);
        requiredOption.setRequired(true);
        Option nonRequiredOption = new Option(null, "option", false, null);
        nonRequiredOption.setRequired(false);
        when(subOptions1.getOptions()).thenReturn(Collections.singletonList(requiredOption));
        when(subOptions2.getOptions()).thenReturn(Collections.singletonList(nonRequiredOption));
        when(subInfo1.getOptions()).thenReturn(subOptions1);
        when(subInfo2.getOptions()).thenReturn(subOptions2);

        when(info1.getSubcommands()).thenReturn(Arrays.asList(subInfo1, subInfo2));

        Options options = launcher.mergeSubcommandOptionsWithParent(info1, new String[] { "sub1" });
        assertTrue(options.getOption("option").isRequired());
    }

    @Test
    public void testBadOption() {
        String expected = "Could not parse options: Unrecognized option: --argNotAccepted\n"
                        + "usage: thermostat  <--arg1 <arg>> [--arg2 <arg>]\n"
                        + "                  description 1\n"
                        + "     --arg1 <arg>\n"
                        + "     --arg2 <arg>\n"
                        + "     --help              show usage of command\n"
                        + "  -l,--logLevel <arg>\n";
        runAndVerifyCommand(new String[] {"agent", "--arg1", "arg1value", "--argNotAccepted"}, expected);
    }

    @Test
    public void testMissingRequiredOption() {
        String expected = "Missing required option: --arg1\n"
                + "usage: thermostat  <--arg1 <arg>> [--arg2 <arg>]\n"
                + "                  description 1\n"
                + "     --arg1 <arg>\n"
                + "     --arg2 <arg>\n"
                + "     --help              show usage of command\n"
                + "  -l,--logLevel <arg>\n";
        runAndVerifyCommand(new String[] {"test1"}, expected);
    }

    @Test
    public void testOptionMissingRequiredArgument() {
        String expected = "Could not parse options: Missing argument for option: arg1\n"
                + "usage: thermostat  <--arg1 <arg>> [--arg2 <arg>]\n"
                + "                  description 1\n"
                + "     --arg1 <arg>\n"
                + "     --arg2 <arg>\n"
                + "     --help              show usage of command\n"
                + "  -l,--logLevel <arg>\n";
        runAndVerifyCommand(new String[] {"test1", "--arg1"}, expected);
    }

    private void runAndVerifyCommand(String[] args, String expected) {
        wrappedRun(launcher, args);
        assertEquals(expected, ctxFactory.getOutput());
    }
    
    private void wrappedRun(LauncherImpl launcher, String[] args) {
        wrappedRun(launcher, args, null);
    }
    
    private void wrappedRun(LauncherImpl launcher, String[] args, Collection<ActionListener<ApplicationState>> listeners) {
        try {
            launcher.run(args, listeners);
        } catch (ExitException e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void verifyVersionInfoQuery() {
        String versionString = "foo bar baz";

        String expectedVersionInfo = versionString + "\n";

        when(version.getVersionInfo()).thenReturn(versionString);

        wrappedRun(launcher, new String[] {Version.VERSION_OPTION});

        assertEquals(expectedVersionInfo, ctxFactory.getOutput());
    }
    
    /**
     * Tests if USER_THERMOSTAT_HOME and THERMOSTAT_HOME gets logged correctly
     * on instantiation.
     */
    @Test
    public void verifyLogsUserHomeThermostatHomeOnInstantiation() {
        Logger logger = Logger.getLogger("com.redhat.thermostat");
        logger.setLevel(Level.ALL);
        assertTrue(logger.getLevel() == Level.ALL);
        TestLogHandler handler = new TestLogHandler();
        logger.addHandler(handler);
        CommonPaths logPaths = mock(CommonPaths.class);
        when(logPaths.getUserThermostatHome()).thenReturn(mock(File.class));
        when(logPaths.getSystemThermostatHome()).thenReturn(mock(File.class));
        when(logPaths.getUserPluginRoot()).thenReturn(userPluginRoot);
        when(logPaths.getSystemLibRoot()).thenReturn(systemLibRoot);
        when(logPaths.getSystemPluginRoot()).thenReturn(systemPluginRoot);
        
        try {
            assertFalse(handler.loggedThermostatHome);
            assertFalse(handler.loggedUserHome);
            // this should trigger logging
            new LauncherImpl(bundleContext, ctxFactory, registry,
                    infos, new CommandSource(bundleContext),
                    version, logPaths);
            assertTrue(handler.loggedThermostatHome);
            assertTrue(handler.loggedUserHome);
            verify(logPaths).getUserThermostatHome();
            verify(logPaths).getSystemThermostatHome();
        } finally {
            // clean-up in order to avoid logs for other tests.
            logger.removeHandler(handler);
            handler = null;
            logger.setLevel(Level.INFO);
        }
    }

    @Test
    public void verifyShutdown() throws BundleException {
        wrappedRun(launcher, new String[] { "test1" });

        verify(sysBundle).stop();
    }
    
    @Test
    public void verifySetExitStatus() {
        try {
            launcher.run(new String[] { "test1" });
            fail("Should have called System.exit()");
        } catch (ExitException e) {
            // pass, by default launcher exits with an exit status
            // of 0.
            assertEquals(ExitStatus.EXIT_SUCCESS, e.getExitStatus());
        }
    }

    private static class TestLogHandler extends Handler {
        
        private boolean loggedThermostatHome;
        private boolean loggedUserHome;
        
        @Override
        public void close() throws SecurityException {
            // nothing
        }

        @Override
        public void flush() {
            // nothing
        }

        @Override
        public void publish(LogRecord record) {
            String logMessage = record.getMessage();
            System.out.println(logMessage);
            if (record.getLevel() == Level.CONFIG && logMessage.startsWith("THERMOSTAT_HOME")) {
                loggedThermostatHome = true;
            }
            if (record.getLevel() == Level.CONFIG && logMessage.startsWith("USER_THERMOSTAT_HOME")) {
                loggedUserHome = true;
            }
        }
        
    }

    void deleteDirectory(Path dir) throws IOException {
        Files.walkFileTree(dir, new FileVisitor<Path>() {

            @Override
            public FileVisitResult preVisitDirectory(Path dir,
                                                     BasicFileAttributes attrs) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file,
                                             BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc)
                    throws IOException {
                exc.printStackTrace();
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc)
                    throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

}


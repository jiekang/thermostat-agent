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

package com.redhat.thermostat.shared.config.internal;

import static com.redhat.thermostat.testutils.TestUtils.deleteRecursively;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermissions;

import com.redhat.thermostat.shared.config.OS;

import com.redhat.thermostat.shared.config.CommonPaths;
import com.redhat.thermostat.shared.config.InvalidConfigurationException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

public class CommonPathsImplTest {

    private static final char s = File.separatorChar;
    private static final String THERMOSTAT_HOME_PROPERTY = "THERMOSTAT_HOME";
    private static final String USER_THERMOSTAT_HOME_PROPERTY = "USER_THERMOSTAT_HOME";
    private static final String THERMOSTAT_SYSTEM_USER_PROPERTY = "THERMOSTAT_SYSTEM_USER";

    private String savedHome, savedUserHome, savedSystemUser;

    @Before
    public void setUp() {
        savedHome = System.clearProperty(THERMOSTAT_HOME_PROPERTY);
        savedUserHome = System.clearProperty(USER_THERMOSTAT_HOME_PROPERTY);
        savedSystemUser = System.clearProperty(THERMOSTAT_SYSTEM_USER_PROPERTY);
    }

    @After
    public void tearDown() {
        restoreProperty(THERMOSTAT_HOME_PROPERTY, savedHome);
        restoreProperty(USER_THERMOSTAT_HOME_PROPERTY, savedUserHome);
        restoreProperty(THERMOSTAT_SYSTEM_USER_PROPERTY, savedSystemUser);
    }

    private void restoreProperty(String key, String oldValue) {
        if (oldValue == null) {
            System.clearProperty(key);
        } else {
            System.setProperty(key, oldValue);
        }
    }

    private String setupTempDir(String tempPrefix, String propertyKey, FileAttribute<?>... attrs) throws IOException {
        File tmpDir = Files.createTempDirectory(tempPrefix, attrs).toFile();
        if (!tmpDir.exists()) {
            tmpDir.mkdirs();
        }
        String path = tmpDir.getAbsolutePath();
        System.setProperty(propertyKey, path);
        return path;
    }

    private void deleteTempDir(String tmpPath) throws IOException {
        File tmpDir = new File(tmpPath);
        if (tmpDir.exists()) {
            deleteRecursively(tmpDir);
        }
    }

    private String concatPath(String prefix, String ... pathElements) {
        StringBuilder sb = new StringBuilder(prefix);
        for (String pathElement : pathElements) {
            sb.append(s);
            sb.append(pathElement);
        }
        return sb.toString();
    }

    @Test
    public void testSystemLocations() throws InvalidConfigurationException, IOException {
        String thermostatHome = null;
        try {
            thermostatHome = setupTempDir("CommonPathsImplTest.testSystemLocations",
                    THERMOSTAT_HOME_PROPERTY);
            CommonPaths config = new CommonPathsImpl();

            Assert.assertEquals(thermostatHome,
                    config.getSystemThermostatHome().getAbsolutePath());
            Assert.assertEquals(concatPath(thermostatHome, "libs", "native"),
                    config.getSystemNativeLibsRoot().getAbsolutePath());
            Assert.assertEquals(concatPath(thermostatHome, "etc"),
                    config.getSystemConfigurationDirectory().getAbsolutePath());
            Assert.assertEquals(concatPath(thermostatHome, "libs"),
                    config.getSystemLibRoot().getAbsolutePath());
            Assert.assertEquals(concatPath(thermostatHome, "plugins"),
                    config.getSystemPluginRoot().getAbsolutePath());
            Assert.assertEquals(concatPath(thermostatHome, "etc", "plugins.d"),
                    config.getSystemPluginConfigurationDirectory().getAbsolutePath());
        } finally {
            if (thermostatHome != null) {
                deleteTempDir(thermostatHome);
            }
        }
    }

    @Test
    public void testUserLocations() throws InvalidConfigurationException, IOException {
        String thermostatHome = null;
        try {
            thermostatHome = setupTempDir("CommonPathsImplTest.testUserLocations",
                    THERMOSTAT_HOME_PROPERTY);
            String userHome = getUserHome();
            CommonPaths config = new CommonPathsImpl();

            Assert.assertEquals(concatPath(userHome, "etc", "agent.properties"),
                    config.getUserAgentConfigurationFile().getAbsolutePath());
            Assert.assertEquals(concatPath(userHome, "etc", "agent.auth"),
                    config.getUserAgentAuthConfigFile().getAbsolutePath());
            Assert.assertEquals(concatPath(userHome, "etc", "db.properties"),
                    config.getUserStorageConfigurationFile().getAbsolutePath());
            Assert.assertEquals(concatPath(userHome, "data", "db"),
                    config.getUserStorageDirectory().getAbsolutePath());
            Assert.assertEquals(concatPath(userHome, "run", "db.pid"),
                    config.getUserStoragePidFile().getAbsolutePath());
            Assert.assertEquals(concatPath(userHome, "logs", "db.log"),
                    config.getUserStorageLogFile().getAbsolutePath());
            Assert.assertEquals(concatPath(userHome, "data", "plugins"),
                    config.getUserPluginRoot().getAbsolutePath());
            Assert.assertEquals(concatPath(userHome, "etc", "plugins.d"),
                    config.getUserPluginConfigurationDirectory().getAbsolutePath());
        } finally {
            if (thermostatHome != null) {
                deleteTempDir(thermostatHome);
            }
        }
    }
    
    // Duplicated from CommonPathsImpl.UnprivilegedUserDirectories
    private String getUserHome() {
        String userHome = System.getProperty(USER_THERMOSTAT_HOME_PROPERTY);
        if (userHome == null) {
            userHome = System.getenv(USER_THERMOSTAT_HOME_PROPERTY);
        }
        if (userHome == null) {
            userHome = System.getProperty("user.home") + s + ".thermostat-ng";
        }
        return userHome;
    }

    @Test
    public void testPrivilegedUserLocations() throws InvalidConfigurationException, IOException {
        String thermostatHomeAndFakeRoot = null;
        try {
            // It's not safe to create directories in this setup. It's treated
            // as a prefix "/tmp/foo" for example in this config. Creating
            // directories in a random prefix is not a good idea. Not even for
            // tests. Skip the test in this case
            String userHomeEnv = System.getenv(USER_THERMOSTAT_HOME_PROPERTY);
            if (userHomeEnv != null) {
                System.out.println("USER_TEHMOSTAT_HOME set to: " + userHomeEnv + " skipping test!");
                return;
            }
            thermostatHomeAndFakeRoot = setupTempDir("CommonPathsImplTest.testPrivilegedUserLocations",
                    THERMOSTAT_HOME_PROPERTY);
            System.setProperty(THERMOSTAT_SYSTEM_USER_PROPERTY, "");
            CommonPaths config = new CommonPathsImpl(thermostatHomeAndFakeRoot);

            Assert.assertEquals(concatPath(thermostatHomeAndFakeRoot, "etc", "thermostat", "agent.properties"),
                    config.getUserAgentConfigurationFile().getAbsolutePath());
            Assert.assertEquals(concatPath(thermostatHomeAndFakeRoot, "etc", "thermostat", "agent.auth"),
                    config.getUserAgentAuthConfigFile().getAbsolutePath());
            Assert.assertEquals(concatPath(thermostatHomeAndFakeRoot, "etc", "thermostat", "db.properties"),
                    config.getUserStorageConfigurationFile().getAbsolutePath());
            Assert.assertEquals(concatPath(thermostatHomeAndFakeRoot, "var", "lib", "thermostat", "db"),
                    config.getUserStorageDirectory().getAbsolutePath());
            Assert.assertEquals(concatPath(thermostatHomeAndFakeRoot, "var", "run", "thermostat", "db.pid"),
                    config.getUserStoragePidFile().getAbsolutePath());
            Assert.assertEquals(concatPath(thermostatHomeAndFakeRoot, "var", "log", "thermostat", "db.log"),
                    config.getUserStorageLogFile().getAbsolutePath());
            Assert.assertEquals(concatPath(thermostatHomeAndFakeRoot, "var", "lib", "thermostat", "plugins"),
                    config.getUserPluginRoot().getAbsolutePath());
            Assert.assertEquals(concatPath(thermostatHomeAndFakeRoot, "etc", "thermostat", "plugins.d"),
                    config.getUserPluginConfigurationDirectory().getAbsolutePath());
        } finally {
            if (thermostatHomeAndFakeRoot != null) {
                deleteTempDir(thermostatHomeAndFakeRoot);
            }
        }
    }

    @Test
    public void testPrivilegedUserLocationsWithPrefix() throws InvalidConfigurationException, IOException {
        String thermostatHome = null;
        String prefix = null;
        try {
            thermostatHome = setupTempDir("CommonPathsImplTest.testPrivilegedUserLocationsWithPrefix",
                    THERMOSTAT_HOME_PROPERTY);
            prefix = setupTempDir("CommonPathsImplTest.testPrivilegedUserLocationsWithPrefix_prefix",
                    USER_THERMOSTAT_HOME_PROPERTY);
            System.setProperty(THERMOSTAT_SYSTEM_USER_PROPERTY, "");
            CommonPaths config = new CommonPathsImpl();

            Assert.assertEquals(concatPath(prefix, "etc", "thermostat", "agent.properties"),
                    config.getUserAgentConfigurationFile().getAbsolutePath());
            Assert.assertEquals(concatPath(prefix, "etc", "thermostat", "agent.auth"),
                    config.getUserAgentAuthConfigFile().getAbsolutePath());
            Assert.assertEquals(concatPath(prefix, "etc", "thermostat", "db.properties"),
                    config.getUserStorageConfigurationFile().getAbsolutePath());
            Assert.assertEquals(concatPath(prefix, "var", "lib", "thermostat", "db"),
                    config.getUserStorageDirectory().getAbsolutePath());
            Assert.assertEquals(concatPath(prefix, "var", "run", "thermostat", "db.pid"),
                    config.getUserStoragePidFile().getAbsolutePath());
            Assert.assertEquals(concatPath(prefix, "var", "log", "thermostat", "db.log"),
                    config.getUserStorageLogFile().getAbsolutePath());
            Assert.assertEquals(concatPath(prefix, "var", "lib", "thermostat", "plugins"),
                    config.getUserPluginRoot().getAbsolutePath());
            Assert.assertEquals(concatPath(prefix, "etc", "thermostat", "plugins.d"),
                    config.getUserPluginConfigurationDirectory().getAbsolutePath());
        } finally {
            if (thermostatHome != null) {
                deleteTempDir(thermostatHome);
            }
            if (prefix != null) {
                deleteTempDir(prefix);
            }
        }
    }

    @Test
    public void instantiationThrowsExceptionUndefinedThermostatHome() {
        // This test does not make sense when THERMOSTAT_HOME env var is defined
        // externally. Ignore in this case.
        Assume.assumeTrue(System.getenv(THERMOSTAT_HOME_PROPERTY) == null);
        try {
            new CommonPathsImpl();
            // The web archive uses this. See WebStorageEndPoint#init();
            fail("Should have thrown InvalidConfigurationException");
        } catch (InvalidConfigurationException e) {
            // pass
        }
    }

    @Test
    public void instantiationThrowsExceptionThermostatHomeNotExists() throws IOException {
        String thermostatHome = null;
        try  {
            thermostatHome = setupTempDir("CommonPathsImplTest.testPrivilegedUserLocationsWithPrefix",
                    THERMOSTAT_HOME_PROPERTY);
        } finally {
            if (thermostatHome != null) {
                deleteTempDir(thermostatHome);
            }
        }

        try {
            new CommonPathsImpl();
            fail("Should have thrown InvalidConfigurationException");
        } catch (InvalidConfigurationException e) {
            // pass
        }
    }

    @Test
    public void instantiationThrowsExceptionThermostatHomeNotReadable() throws IOException {
        Assume.assumeTrue(OS.IS_UNIX);
        String thermostatHome = null;
        try {
            thermostatHome = setupTempDir("CommonPathsImplTest.instantiationThrowsExceptionThermostatHomeNotReadable",
                    THERMOSTAT_HOME_PROPERTY,
                    PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("-wx--x---")));
            new CommonPathsImpl();
            fail("Should have thrown InvalidConfigurationException");
        } catch (InvalidConfigurationException e) {
            // pass
        } catch (UnsupportedOperationException e) {
            // TODO - fix permissions for Windows
            // Windows uses ACLs, not Posix permissions.
            // All the Thermostat code currently assumes posix permissions.
            throw e;
        } finally {
            if (thermostatHome != null) {
                // Don't use deleteTempDir because it can't list contents of unreadable dir
                File tmpDir = new File(thermostatHome);
                if (tmpDir.exists()) {
                    tmpDir.delete();
                }
            }
        }
    }
}


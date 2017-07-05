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

package com.redhat.thermostat.agent.config;

import com.redhat.thermostat.storage.config.StartupConfiguration;

public class AgentStartupConfiguration implements StartupConfiguration {

    private boolean purge;
    private String dbUrl;
    private long startTime;

    private boolean keycloakEnabled;
    private String keycloakUrl;
    private String keycloakRealm;
    private String keycloakClient;
    private String keycloakUsername;
    private String keycloakPassword;
    
    AgentStartupConfiguration() {
    }
    
    @Override
    public String getDBConnectionString() {
        return dbUrl;
    }

    public void setDatabaseURL(String url) {
        this.dbUrl = url;
    }
    
    // TODO: that should be a friend, we only want the Service to set this value
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }
    
    public long getStartTime() {
        return startTime;
    }

    void setPurge(boolean purge) {
        this.purge = purge;
    }
    
    public boolean purge() {
        return purge;
    }

    public String getKeycloakUrl() {
        return keycloakUrl;
    }

    public void setKeycloakUrl(String keycloakUrl) {
        this.keycloakUrl = keycloakUrl;
    }

    public String getKeycloakClient() {
        return keycloakClient;
    }

    public void setKeycloakClient(String keycloakClient) {
        this.keycloakClient = keycloakClient;
    }

    public String getKeycloakUsername() {
        return keycloakUsername;
    }

    public void setKeycloakUsername(String keycloakUsername) {
        this.keycloakUsername = keycloakUsername;
    }

    public String getKeycloakPassword() {
        return keycloakPassword;
    }

    public void setKeycloakPassword(String keycloakPassword) {
        this.keycloakPassword = keycloakPassword;
    }

    public boolean isKeycloakEnabled() {
        return keycloakEnabled;
    }

    public void setKeycloakEnabled(boolean keycloakEnabled) {
        this.keycloakEnabled = keycloakEnabled;
    }

    public String getKeycloakRealm() {
        return keycloakRealm;
    }

    public void setKeycloakRealm(String keycloakRealm) {
        this.keycloakRealm = keycloakRealm;
    }
}


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

package com.redhat.thermostat.agent.keycloak;

import java.util.concurrent.TimeUnit;

import com.google.gson.annotations.SerializedName;

public class KeycloakAccessToken {
    
    @SerializedName("access_token")
    private String accessToken;

    // In seconds
    @SerializedName("expires_in")
    private long expiresIn;

    // In seconds
    @SerializedName("refresh_expires_in")
    private long refreshExpiresIn;

    @SerializedName("refresh_token")
    private String refreshToken;

    // In nanoseconds
    private transient long acquireTime;

    public KeycloakAccessToken() {
        // default constructor
    }
    
    // for testing
    KeycloakAccessToken(Long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public Long getRefreshExpiresIn() {
        return refreshExpiresIn;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public long getAcquireTime() {
        return acquireTime;
    }

    public void setAcquireTime(long acquireTime) {
        this.acquireTime = acquireTime;
    }
    
    public boolean isKeycloakTokenExpired() {
        return System.nanoTime() > TimeUnit.NANOSECONDS.convert(getExpiresIn(), TimeUnit.SECONDS) + getAcquireTime();
    }
}

/*
 * Copyright 2012 Red Hat, Inc.
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

package com.redhat.thermostat.common.dao;

import java.util.List;

import com.redhat.thermostat.common.model.AgentInformation;
import com.redhat.thermostat.common.storage.Category;
import com.redhat.thermostat.common.storage.Key;

public interface AgentInfoDAO extends Countable {

    static final Key<Long> START_TIME_KEY = new Key<>("start-time", false);
    static final Key<Long> STOP_TIME_KEY = new Key<>("stop-time", false);
    static final Key<Boolean> ALIVE_KEY = new Key<>("alive", false);
    static final Key<String> CONFIG_LISTEN_ADDRESS = new Key<>("config-listen-address", false);

    static final Category CATEGORY = new Category("agent-config",
            Key.AGENT_ID,
            START_TIME_KEY,
            STOP_TIME_KEY,
            ALIVE_KEY,
            CONFIG_LISTEN_ADDRESS);

    List<AgentInformation> getAllAgentInformation();

    List<AgentInformation> getAliveAgents();

    AgentInformation getAgentInformation(HostRef agentRef);

    void addAgentInformation(AgentInformation agentInfo);

    void updateAgentInformation(AgentInformation agentInfo);

    void removeAgentInformation(AgentInformation agentInfo);

}
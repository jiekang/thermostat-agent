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

package com.redhat.thermostat.utils.management.internal;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;

import com.redhat.thermostat.agent.utils.management.MXBeanConnection;
import com.redhat.thermostat.agent.utils.management.MXBeanConnectionException;
import com.redhat.thermostat.agent.utils.management.MXBeanConnectionPool;

@Component
@Service(value = MXBeanConnectionPool.class)
public class MXBeanConnectionPoolImpl implements MXBeanConnectionPool {

    // Keys are PIDs of target JVMs
    private final Map<Integer, MXBeanConnectionPoolEntry> pool;
    private final ManagementAgentHelper helper;
    
    public MXBeanConnectionPoolImpl() {
        this(new ManagementAgentHelper());
    }

    MXBeanConnectionPoolImpl(ManagementAgentHelper helper) {
        this.pool = new HashMap<>();
        this.helper = helper;
    }

    @Override
    public synchronized MXBeanConnection acquire(int pid) throws MXBeanConnectionException {
        MXBeanConnectionPoolEntry data = pool.get(pid);
        if (data == null) {
            data = new MXBeanConnectionPoolEntry(pid);
            try {
                // Attach to JVM and retrieve JMX service URL
                ManagementAgentAttacher attacher = helper.createAttacher(pid);
                attacher.attach();
                String jmxUrl = attacher.getConnectorAddress();
                
                // Connect using JMX service URL
                MXBeanConnector connector = helper.createConnector(jmxUrl);
                MXBeanConnectionImpl connection = connector.connect();
                data.setConnection(connection);
                pool.put(pid, data);
            } catch (IOException e) {
                pool.remove(pid);
                throw new MXBeanConnectionException(e);
            }
        } else {
            data.incrementUsageCount();
        }
        return data.getConnection();
    }

    @Override
    public synchronized void release(int pid, MXBeanConnection toRelease) throws MXBeanConnectionException {
        MXBeanConnectionPoolEntry data = pool.get(pid);
        if (data == null) {
            throw new MXBeanConnectionException("Unknown pid: " + pid);
        }
        MXBeanConnectionImpl connection = data.getConnection();
        if (connection == null) {
            throw new MXBeanConnectionException("No known open connection for pid: " + pid);
        } else if (connection != toRelease) {
            throw new MXBeanConnectionException("Connection mismatch for pid: " + pid);
        }
        
        data.decrementUsageCount();
        int usageCount = data.getUsageCount();
        if (usageCount == 0) {
            try {
                connection.close();
            } catch (IOException e) {
                throw new MXBeanConnectionException(e);
            }
            pool.remove(pid);
        }
    }
    
    static class ManagementAgentHelper {
        ManagementAgentAttacher createAttacher(int pid) {
            return new ManagementAgentAttacher(pid);
        }
        
        MXBeanConnector createConnector(String jmxUrl) throws IOException {
            MXBeanConnector connector = new MXBeanConnector(jmxUrl);
            return connector;
        }
    }
    
}


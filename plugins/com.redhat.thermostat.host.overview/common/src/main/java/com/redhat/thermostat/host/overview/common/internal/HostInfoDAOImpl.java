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

package com.redhat.thermostat.host.overview.common.internal;

import java.util.List;
import java.util.logging.Logger;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;

import com.redhat.thermostat.common.utils.LoggingUtils;
import com.redhat.thermostat.host.overview.common.HostInfoDAO;
import com.redhat.thermostat.host.overview.common.model.HostInfo;
import com.redhat.thermostat.storage.core.AgentId;
import com.redhat.thermostat.storage.core.Category;
import com.redhat.thermostat.storage.core.CategoryAdapter;
import com.redhat.thermostat.storage.core.Key;
import com.redhat.thermostat.storage.core.PreparedStatement;
import com.redhat.thermostat.storage.core.Storage;
import com.redhat.thermostat.storage.dao.AbstractDaoQuery;
import com.redhat.thermostat.storage.dao.AbstractDaoStatement;
import com.redhat.thermostat.storage.dao.BaseCountable;
import com.redhat.thermostat.storage.dao.SimpleDaoQuery;
import com.redhat.thermostat.storage.model.AggregateCount;

@Component
@Service(value = HostInfoDAO.class)
public class HostInfoDAOImpl extends BaseCountable implements HostInfoDAO {
    
    private static final Logger logger = LoggingUtils.getLogger(HostInfoDAOImpl.class);
    static final String QUERY_HOST_INFO = "QUERY "
            + hostInfoCategory.getName() + " WHERE '"
            + Key.AGENT_ID.getName() + "' = ?s LIMIT 1";
    static final String QUERY_ALL_HOSTS = "QUERY " + hostInfoCategory.getName();
    // We can use hostInfoCategory.getName() here since this query
    // only changes the data class. When executed we use the adapted
    // aggregate category.
    static final String AGGREGATE_COUNT_ALL_HOSTS = "QUERY-COUNT " + hostInfoCategory.getName();
    // ADD host-info SET 'agentId' = ?s , \
    //                   'hostname' = ?s , \
    //                   'osName' = ?s , \
    //                   'osKernel' = ?s , \
    //                   'cpuModel' = ?s , \
    //                   'cpuCount' = ?i , \
    //                   'totalMemory' = ?l
    static final String DESC_ADD_HOST_INFO = "ADD " + hostInfoCategory.getName() +
            " SET '" + Key.AGENT_ID.getName() + "' = ?s , " +
                 "'" + hostNameKey.getName() + "' = ?s , " +
                 "'" + osNameKey.getName() + "' = ?s , " +
                 "'" + osKernelKey.getName() + "' = ?s , " +
                 "'" + cpuModelKey.getName() + "' = ?s , " +
                 "'" + cpuCountKey.getName() + "' = ?i , " +
                 "'" + hostMemoryTotalKey.getName() + "' = ?l";

    private final Category<AggregateCount> aggregateCategory;
    
    @Reference
    private Storage storage;

    public HostInfoDAOImpl() {
        this(null);
    }
    
    public HostInfoDAOImpl(Storage storage) {
        this.storage = storage;
        // Adapt category to the aggregate form
        CategoryAdapter<HostInfo, AggregateCount> adapter = new CategoryAdapter<>(hostInfoCategory);
        this.aggregateCategory = adapter.getAdapted(AggregateCount.class);
    }
    
    @Activate
    private void activate() {
        storage.registerCategory(hostInfoCategory);
        storage.registerCategory(aggregateCategory);
    }

    @Override
    public HostInfo getHostInfo(final AgentId agentId) {
        return executeQuery(new AbstractDaoQuery<HostInfo>(storage, hostInfoCategory, QUERY_HOST_INFO) {
            @Override
            public PreparedStatement<HostInfo> customize(PreparedStatement<HostInfo> preparedStatement) {
                preparedStatement.setString(0, agentId.get());
                return preparedStatement;
            }
        }).head();
    }

    @Override
    public void putHostInfo(final HostInfo info) {
        executeStatement(new AbstractDaoStatement<HostInfo>(storage, hostInfoCategory, DESC_ADD_HOST_INFO) {
            @Override
            public PreparedStatement<HostInfo> customize(PreparedStatement<HostInfo> preparedStatement) {
                preparedStatement.setString(0, info.getAgentId());
                preparedStatement.setString(1, info.getHostname());
                preparedStatement.setString(2, info.getOsName());
                preparedStatement.setString(3, info.getOsKernel());
                preparedStatement.setString(4, info.getCpuModel());
                preparedStatement.setInt(5, info.getCpuCount());
                preparedStatement.setLong(6, info.getTotalMemory());
                return preparedStatement;
            }
        });
    }

    @Override
    public List<HostInfo> getAllHostInfos() {
        return executeQuery(new SimpleDaoQuery<>(storage, hostInfoCategory, QUERY_ALL_HOSTS)).asList();
    }

    @Override
    public long getCount() {
        return getCount(storage, aggregateCategory, AGGREGATE_COUNT_ALL_HOSTS);
    }
    
    @Override
    protected Logger getLogger() {
        return logger;
    }

}


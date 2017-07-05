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

package com.redhat.thermostat.jvm.overview.agent.internal.model;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;

import com.redhat.thermostat.agent.http.HttpRequestService;
import com.redhat.thermostat.agent.http.HttpRequestService.RequestFailedException;
import com.redhat.thermostat.common.utils.LoggingUtils;
import com.redhat.thermostat.jvm.overview.agent.internal.model.VmInfoTypeAdapter.VmInfoUpdateTypeAdapter;
import com.redhat.thermostat.jvm.overview.agent.model.VmId;
import com.redhat.thermostat.jvm.overview.agent.model.VmInfo;
import com.redhat.thermostat.storage.core.AgentId;

@Component
@Service(VmInfoDAO.class)
public class VmInfoDAOImpl implements VmInfoDAO {
    
    private final Logger logger = LoggingUtils.getLogger(VmInfoDAOImpl.class);
    
    private static final String GATEWAY_URL = "http://localhost:26000/api/v100"; // TODO configurable
    private static final String GATEWAY_PATH = "/vm-info/systems/*/agents/";
    private static final String GATEWAY_PATH_JVM_SUFFIX = "/jvms/";
    
    private final JsonHelper jsonHelper;
    
    @Reference
    private HttpRequestService httpRequestService;

    public VmInfoDAOImpl() throws Exception {
        this(new JsonHelper(new VmInfoTypeAdapter(), new VmInfoUpdateTypeAdapter()));
    }

    VmInfoDAOImpl(JsonHelper jsonHelper) throws Exception {
        this.jsonHelper = jsonHelper;
    }

    @Override
    public VmInfo getVmInfo(final VmId id) {
        return null; // TODO Remove once VM Id completer is removed
    }

    @Override
    public Set<VmId> getVmIds(AgentId agentId) {
        return Collections.emptySet(); // TODO Remove once VM Id completer is removed
    }

    @Override
    public void putVmInfo(final VmInfo info) {
        // FIXME: Re-enable once /jvms endpoint is being used properly and re-enable ignored
        //        unit-tests.
//        try {
//            // Encode as JSON and send as POST request
//            String json = jsonHelper.toJson(Arrays.asList(info));
//            String url = getAddURL(info.getAgentId());
//            httpRequestService.sendHttpRequest(json, url, HttpRequestService.POST);
//        } catch (IOException | RequestFailedException e) {
//           logger.log(Level.WARNING, "Failed to send JVM information to web gateway", e);
//        }
    }

    @Override
    public void putVmStoppedTime(final String agentId, final String vmId, final long timestamp) {
        try {
            // Encode as JSON and send as PUT request
            VmInfoUpdate update = new VmInfoUpdate(timestamp);
            String json = jsonHelper.toJson(update);
            String url = getUpdateURL(agentId, vmId);
            httpRequestService.sendHttpRequest(json, url, HttpRequestService.PUT);
        } catch (IOException | RequestFailedException e) {
           logger.log(Level.WARNING, "Failed to send JVM information update to web gateway", e);
        }
    }
    
    private String getAddURL(String agentId) {
        StringBuilder builder = buildURL(agentId);
        return builder.toString();
    }

    private StringBuilder buildURL(String agentId) {
        StringBuilder builder = new StringBuilder();
        builder.append(GATEWAY_URL);
        builder.append(GATEWAY_PATH);
        builder.append(agentId);
        return builder;
    }
    
    private String getUpdateURL(String agentId, String vmId) {
        StringBuilder builder = buildURL(agentId);
        builder.append(GATEWAY_PATH_JVM_SUFFIX);
        builder.append(vmId);
        return builder.toString();
    }
    
    protected void bindHttpRequestService(HttpRequestService httpRequestService) {
        this.httpRequestService = httpRequestService;
    }

    protected void unbindHttpRequestService(HttpRequestService httpRequestService) {
        this.httpRequestService = null;
        logger.log(Level.INFO, "Unbound HTTP service. Further attempts to store data will fail until bound again.");
    }
    
    static class VmInfoUpdate {
        
        private final long stoppedTime;
        
        VmInfoUpdate(long stoppedTime) {
           this.stoppedTime = stoppedTime;
        }
        
        long getStoppedTime() {
            return stoppedTime;
        }
    }
    
    // For testing purposes
    static class JsonHelper {
        
        private final VmInfoTypeAdapter typeAdapter;
        private final VmInfoUpdateTypeAdapter updateTypeAdapter;
        
        public JsonHelper(VmInfoTypeAdapter typeAdapter, VmInfoUpdateTypeAdapter updateTypeAdapter) {
            this.typeAdapter = typeAdapter;
            this.updateTypeAdapter = updateTypeAdapter;
        }
        
        String toJson(List<VmInfo> infos) throws IOException {
            return typeAdapter.toJson(infos);
        }
        
        String toJson(VmInfoUpdate update) throws IOException {
            return updateTypeAdapter.toJson(update);
        }
        
    }
}


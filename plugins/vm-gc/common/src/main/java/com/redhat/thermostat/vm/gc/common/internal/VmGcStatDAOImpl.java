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

package com.redhat.thermostat.vm.gc.common.internal;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.redhat.thermostat.common.utils.LoggingUtils;
import com.redhat.thermostat.storage.dao.AbstractDao;
import com.redhat.thermostat.vm.gc.common.VmGcStatDAO;
import com.redhat.thermostat.vm.gc.common.model.VmGcStat;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;

public class VmGcStatDAOImpl extends AbstractDao implements VmGcStatDAO {
    
    private static Logger logger = LoggingUtils.getLogger(VmGcStatDAOImpl.class);
    private final JsonHelper jsonHelper;
    private final HttpHelper httpHelper;
    private final HttpClient httpClient;

    static final String GATEWAY_URL = "http://localhost:30000"; // TODO configurable
    static final String GATEWAY_PATH = "/jvm-gc/0.0.2/";
    static final String CONTENT_TYPE = "application/json";

    VmGcStatDAOImpl() throws Exception {
        this(new HttpClient(), new JsonHelper(new VmGcStatTypeAdapter()), new HttpHelper());
    }

    VmGcStatDAOImpl(HttpClient client, JsonHelper jh, HttpHelper hh) throws Exception {
        this.httpClient = client;
        this.jsonHelper = jh;
        this.httpHelper = hh;

        this.httpHelper.startClient(this.httpClient);
    }

    @Override
    public void putVmGcStat(final VmGcStat stat) {
        try {
            String json = jsonHelper.toJson(Arrays.asList(stat));
            StringContentProvider provider = httpHelper.createContentProvider(json);

            String url = buildUrl();
            Request httpRequest = httpClient.newRequest(url);
            httpRequest.method(HttpMethod.POST);
            httpRequest.content(provider, CONTENT_TYPE);
            sendRequest(httpRequest);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to send VmGcStat information to web gateway", e);
        }
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    private String buildUrl() {
        StringBuilder builder = new StringBuilder();
        builder.append(GATEWAY_URL);
        builder.append(GATEWAY_PATH);
        return builder.toString();
    }

    private void sendRequest(Request httpRequest)
            throws InterruptedException, TimeoutException, ExecutionException, IOException {
        ContentResponse resp = httpRequest.send();
        int status = resp.getStatus();
        if (status != HttpStatus.OK_200) {
            throw new IOException("Gateway returned HTTP status " + String.valueOf(status) + " - " + resp.getReason());
        }
    }

    // For Testing purposes
    static class JsonHelper {

        private final VmGcStatTypeAdapter adapter;

        public JsonHelper(VmGcStatTypeAdapter adapter) {
            this.adapter = adapter;
        }

        public String toJson(List<VmGcStat> list) throws IOException {
            return adapter.toJson(list);
        }

    }

    // For Testing purposes
    static class HttpHelper {

        void startClient(HttpClient httpClient) throws Exception {
            httpClient.start();
        }

        StringContentProvider createContentProvider(String content) {
            return new StringContentProvider(content);
        }
    }
}


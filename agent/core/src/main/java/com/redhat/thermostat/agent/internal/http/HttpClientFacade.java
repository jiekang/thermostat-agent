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

package com.redhat.thermostat.agent.internal.http;

import java.net.URI;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLContext;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import com.redhat.thermostat.common.ssl.SSLContextFactory;
import com.redhat.thermostat.common.ssl.SslInitException;
import com.redhat.thermostat.common.utils.LoggingUtils;
import com.redhat.thermostat.shared.config.SSLConfiguration;

class HttpClientFacade {

    private static final Logger logger = LoggingUtils.getLogger(HttpClientFacade.class);
    private static final long PER_REQUEST_TIMEOUT_SEC = 5;
    private final HttpClient httpsClient;
    
    HttpClientFacade(SSLConfiguration sslConfig) {
        httpsClient = createHttpsClient(sslConfig);
    }

    private static HttpClient createHttpsClient(SSLConfiguration config) {
        try {
            SSLContext context = SSLContextFactory.getClientContext(config);
            SslContextFactory sslFactory = new SslContextFactory();
            sslFactory.setSslContext(context);
            // Don't send SSLv2 Client Hello. Some servers will refuse to
            // accept it. So does the web-gateway with our self-signed cert.
            sslFactory.setIncludeProtocols("TLSv1", "TLSv1.2");
            if (config.disableHostnameVerification()) {
                logger.fine("HTTPS endpoint verification disabled.");
            } else {
                sslFactory.setEndpointIdentificationAlgorithm("HTTPS");
            }
            HttpClient client = new HttpClient(sslFactory);
            return client;
        } catch (SslInitException e) {
            logger.log(Level.INFO, "Failed to initialize SSL context.", e);
            logger.severe("Failed to initialize SSL context. Reason: " + e.getMessage());
            throw new RuntimeException(e);
        } 
    }
    
    void start() throws Exception {
        httpsClient.start();
    }
    
    Request newRequest(URI uri) {
        return httpsClient.newRequest(uri).timeout(PER_REQUEST_TIMEOUT_SEC, TimeUnit.SECONDS);
    }
    
    Request newRequest(String url) {
        return httpsClient.newRequest(url).timeout(PER_REQUEST_TIMEOUT_SEC, TimeUnit.SECONDS);
    }
}

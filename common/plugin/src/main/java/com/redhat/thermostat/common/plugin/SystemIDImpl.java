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

package com.redhat.thermostat.common.plugin;

import com.redhat.thermostat.shared.config.OS;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;

@Component
@Service(value = SystemID.class)
public class SystemIDImpl implements SystemID {

    private Logger logger = Logger.getLogger(SystemIDImpl.class.getName());

    private String cachedSystemID;

    String getHostnameFromEnvironment() {
        // Don't use PortableHostFactory.getInstance().getHostName() as we want to avoid pulling in the portability library
        if (OS.IS_WINDOWS) {
            return System.getenv("COMPUTERNAME");
        }
        else {
            final String env = System.getenv("HOSTNAME");
            return env != null ? env : "localhost";
        }
    }

    private String hash(final String str) {
        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA-1");
            digest.update(str.getBytes("utf8"));
            final byte[] digestBytes = digest.digest();
            return javax.xml.bind.DatatypeConverter.printHexBinary(digestBytes);
        } catch (NoSuchAlgorithmException e) {
            logger.severe("SHA-1 algorithm is not implemented");
            return str;
        }
        catch (UnsupportedEncodingException e) {
            logger.severe("utf-8 encoding is not implemented");
            return str;
        }
    }

    @Activate
    public void activate() {
        cachedSystemID = hash(getHostnameFromEnvironment());
    }


    public String getSystemID() {
        return cachedSystemID;
    }
}

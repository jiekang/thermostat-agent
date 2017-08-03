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

package com.redhat.thermostat.commands.agent.internal.receiver;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;

import com.redhat.thermostat.commands.agent.receiver.RequestReceiver;
import com.redhat.thermostat.commands.model.AgentRequest;
import com.redhat.thermostat.commands.model.WebSocketResponse;
import com.redhat.thermostat.commands.model.WebSocketResponse.ResponseType;
import com.redhat.thermostat.common.utils.LoggingUtils;

@Component
@Service(value = RequestReceiver.class)
@Property(name = "servicename", value = PingReceiver.ACTION_NAME)
public class PingReceiver implements RequestReceiver {

    public static final String ACTION_NAME = "ping";
    private static Logger logger = LoggingUtils.getLogger(PingReceiver.class);
    
    @Activate
    public void activate() {
        logger.log(Level.FINE, "PingReceiver activated.");
    }
    
    @Override
    public WebSocketResponse receive(AgentRequest request) {
        // Sanity check. We should never get requests outside our action domain.
        if (!ACTION_NAME.equals(request.getAction())) {
            logger.severe("Received action '" + request.getAction() + "' for receiver '" + ACTION_NAME + "'");
            return new WebSocketResponse(request.getSequenceId(), ResponseType.ERROR);
        }
        return new WebSocketResponse(request.getSequenceId(), ResponseType.OK);
    }

}


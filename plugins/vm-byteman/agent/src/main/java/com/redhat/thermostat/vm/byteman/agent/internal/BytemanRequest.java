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

package com.redhat.thermostat.vm.byteman.agent.internal;

public class BytemanRequest {

    public static enum RequestAction {
        LOAD_RULES(0),
        UNLOAD_RULES(1),
        ;

        private int actionId;

        private RequestAction(int id) {
            this.actionId = id;
        }

        public static RequestAction fromIntString(String id) {
            int intId;
            try {
                intId = Integer.parseInt(id);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Unknown action: " + id);
            }
            switch(intId) {
            case(0): return LOAD_RULES;
            case(1): return UNLOAD_RULES;
            default:
                throw new IllegalArgumentException("Unknown action: " + id);
            }
        }

        int getActionId() {
            return actionId;
        }

    }

    public static final String VM_PID_PARAM_NAME = "vm-pid";
    public static final String ACTION_PARAM_NAME = "byteman-action";
    public static final String LISTEN_PORT_PARAM_NAME = "listen-port";
    public static final String RULE_PARAM_NAME = "byteman-rule";
    public static final int NOT_ATTACHED_PORT = -1;

}

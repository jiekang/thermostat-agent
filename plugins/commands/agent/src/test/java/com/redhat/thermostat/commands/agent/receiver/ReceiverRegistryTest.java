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

package com.redhat.thermostat.commands.agent.receiver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;

import java.util.Collection;

import org.junit.Test;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import com.redhat.thermostat.testutils.StubBundleContext;

public class ReceiverRegistryTest {
    
    private static final String FILTER_FORMAT = "(&(objectclass=*)(servicename=%s))";

    @Test
    public void testRegister() throws InvalidSyntaxException {
        StubBundleContext context = new StubBundleContext();
        ReceiverRegistry reg = new ReceiverRegistry(context);
        RequestReceiver receiver = mock(RequestReceiver.class);
        
        reg.registerReceiver(receiver);
        
        context.isServiceRegistered(RequestReceiver.class.getName(), receiver.getClass());
        Collection<?> services = context.getServiceReferences(RequestReceiver.class, String.format(FILTER_FORMAT, receiver.getClass().getName()));
        assertEquals(1, services.size());
        @SuppressWarnings("unchecked")
        ServiceReference<RequestReceiver> sr = (ServiceReference<RequestReceiver>)services.iterator().next();
        String serviceName = (String)sr.getProperty("servicename");
        assertEquals(receiver.getClass().getName(), serviceName);
    }
    
    @Test
    public void testGetReceiver() {
        StubBundleContext context = new StubBundleContext();
        ReceiverRegistry reg = new ReceiverRegistry(context);
        assertNull(reg.getReceiver(String.class.getName()));
        
        RequestReceiver receiver = mock(RequestReceiver.class);
        reg.registerReceiver(receiver);
        RequestReceiver actual = reg.getReceiver(receiver.getClass().getName());
        assertSame(receiver, actual);
    }
}

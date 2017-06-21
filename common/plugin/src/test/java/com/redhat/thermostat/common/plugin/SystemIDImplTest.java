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

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class SystemIDImplTest {

    @Test
    public void testUnactivatedReturnsNull() {
        SystemID si = new SystemIDImpl();
        assertNull(si.getSystemID());
    }

    @Test
    public void testReturnsSomething() {
        SystemIDImpl si = new SystemIDImpl();
        si.activate();
        final String id = si.getSystemID();
        assertNotNull(id);
        assertFalse(id.isEmpty());
    }

    @Test
    public void testSameInstanceSameResult() {
        final SystemIDImpl svcimpl = new SystemIDImpl();
        svcimpl.activate();
        assertEquals(svcimpl.getSystemID(), svcimpl.getSystemID());
    }

    @Test
    public void testDifferentInstancesSameResult() {
        SystemIDImpl svc1 = new SystemIDImpl();
        svc1.activate();
        SystemIDImpl svc2 = new SystemIDImpl();
        svc2.activate();
        assertEquals(svc1.getSystemID(), svc2.getSystemID());
    }

    static class TesSI extends SystemIDImpl {
        String hn;

        @Override
        String getHostnameFromEnvironment() {
            return hn != null ? hn : super.getHostnameFromEnvironment();
        }
    }

    @Test
    public void testDifferentHostDifferentResult() {
        TesSI si = new TesSI();
        si.activate();
        final String id1 = si.getSystemID();
        si.hn = "foo1";
        si.activate();
        final String id2 = si.getSystemID();
        assertFalse(id1.equals(id2));
    }
}

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

package com.redhat.thermostat.jvm.overview.agent;

import com.redhat.thermostat.jvm.overview.agent.model.VmId;
import com.redhat.thermostat.lang.schema.models.Id;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class VmIdTest {

    protected Id fooId1;
    protected Id fooId2;
    protected Id barId;

    @Before
    public void setup() {
        fooId1 = createId("foo");
        fooId2 = createId("foo");
        barId = createId("bar");
    }

    @Test
    public void testEquals() {
        assertTrue(fooId1.equals(fooId1));
        assertTrue(fooId1.equals(fooId2));
    }

    @Test
    public void testNotEquals() {
        assertFalse(fooId1.equals(null));
        assertFalse(fooId1.equals(barId));
    }

    @Test
    public void testEqualsHaveIdenticalHashcode() {
        assertEquals(fooId1, fooId2);

        assertEquals(fooId1.hashCode(), fooId1.hashCode());
        assertEquals(fooId1.hashCode(), fooId2.hashCode());
    }

    protected Id createId(String id) { return new VmId(id); }
}

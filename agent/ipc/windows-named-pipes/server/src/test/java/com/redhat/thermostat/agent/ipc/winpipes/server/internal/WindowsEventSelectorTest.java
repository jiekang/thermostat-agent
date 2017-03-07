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

package com.redhat.thermostat.agent.ipc.winpipes.server.internal;

import com.redhat.thermostat.agent.ipc.winpipes.common.internal.WinPipesNativeHelper;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.LinkedHashSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class WindowsEventSelectorTest {

    private static final int MAX_INSTANCES = 5;
    private WinPipesNativeHelper nativeMock;

    @Before
    public void setup() {
        nativeMock = mock(WinPipesNativeHelper.class);
        when(nativeMock.getLastError()).thenReturn(WinPipesNativeHelper.ERROR_SUCCESS);

        /*
         * The mock will return an index based on the size of the handle array.
         * For the tests to work, assume the array is ordered in the order of insertion
         * To do this, create the WindowsEventSelector with a LinkedHashSet instead of a simple HashSet
         */
        when(nativeMock.waitForMultipleObjects(eq(0), any(long[].class), anyBoolean(), anyInt())).thenReturn(-1);
        when(nativeMock.waitForMultipleObjects(eq(1), any(long[].class), anyBoolean(), anyInt())).thenReturn((int) WinPipesNativeHelper.WAIT_OBJECT_0 + 0);
        when(nativeMock.waitForMultipleObjects(eq(2), any(long[].class), anyBoolean(), anyInt())).thenReturn((int) WinPipesNativeHelper.WAIT_OBJECT_0 + 1);
    }

    private WindowsEventSelector create() {
        return new WindowsEventSelector(MAX_INSTANCES, new LinkedHashSet<WindowsEventSelector.EventHandler>(), nativeMock);
    }

    @Test
    public void testCanConstruct() {
        create();
    }

    @Test
    public void testWait() {
        final WindowsEventSelector wes = create();
        try {
            wes.waitForEvent();
            // no events added, so should throw an error
            fail("expected IOException");
        } catch (IOException ignored) {
        }
        verify(nativeMock).getLastError();
        verify(nativeMock).waitForMultipleObjects(anyInt(), any(long[].class), anyBoolean(), anyInt());
    }

    @Test
    public void testAdd() throws IOException {
        final WindowsEventSelector wes = create();
        final WindowsEventSelector.EventHandler eh1 = mock(WindowsEventSelector.EventHandler.class);
        wes.add(eh1);
        final WindowsEventSelector.EventHandler en = wes.waitForEvent();
        assertEquals(eh1, en);
        verify(nativeMock, never()).getLastError();
        verify(nativeMock).waitForMultipleObjects(anyInt(), any(long[].class), anyBoolean(), anyInt());
    }

    @Test
    public void testRemove() throws IOException {
        final WindowsEventSelector wes = create();
        final WindowsEventSelector.EventHandler eh1 = mock(WindowsEventSelector.EventHandler.class);
        final WindowsEventSelector.EventHandler eh2 = mock(WindowsEventSelector.EventHandler.class);

        wes.add(eh1);
        final WindowsEventSelector.EventHandler en1 = wes.waitForEvent();
        assertEquals(eh1, en1);

        wes.add(eh2);
        final WindowsEventSelector.EventHandler en2 = wes.waitForEvent();
        assertEquals(eh2, en2);

        wes.remove(eh1);
        final WindowsEventSelector.EventHandler en3 = wes.waitForEvent();
        assertEquals(eh2, en3);

        verify(nativeMock, never()).getLastError();
        verify(nativeMock, times(3)).waitForMultipleObjects(anyInt(), any(long[].class), anyBoolean(), anyInt());
    }
}

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

package com.redhat.thermostat.vm.memory.agent.internal;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.redhat.thermostat.jvm.overview.agent.VmUpdate;
import com.redhat.thermostat.jvm.overview.agent.VmUpdateException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.redhat.thermostat.vm.memory.agent.internal.models.VmMemoryStatDAO;
import com.redhat.thermostat.vm.memory.agent.internal.models.VmTlabStatDAO;
import com.redhat.thermostat.vm.memory.agent.model.VmMemoryStat;
import com.redhat.thermostat.vm.memory.agent.model.VmMemoryStat.Generation;
import com.redhat.thermostat.vm.memory.agent.model.VmMemoryStat.Space;
import com.redhat.thermostat.vm.memory.agent.model.VmTlabStat;

public class VmMemoryVmListenerTest {
    private static final String[] GEN_NAMES = new String[] { "Gen1", "Gen2" };
    private static final Long[] GEN_CAPS = new Long[] { 500L, 1000L };
    private static final Long[] GEN_MAX_CAPS = new Long[] { 5000L, 10000L };
    private static final String[] GEN_GCS = new String[] { "GC1", "GC2" };
    private static final Long[] GEN_SPACES = new Long[] { 2L, 1L };
    private static final String[][] SPACE_NAME = new String[][] { 
        { "Space1", "Space2" },
        { "Space3" }
    };
    private static final Long[][] SPACE_CAPS = new Long[][] {
        { 225L, 275L },
        { 1000L }
    };
    private static final Long[][] SPACE_MAX_CAPS = new Long[][] {
        { 2250L, 2750L },
        { 10000L }
    };
    private static final Long[][] SPACE_USED = new Long[][] {
        { 125L, 175L },
        { 900L }
    };

    private static final long METASPACE_MAX_CAPACITY = 10;
    private static final long METASPACE_MIN_CAPACITY = 0;
    private static final long METASPACE_CAPACITY = 10;
    private static final long METASPACE_USED = 5;
    
    private VmMemoryVmListener vmListener;
    private VmMemoryDataExtractor extractor;
    private VmMemoryStatDAO vmMemoryStatDAO;
    private VmTlabStatDAO vmTlabStatDAO;
    
    @Before
    public void setup() throws VmUpdateException {
        final int numGens = 2;
        vmMemoryStatDAO = mock(VmMemoryStatDAO.class);
        vmTlabStatDAO = mock(VmTlabStatDAO.class);
        vmListener = new VmMemoryVmListener("foo-agent", vmMemoryStatDAO, vmTlabStatDAO, "vmId");
        extractor = mock(VmMemoryDataExtractor.class);

        mockTotalGenerations(numGens);

        for (int i = 0; i < numGens; i++) {
            mockGenerationName(i);
            mockGenerationCapacity(i);
            mockGenerationMaxCapacity(i);
            mockGenerationGC(i);
            mockTotalSpaces(i);
            int numSpaces = GEN_SPACES[i].intValue();
            for (int j = 0; j < numSpaces; j++) {
                mockSpaceName(i, j);
                mockSpaceCapacity(i, j);
                mockSpaceMaxCapacity(i, j);
                mockSpaceUsed(i, j);
            }
        }

        mockMetaspace();
        mockTlab();
    }

    private void mockTotalGenerations(long gens) throws VmUpdateException {
        when(extractor.getTotalGcGenerations()).thenReturn(gens);
    }

    private void mockGenerationName(int gen) throws VmUpdateException {
        when(extractor.getGenerationName(gen)).thenReturn(GEN_NAMES[gen]);
    }
    
    private void mockGenerationCapacity(int gen) throws VmUpdateException {
        when(extractor.getGenerationCapacity(gen)).thenReturn(GEN_CAPS[gen]);
    }

    private void mockGenerationMaxCapacity(int gen) throws VmUpdateException {
        when(extractor.getGenerationMaxCapacity(gen)).thenReturn(GEN_MAX_CAPS[gen]);
    }
    
    private void mockGenerationGC(int gen) throws VmUpdateException {
        when(extractor.getGenerationCollector(gen)).thenReturn(GEN_GCS[gen]);
    }
    
    private void mockTotalSpaces(int gen) throws VmUpdateException {
        when(extractor.getTotalSpaces(gen)).thenReturn(GEN_SPACES[gen]);
    }
    
    private void mockSpaceName(int gen, int space) throws VmUpdateException {
        when(extractor.getSpaceName(gen, space)).thenReturn(SPACE_NAME[gen][space]);
    }
    
    private void mockSpaceCapacity(int gen, int space) throws VmUpdateException {
        when(extractor.getSpaceCapacity(gen, space)).thenReturn(SPACE_CAPS[gen][space]);
    }
    
    private void mockSpaceMaxCapacity(int gen, int space) throws VmUpdateException {
        when(extractor.getSpaceMaxCapacity(gen, space)).thenReturn(SPACE_MAX_CAPS[gen][space]);
    }
    
    private void mockSpaceUsed(int gen, int space) throws VmUpdateException {
        when(extractor.getSpaceUsed(gen, space)).thenReturn(SPACE_USED[gen][space]);
    }

    private void mockMetaspace() {
        when(extractor.getMetaspaceMaxCapacity(anyLong())).thenReturn(METASPACE_MAX_CAPACITY);
        when(extractor.getMetaspaceMinCapacity(anyLong())).thenReturn(METASPACE_MIN_CAPACITY);
        when(extractor.getMetaspaceCapacity(anyLong())).thenReturn(METASPACE_CAPACITY);
        when(extractor.getMetaspaceUsed(anyLong())).thenReturn(METASPACE_USED);
    }

    private void mockTlab() {
//        when(extractor.getTlabAllocThreads(TLAB_ALLOC_THREADS));
    }

    @Test
    public void testMonitorsUpdated() throws VmUpdateException {
        VmUpdate update = mock(VmUpdate.class);
        vmListener.countersUpdated(update);

        verify(vmMemoryStatDAO).putVmMemoryStat(isA(VmMemoryStat.class));
        verify(vmTlabStatDAO).putStat(isA(VmTlabStat.class));
    }

    @Test
    public void testRecordMemoryStat() {
        vmListener.recordMemoryStat(extractor);
        ArgumentCaptor<VmMemoryStat> captor = ArgumentCaptor.forClass(VmMemoryStat.class);
        verify(vmMemoryStatDAO).putVmMemoryStat(captor.capture());
        VmMemoryStat memoryStat = captor.getValue();
        
        Generation[] gens = memoryStat.getGenerations();
        assertEquals(2, gens.length);
        for (int i = 0; i < gens.length; i++) {
            Generation gen = gens[i];
            assertEquals(GEN_NAMES[i], gen.getName());
            assertEquals(GEN_CAPS[i], (Long) gen.getCapacity());
            assertEquals(GEN_MAX_CAPS[i], (Long) gen.getMaxCapacity());
            assertEquals(GEN_GCS[i], gen.getCollector());
            assertEquals(GEN_SPACES[i], Long.valueOf(gen.getSpaces().length));
            Space[] spaces = gen.getSpaces();
            for (int j = 0; j < spaces.length; j++) {
                Space space = spaces[j];
                assertEquals(SPACE_NAME[i][j], space.getName());
                assertEquals(SPACE_CAPS[i][j], (Long) space.getCapacity());
                assertEquals(SPACE_MAX_CAPS[i][j], (Long) space.getMaxCapacity());
                assertEquals(SPACE_USED[i][j], (Long) space.getUsed());
            }
        }

        assertEquals(METASPACE_MAX_CAPACITY, memoryStat.getMetaspaceMaxCapacity());
        assertEquals(METASPACE_MIN_CAPACITY, memoryStat.getMetaspaceMinCapacity());
        assertEquals(METASPACE_CAPACITY, memoryStat.getMetaspaceCapacity());
        assertEquals(METASPACE_USED, memoryStat.getMetaspaceUsed());
    }

    @Test
    public void testRecordingMemoryInPresenseOfExtrationErrors() throws VmUpdateException {
        when(extractor.getTotalGcGenerations()).thenThrow(new VmUpdateException());
        vmListener.recordMemoryStat(extractor);

        verifyNoMoreInteractions(vmMemoryStatDAO);
    }
    
    @Test
    public void testRecordMemoryStatNoTotal() throws VmUpdateException {
        when(extractor.getTotalGcGenerations()).thenReturn(null);
        vmListener.recordMemoryStat(extractor);
        verify(vmMemoryStatDAO, never()).putVmMemoryStat(any(VmMemoryStat.class));
    }

    @Test
    public void testRecordMemoryStatNoName() throws VmUpdateException {
        when(extractor.getGenerationName(0)).thenReturn(null);
        vmListener.recordMemoryStat(extractor);
        ArgumentCaptor<VmMemoryStat> captor = ArgumentCaptor.forClass(VmMemoryStat.class);
        verify(vmMemoryStatDAO).putVmMemoryStat(captor.capture());
        VmMemoryStat memoryStat = captor.getValue();
        
        Generation[] gens = memoryStat.getGenerations();
        assertEquals(1, gens.length);
        Generation gen = gens[0];
        assertEquals(GEN_NAMES[1], gen.getName());
        assertEquals(GEN_CAPS[1], (Long) gen.getCapacity());
        assertEquals(GEN_MAX_CAPS[1], (Long) gen.getMaxCapacity());
        assertEquals(GEN_GCS[1], gen.getCollector());
        assertEquals(GEN_SPACES[1], Long.valueOf(gen.getSpaces().length));
        Space[] spaces = gen.getSpaces();
        for (int j = 1; j < spaces.length; j++) {
            Space space = spaces[j];
            assertEquals(SPACE_NAME[1][j], space.getName());
            assertEquals(SPACE_CAPS[1][j], (Long) space.getCapacity());
            assertEquals(SPACE_MAX_CAPS[1][j], (Long) space.getMaxCapacity());
            assertEquals(SPACE_USED[1][j], (Long) space.getUsed());
        }
    }
    
    @Test
    public void testRecordMemoryStatNoCapacity() throws VmUpdateException {
        when(extractor.getGenerationCapacity(0)).thenReturn(null);
        vmListener.recordMemoryStat(extractor);
        ArgumentCaptor<VmMemoryStat> captor = ArgumentCaptor.forClass(VmMemoryStat.class);
        verify(vmMemoryStatDAO).putVmMemoryStat(captor.capture());
        VmMemoryStat memoryStat = captor.getValue();
        
        Generation[] gens = memoryStat.getGenerations();
        assertEquals(1, gens.length);
        Generation gen = gens[0];
        assertEquals(GEN_NAMES[1], gen.getName());
        assertEquals(GEN_CAPS[1], (Long) gen.getCapacity());
        assertEquals(GEN_MAX_CAPS[1], (Long) gen.getMaxCapacity());
        assertEquals(GEN_GCS[1], gen.getCollector());
        assertEquals(GEN_SPACES[1], Long.valueOf(gen.getSpaces().length));
        Space[] spaces = gen.getSpaces();
        for (int j = 1; j < spaces.length; j++) {
            Space space = spaces[j];
            assertEquals(SPACE_NAME[1][j], space.getName());
            assertEquals(SPACE_CAPS[1][j], (Long) space.getCapacity());
            assertEquals(SPACE_MAX_CAPS[1][j], (Long) space.getMaxCapacity());
            assertEquals(SPACE_USED[1][j], (Long) space.getUsed());
        }
    }
    
    @Test
    public void testRecordMemoryStatNoMaxCapacity() throws VmUpdateException {
        when(extractor.getGenerationMaxCapacity(0)).thenReturn(null);
        vmListener.recordMemoryStat(extractor);
        ArgumentCaptor<VmMemoryStat> captor = ArgumentCaptor.forClass(VmMemoryStat.class);
        verify(vmMemoryStatDAO).putVmMemoryStat(captor.capture());
        VmMemoryStat memoryStat = captor.getValue();
        
        Generation[] gens = memoryStat.getGenerations();
        assertEquals(1, gens.length);
        Generation gen = gens[0];
        assertEquals(GEN_NAMES[1], gen.getName());
        assertEquals(GEN_CAPS[1], (Long) gen.getCapacity());
        assertEquals(GEN_MAX_CAPS[1], (Long) gen.getMaxCapacity());
        assertEquals(GEN_GCS[1], gen.getCollector());
        assertEquals(GEN_SPACES[1], Long.valueOf(gen.getSpaces().length));
        Space[] spaces = gen.getSpaces();
        for (int j = 1; j < spaces.length; j++) {
            Space space = spaces[j];
            assertEquals(SPACE_NAME[1][j], space.getName());
            assertEquals(SPACE_CAPS[1][j], (Long) space.getCapacity());
            assertEquals(SPACE_MAX_CAPS[1][j], (Long) space.getMaxCapacity());
            assertEquals(SPACE_USED[1][j], (Long) space.getUsed());
        }
    }
    
    @Test
    public void testRecordMemoryStatNoCollector() throws VmUpdateException {
        when(extractor.getGenerationCollector(0)).thenReturn(null);
        vmListener.recordMemoryStat(extractor);
        ArgumentCaptor<VmMemoryStat> captor = ArgumentCaptor.forClass(VmMemoryStat.class);
        verify(vmMemoryStatDAO).putVmMemoryStat(captor.capture());
        VmMemoryStat memoryStat = captor.getValue();
        
        Generation[] gens = memoryStat.getGenerations();
        assertEquals(1, gens.length);
        Generation gen = gens[0];
        assertEquals(GEN_NAMES[1], gen.getName());
        assertEquals(GEN_CAPS[1], (Long) gen.getCapacity());
        assertEquals(GEN_MAX_CAPS[1], (Long) gen.getMaxCapacity());
        assertEquals(GEN_GCS[1], gen.getCollector());
        assertEquals(GEN_SPACES[1], Long.valueOf(gen.getSpaces().length));
        Space[] spaces = gen.getSpaces();
        for (int j = 1; j < spaces.length; j++) {
            Space space = spaces[j];
            assertEquals(SPACE_NAME[1][j], space.getName());
            assertEquals(SPACE_CAPS[1][j], (Long) space.getCapacity());
            assertEquals(SPACE_MAX_CAPS[1][j], (Long) space.getMaxCapacity());
            assertEquals(SPACE_USED[1][j], (Long) space.getUsed());
        }
    }
    
    @Test
    public void testRecordMemoryStatNoTotalSpaces() throws VmUpdateException {
        when(extractor.getTotalSpaces(0)).thenReturn(null);
        vmListener.recordMemoryStat(extractor);
        ArgumentCaptor<VmMemoryStat> captor = ArgumentCaptor.forClass(VmMemoryStat.class);
        verify(vmMemoryStatDAO).putVmMemoryStat(captor.capture());
        VmMemoryStat memoryStat = captor.getValue();
        
        Generation[] gens = memoryStat.getGenerations();
        assertEquals(1, gens.length);
        Generation gen = gens[0];
        assertEquals(GEN_NAMES[1], gen.getName());
        assertEquals(GEN_CAPS[1], (Long) gen.getCapacity());
        assertEquals(GEN_MAX_CAPS[1], (Long) gen.getMaxCapacity());
        assertEquals(GEN_GCS[1], gen.getCollector());
        assertEquals(GEN_SPACES[1], Long.valueOf(gen.getSpaces().length));
        Space[] spaces = gen.getSpaces();
        for (int j = 1; j < spaces.length; j++) {
            Space space = spaces[j];
            assertEquals(SPACE_NAME[1][j], space.getName());
            assertEquals(SPACE_CAPS[1][j], (Long) space.getCapacity());
            assertEquals(SPACE_MAX_CAPS[1][j], (Long) space.getMaxCapacity());
            assertEquals(SPACE_USED[1][j], (Long) space.getUsed());
        }
    }
    
    @Test
    public void testRecordMemoryStatNoSpaceName() throws VmUpdateException {
        when(extractor.getSpaceName(0, 1)).thenReturn(null);
        vmListener.recordMemoryStat(extractor);
        ArgumentCaptor<VmMemoryStat> captor = ArgumentCaptor.forClass(VmMemoryStat.class);
        verify(vmMemoryStatDAO).putVmMemoryStat(captor.capture());
        VmMemoryStat memoryStat = captor.getValue();
        
        Generation[] gens = memoryStat.getGenerations();
        assertEquals(2, gens.length);
        for (int i = 0; i < gens.length; i++) {
            Generation gen = gens[i];
            assertEquals(GEN_NAMES[i], gen.getName());
            assertEquals(GEN_CAPS[i], (Long) gen.getCapacity());
            assertEquals(GEN_MAX_CAPS[i], (Long) gen.getMaxCapacity());
            assertEquals(GEN_GCS[i], gen.getCollector());
            if (i == 0) {
                // Bad space in first generation
                assertEquals(Long.valueOf(GEN_SPACES[i] - 1), Long.valueOf(gen.getSpaces().length));
            }
            else {
                assertEquals(GEN_SPACES[i], Long.valueOf(gen.getSpaces().length));
            }
            Space[] spaces = gen.getSpaces();
            for (int j = 0; j < spaces.length; j++) {
                Space space = spaces[j];
                assertEquals(SPACE_NAME[i][j], space.getName());
                assertEquals(SPACE_CAPS[i][j], (Long) space.getCapacity());
                assertEquals(SPACE_MAX_CAPS[i][j], (Long) space.getMaxCapacity());
                assertEquals(SPACE_USED[i][j], (Long) space.getUsed());
            }
        }
    }
    
    @Test
    public void testRecordMemoryStatNoSpaceCapacity() throws VmUpdateException {
        when(extractor.getSpaceCapacity(0, 1)).thenReturn(null);
        vmListener.recordMemoryStat(extractor);
        ArgumentCaptor<VmMemoryStat> captor = ArgumentCaptor.forClass(VmMemoryStat.class);
        verify(vmMemoryStatDAO).putVmMemoryStat(captor.capture());
        VmMemoryStat memoryStat = captor.getValue();
        
        Generation[] gens = memoryStat.getGenerations();
        assertEquals(2, gens.length);
        for (int i = 0; i < gens.length; i++) {
            Generation gen = gens[i];
            assertEquals(GEN_NAMES[i], gen.getName());
            assertEquals(GEN_CAPS[i], (Long) gen.getCapacity());
            assertEquals(GEN_MAX_CAPS[i], (Long) gen.getMaxCapacity());
            assertEquals(GEN_GCS[i], gen.getCollector());
            if (i == 0) {
                // Bad space in first generation
                assertEquals(Long.valueOf(GEN_SPACES[i] - 1), Long.valueOf(gen.getSpaces().length));
            }
            else {
                assertEquals(GEN_SPACES[i], Long.valueOf(gen.getSpaces().length));
            }
            Space[] spaces = gen.getSpaces();
            for (int j = 0; j < spaces.length; j++) {
                Space space = spaces[j];
                assertEquals(SPACE_NAME[i][j], space.getName());
                assertEquals(SPACE_CAPS[i][j], (Long) space.getCapacity());
                assertEquals(SPACE_MAX_CAPS[i][j], (Long) space.getMaxCapacity());
                assertEquals(SPACE_USED[i][j], (Long) space.getUsed());
            }
        }
    }
    
    @Test
    public void testRecordMemoryStatNoSpaceMaxCapacity() throws VmUpdateException {
        when(extractor.getSpaceMaxCapacity(0, 1)).thenReturn(null);
        vmListener.recordMemoryStat(extractor);
        ArgumentCaptor<VmMemoryStat> captor = ArgumentCaptor.forClass(VmMemoryStat.class);
        verify(vmMemoryStatDAO).putVmMemoryStat(captor.capture());
        VmMemoryStat memoryStat = captor.getValue();
        
        Generation[] gens = memoryStat.getGenerations();
        assertEquals(2, gens.length);
        for (int i = 0; i < gens.length; i++) {
            Generation gen = gens[i];
            assertEquals(GEN_NAMES[i], gen.getName());
            assertEquals(GEN_CAPS[i], (Long) gen.getCapacity());
            assertEquals(GEN_MAX_CAPS[i], (Long) gen.getMaxCapacity());
            assertEquals(GEN_GCS[i], gen.getCollector());
            if (i == 0) {
                // Bad space in first generation
                assertEquals(Long.valueOf(GEN_SPACES[i] - 1), Long.valueOf(gen.getSpaces().length));
            }
            else {
                assertEquals(GEN_SPACES[i], Long.valueOf(gen.getSpaces().length));
            }
            Space[] spaces = gen.getSpaces();
            for (int j = 0; j < spaces.length; j++) {
                Space space = spaces[j];
                assertEquals(SPACE_NAME[i][j], space.getName());
                assertEquals(SPACE_CAPS[i][j], (Long) space.getCapacity());
                assertEquals(SPACE_MAX_CAPS[i][j], (Long) space.getMaxCapacity());
                assertEquals(SPACE_USED[i][j], (Long) space.getUsed());
            }
        }
    }
    
    @Test
    public void testRecordMemoryStatNoSpaceUsed() throws VmUpdateException {
        when(extractor.getSpaceUsed(0, 1)).thenReturn(null);
        vmListener.recordMemoryStat(extractor);
        ArgumentCaptor<VmMemoryStat> captor = ArgumentCaptor.forClass(VmMemoryStat.class);
        verify(vmMemoryStatDAO).putVmMemoryStat(captor.capture());
        VmMemoryStat memoryStat = captor.getValue();
        
        Generation[] gens = memoryStat.getGenerations();
        assertEquals(2, gens.length);
        for (int i = 0; i < gens.length; i++) {
            Generation gen = gens[i];
            assertEquals(GEN_NAMES[i], gen.getName());
            assertEquals(GEN_CAPS[i], (Long) gen.getCapacity());
            assertEquals(GEN_MAX_CAPS[i], (Long) gen.getMaxCapacity());
            assertEquals(GEN_GCS[i], gen.getCollector());
            if (i == 0) {
                // Bad space in first generation
                assertEquals(Long.valueOf(GEN_SPACES[i] - 1), Long.valueOf(gen.getSpaces().length));
            }
            else {
                assertEquals(GEN_SPACES[i], Long.valueOf(gen.getSpaces().length));
            }
            Space[] spaces = gen.getSpaces();
            for (int j = 0; j < spaces.length; j++) {
                Space space = spaces[j];
                assertEquals(SPACE_NAME[i][j], space.getName());
                assertEquals(SPACE_CAPS[i][j], (Long) space.getCapacity());
                assertEquals(SPACE_MAX_CAPS[i][j], (Long) space.getMaxCapacity());
                assertEquals(SPACE_USED[i][j], (Long) space.getUsed());
            }
        }
    }

    @Test
    public void testRecordTlabStatWhenDataIsMissing() {
        when(extractor.getTlabTotalAllocatingThreads(VmTlabStat.UNKNOWN)).thenReturn(VmTlabStat.UNKNOWN);
        when(extractor.getTlabTotalAllocations(VmTlabStat.UNKNOWN)).thenReturn(VmTlabStat.UNKNOWN);
        when(extractor.getTlabTotalRefills(VmTlabStat.UNKNOWN)).thenReturn(VmTlabStat.UNKNOWN);
        when(extractor.getTlabMaxRefills(VmTlabStat.UNKNOWN)).thenReturn(VmTlabStat.UNKNOWN);
        when(extractor.getTlabTotalSlowAllocs(VmTlabStat.UNKNOWN)).thenReturn(VmTlabStat.UNKNOWN);
        when(extractor.getTlabMaxSlowAllocs(VmTlabStat.UNKNOWN)).thenReturn(VmTlabStat.UNKNOWN);
        when(extractor.getTlabTotalGcWaste(VmTlabStat.UNKNOWN)).thenReturn(VmTlabStat.UNKNOWN);
        when(extractor.getTlabMaxGcWaste(VmTlabStat.UNKNOWN)).thenReturn(VmTlabStat.UNKNOWN);
        when(extractor.getTlabTotalSlowWaste(VmTlabStat.UNKNOWN)).thenReturn(VmTlabStat.UNKNOWN);
        when(extractor.getTlabMaxSlowWaste(VmTlabStat.UNKNOWN)).thenReturn(VmTlabStat.UNKNOWN);
        when(extractor.getTlabTotalFastWaste(VmTlabStat.UNKNOWN)).thenReturn(VmTlabStat.UNKNOWN);
        when(extractor.getTlabMaxFastWaste(VmTlabStat.UNKNOWN)).thenReturn(VmTlabStat.UNKNOWN);

        vmListener.recordTlabStat(extractor);

        ArgumentCaptor<VmTlabStat> captor = ArgumentCaptor.forClass(VmTlabStat.class);
        verify(vmTlabStatDAO).putStat(captor.capture());
        VmTlabStat tlabStat = captor.getValue();

        assertEquals(VmTlabStat.UNKNOWN, tlabStat.getTotalAllocatingThreads());
        assertEquals(VmTlabStat.UNKNOWN, tlabStat.getTotalAllocations());
        assertEquals(VmTlabStat.UNKNOWN, tlabStat.getTotalRefills());
        assertEquals(VmTlabStat.UNKNOWN, tlabStat.getMaxRefills());
        assertEquals(VmTlabStat.UNKNOWN, tlabStat.getTotalSlowAllocations());
        assertEquals(VmTlabStat.UNKNOWN, tlabStat.getMaxSlowAllocations());
        assertEquals(VmTlabStat.UNKNOWN, tlabStat.getTotalGcWaste());
        assertEquals(VmTlabStat.UNKNOWN, tlabStat.getMaxGcWaste());
        assertEquals(VmTlabStat.UNKNOWN, tlabStat.getTotalSlowWaste());
        assertEquals(VmTlabStat.UNKNOWN, tlabStat.getMaxSlowWaste());
        assertEquals(VmTlabStat.UNKNOWN, tlabStat.getTotalFastWaste());
        assertEquals(VmTlabStat.UNKNOWN, tlabStat.getMaxFastWaste());
    }

}


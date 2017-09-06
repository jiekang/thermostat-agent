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

package com.redhat.thermostat.jvm.overview.agent.internal.model;

import com.redhat.thermostat.common.utils.LoggingUtils;
import com.redhat.thermostat.jvm.overview.agent.VmUpdate;
import com.redhat.thermostat.jvm.overview.agent.VmUpdateException;
import com.redhat.thermostat.jvm.overview.agent.VmUpdateListener;
import java.util.logging.Level;
import java.util.logging.Logger;

public class VmNativeLibsVmListener implements VmUpdateListener {

    private static final Logger logger = LoggingUtils.getLogger(VmNativeLibsVmListener.class);

    private final VmInfoDAO vmInfoDAO;
    private final String vmId;
    private final int pid;
    private final VmNativeLibsExtractor libExtractor;

    public VmNativeLibsVmListener(VmInfoDAO vmInfoDAO, String vmId, int pid) {
        this.vmInfoDAO = vmInfoDAO;
        this.vmId = vmId;
        this.pid = pid;
        this.libExtractor = VmNativeLibsExtractorFactory.getInstance(pid);
    }

    @Override
    public void countersUpdated(VmUpdate update) {
        VmClassloaderInfoExtractor extractor = new VmClassloaderInfoExtractor(update);
        recordNativeLibs(extractor);
    }

    /**
     * Heuristic for when to extract native libraries: either number of loaded
     * classes counter changed or this counter did not change but N different 
     * classes were loaded instead of N unloaded ones before next VmEvent was 
     * issued, therefore leaving the amount of loaded classes the same but with
     * different set of classes.
     */
    void recordNativeLibs(VmClassloaderInfoExtractor extractor) {
        try {
            Long loadedClassesCount = extractor.getLoadedClassesCount();
            Long unloadedClassesCount = extractor.getUnloadedClassesCount();
            if (loadedClassesCount != null || unloadedClassesCount != null) {
                vmInfoDAO.updateVmNativeLibs(vmId, libExtractor.getNativeLibs());
            }
        } catch (VmUpdateException ex) {
            logger.log(Level.WARNING, "Error gathering native libs for VM " + vmId, ex);
        }
    }

}

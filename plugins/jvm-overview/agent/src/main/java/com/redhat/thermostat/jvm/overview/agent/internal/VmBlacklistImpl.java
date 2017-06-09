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

package com.redhat.thermostat.jvm.overview.agent.internal;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.redhat.thermostat.common.Filter;
import com.redhat.thermostat.jvm.overview.agent.VmBlacklist;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;

@Component
@Service(value = VmBlacklist.class)
public class VmBlacklistImpl implements VmBlacklist {
    
    private final List<Filter<String>> filters;
    
    public VmBlacklistImpl() {
        this.filters = new CopyOnWriteArrayList<>();
    }

    @Override
    public void addVmFilter(Filter<String> filter) {
        filters.add(filter);
    }

    @Override
    public void removeVmFilter(Filter<String> filter) {
        filters.remove(filter);
    }

    @Override
    public boolean isBlacklisted(String mainClass) {
        boolean result = false;
        for (Filter<String> filter : filters) {
            if (filter.matches(mainClass)) {
                result = true;
            }
        }
        return result;
    }

}


/*
 * Copyright 2012 Red Hat, Inc.
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

package com.redhat.thermostat.thread.client.controller.osgi;

import java.util.Map;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.redhat.thermostat.client.osgi.service.ApplicationService;
import com.redhat.thermostat.client.osgi.service.VmInformationService;
import com.redhat.thermostat.common.MultipleServiceTracker;
import com.redhat.thermostat.common.MultipleServiceTracker.Action;
import com.redhat.thermostat.thread.client.common.ThreadViewProvider;
import com.redhat.thermostat.thread.client.controller.impl.ThreadInformationService;
import com.redhat.thermostat.thread.collector.ThreadCollectorFactory;

public class Activator implements BundleActivator {

    @SuppressWarnings({ "rawtypes" })
    @Override
    public void start(final BundleContext context) throws Exception {
        
        Class[] classes = new Class[] {
                ThreadCollectorFactory.class,
                ApplicationService.class,
                ThreadViewProvider.class
        };
        
        Action action = new Action() {
            @Override
            public void doIt(Map<String, Object> services) {
                ThreadCollectorFactory collectorFactory = (ThreadCollectorFactory) services.get(ThreadCollectorFactory.class.getName());
                ApplicationService applicationService = (ApplicationService) services.get(ApplicationService.class.getName());
                ThreadViewProvider viewFactory = (ThreadViewProvider) services.get(ThreadViewProvider.class.getName());
                
                VmInformationService vmInfoService = new ThreadInformationService(applicationService, collectorFactory, viewFactory);
                context.registerService(VmInformationService.class.getName(), vmInfoService, null);
            }
        };
        
        MultipleServiceTracker tracker = new MultipleServiceTracker(context, classes, action);
        tracker.open();
    }
    
    @Override
    public void stop(BundleContext context) throws Exception {}
}
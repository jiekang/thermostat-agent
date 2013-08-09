/*
 * Copyright 2012, 2013 Red Hat, Inc.
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

package com.redhat.thermostat.storage.core;

import java.util.concurrent.ExecutorService;

import com.redhat.thermostat.storage.core.AggregateQuery.AggregateFunction;
import com.redhat.thermostat.storage.model.Pojo;

public class QueuedBackingStorage extends QueuedStorage implements
        BackingStorage {
    
    public QueuedBackingStorage(BackingStorage delegate) {
        super(delegate);
    }

    QueuedBackingStorage(BackingStorage delegate, ExecutorService executor,
            ExecutorService fileExecutor) {
        super(delegate, executor, fileExecutor);
    }

    @Override
    public <T extends Pojo> Query<T> createQuery(Category<T> category) {
        return ((BackingStorage) delegate).createQuery(category);
    }
    
    @Override
    public <T extends Pojo> PreparedStatement<T> prepareStatement(
            StatementDescriptor<T> desc) throws DescriptorParsingException {
        // FIXME: Use some kind of cache in order to avoid parsing of
        // descriptors each time this is called. At least if the descriptor
        // class is the same we should be able to do something here.
        
        // Don't just defer to the delegate, since we want statements
        // prepared by this method to create queries using the
        // createQuery method in this class.
        return PreparedStatementFactory.getInstance(this, desc);
    }

    @Override
    public <T extends Pojo> Query<T> createAggregateQuery(
            AggregateFunction function, Category<T> category) {
        return ((BackingStorage) delegate).createAggregateQuery(function, category);
    }

}
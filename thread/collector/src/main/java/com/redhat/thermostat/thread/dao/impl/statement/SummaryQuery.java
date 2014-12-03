/*
 * Copyright 2012-2014 Red Hat, Inc.
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

package com.redhat.thermostat.thread.dao.impl.statement;

import com.redhat.thermostat.storage.core.experimental.statement.FieldDescriptor;
import com.redhat.thermostat.storage.core.experimental.statement.Id;
import com.redhat.thermostat.storage.core.experimental.statement.LimitCriterion;
import com.redhat.thermostat.storage.core.experimental.statement.Query;
import com.redhat.thermostat.storage.core.experimental.statement.SortCriterion;
import com.redhat.thermostat.storage.core.experimental.statement.StatementUtils;
import com.redhat.thermostat.storage.core.experimental.statement.TypeMapper;
import com.redhat.thermostat.storage.core.experimental.statement.WhereCriterion;
import com.redhat.thermostat.thread.model.ThreadSession;
import com.redhat.thermostat.thread.model.ThreadSummary;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class SummaryQuery extends Query<ThreadSummary> {

    public static final Id id = new Id(SummaryQuery.class.getSimpleName());

    public static class CriteriaId {
        public static final Id getVmId = new Id("0");
        public static final Id timeStampGEQ = new Id("1");
        public static final Id timeStampLEQ = new Id("2");
        public static final Id sessionID = new Id("3");
        public static final Id limit = new Id("4");

    }

    @Override
    public Id getId() {
        return id;
    }

    @Override
    protected void describe(Criteria criteria) {
        List<FieldDescriptor> descriptors = StatementUtils.createDescriptors
                (ThreadSession.class);
        final Map<String, FieldDescriptor> map = StatementUtils.createDescriptorMap(descriptors);

        criteria.add(new WhereCriterion(CriteriaId.getVmId, map.get("vmId"),
                                        TypeMapper.Criteria.Equal));
        criteria.add(new WhereCriterion(CriteriaId.sessionID, map.get("session"),
                                        TypeMapper.Criteria.Equal));
        criteria.add(new WhereCriterion(CriteriaId.timeStampGEQ, map.get("timeStamp"),
                                        TypeMapper.Criteria.GreaterEqual));
        criteria.add(new WhereCriterion(CriteriaId.timeStampLEQ, map.get("timeStamp"),
                                        TypeMapper.Criteria.LessEqual));

        criteria.add(new SortCriterion(map.get("timeStamp"), TypeMapper.Sort.Descending));

        criteria.add(new LimitCriterion(CriteriaId.limit));
    }
}

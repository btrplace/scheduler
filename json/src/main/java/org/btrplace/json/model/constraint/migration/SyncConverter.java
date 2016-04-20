/*
 * Copyright (c) 2016 University Nice Sophia Antipolis
 *
 * This file is part of btrplace.
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.btrplace.json.model.constraint.migration;

import net.minidev.json.JSONObject;
import org.btrplace.json.JSONConverterException;
import org.btrplace.json.model.constraint.ConstraintConverter;
import org.btrplace.model.constraint.migration.Sync;

/**
 * JSON Converter for the constraint {@link org.btrplace.model.constraint.migration.Sync}.
 *
 * @author Vincent Kherbache
 * @see org.btrplace.model.constraint.migration.Sync
 */
public class SyncConverter extends ConstraintConverter<Sync> {

    @Override
    public Class<Sync> getSupportedConstraint() {
        return Sync.class;
    }

    @Override
    public String getJSONId() {
        return "Sync";
    }

    @Override
    public Sync fromJSON(JSONObject in) throws JSONConverterException {
        checkId(in);
        return new Sync(requiredVMs(in, "vms"));
    }

    @Override
    public JSONObject toJSON(Sync sync) throws JSONConverterException {
        JSONObject c = new JSONObject();
        c.put("id", getJSONId());
        c.put("vms", vmsToJSON(sync.getInvolvedVMs()));
        c.put("continuous", sync.isContinuous());
        return c;
    }
}

/*
 * Copyright (c) 2020 University Nice Sophia Antipolis
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

package org.btrplace.json.plan;

import net.minidev.json.JSONObject;
import org.btrplace.json.JSONConverterException;
import org.btrplace.json.JSONs;
import org.btrplace.model.Model;
import org.btrplace.plan.event.BootNode;

/**
 * JSON serialisation for {@link org.btrplace.plan.event.BootNode} actions.
 */
public class BootNodeConverter implements ActionConverter<BootNode> {

  @Override
  public String id() {
    return "bootNode";
  }

  @Override
  public Class<BootNode> supportedAction() {
    return BootNode.class;
  }

  @Override
  public void fillJSON(final BootNode action, final JSONObject ob) {
    ob.put(NODE_LABEL, JSONs.elementToJSON(action.getNode()));
  }

  @Override
  public BootNode fromJSON(final Model mo, final JSONObject ob)
      throws JSONConverterException {

    return new BootNode(JSONs.requiredNode(mo, ob, NODE_LABEL), start(ob),
        end(ob));
  }
}

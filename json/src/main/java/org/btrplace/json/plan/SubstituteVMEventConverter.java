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
import org.btrplace.plan.event.SubstitutedVMEvent;

/**
 * JSON serialisation for {@link org.btrplace.plan.event.SubstitutedVMEvent}
 * events.
 */
public class SubstituteVMEventConverter implements
    EventConverter<SubstitutedVMEvent> {

  @Override
  public String id() {
    return "substitutedVM";
  }

  @Override
  public Class<SubstitutedVMEvent> supportedEvent() {
    return SubstitutedVMEvent.class;
  }

  /**
   * Fill the JSON skeleton that will represent this action. The events will be
   * added in a later stage.
   *
   * @param ev the action.
   * @param ob the object to fill.
   */
  @Override
  public void fillJSON(final SubstitutedVMEvent ev, final JSONObject ob) {
    ob.put(ActionConverter.VM_LABEL, JSONs.elementToJSON(ev.getVM()));
    ob.put("newVm", JSONs.elementToJSON(ev.getNewVM()));
  }

  @Override
  public SubstitutedVMEvent fromJSON(final Model mo, final JSONObject ob)
      throws JSONConverterException {

    return new SubstitutedVMEvent(JSONs.requiredVM(mo, ob,
        ActionConverter.VM_LABEL), JSONs.requiredVM(mo, ob, "newVm"));
  }
}

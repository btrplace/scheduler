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
import org.btrplace.model.Model;
import org.btrplace.plan.event.Event;

/**
 * Specify a JSON converter for a {@link Event}.
 *
 * @author Fabien Hermenier
 */
public interface EventConverter<E extends Event> {

  /**
   * The JSON identifier for the event.
   *
   * @return a String that will be written inside the field
   * {@link ActionConverter#ID_LABEL}
   */
  String id();

  /**
   * The action that is supported by this converter.
   *
   * @return the event class.
   */
  Class<E> supportedEvent();

  void fillJSON(final E ev, final JSONObject ob);

  /**
   * Create an event from a JSON Object.
   *
   * @param mo the model to use to parse elements.
   * @param ob the object to convert
   * @return the resulting event.
   * @throws JSONConverterException if the conversion failed.
   */
  E fromJSON(final Model mo, final JSONObject ob)
      throws JSONConverterException;

  /**
   * Create a JSON from an event.
   * First, a skeleton is created to mention the event identifier,
   * Second, {@link #fillJSON} is called.
   *
   * @param ev the event to convert.
   * @return the resulting JSON message.
   */
  default JSONObject toJSON(final E ev) {
    final JSONObject ob = new JSONObject();
    ob.put(ActionConverter.ID_LABEL, id());
    fillJSON(ev, ob);
    return ob;
  }
}

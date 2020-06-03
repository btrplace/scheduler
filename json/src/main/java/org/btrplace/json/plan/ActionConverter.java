/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.json.plan;

import net.minidev.json.JSONObject;
import org.btrplace.json.JSONConverterException;
import org.btrplace.json.JSONs;
import org.btrplace.model.Model;
import org.btrplace.plan.event.Action;

/**
 * Specify a JSON converter for a {@link Action}.
 *
 * @author Fabien Hermenier
 */
public interface ActionConverter<E extends Action> {

  /**
   * Key that indicates the beginning an action.
   */
  String START_LABEL = "start";

  /**
   * Key that indicates the end an action.
   */
  String END_LABEL = "end";

  /**
   * Key that indicate the action identifier
   */
  String ID_LABEL = "id";

  /**
   * Key that indicates a VM identifier.
   */
  String VM_LABEL = "vm";

  /**
   * Key that indicates a node identifier.
   */
  String NODE_LABEL = "node";

  /**
   * Key that indicates the current position of a VM.
   */
  String ON_LABEL = "on";

  String VM_LOCATION_LABEL = "from";

  String VM_DESTINATION_LABEL = "to";

  /**
   * Key to indicate a resource identifier.
   */
  String RC_LABEL = "rc";

  /**
   * Key to indicate a resource amount.
   */
  String RC_AMOUNT_LABEL = "amount";

  /**
   * The JSON identifier for the action.
   *
   * @return a String that will be written inside the field
   * {@link #ID_LABEL}
   */
  String id();

  /**
   * The action that is supported by this converter.
   *
   * @return the action class.
   */
  Class<E> supportedAction();

  /**
   * Fill the JSON skeleton that will represent this action. The events will
   * be added in a later stage.
   *
   * @param action the action.
   * @param ob     the object to fill.
   */
  void fillJSON(final E action, final JSONObject ob);

  /**
   * Create an action from a JSON Object. The events attached to the
   * hooks will be parsed in a later stage.
   *
   * @param mo the model to use to parse elements.
   * @param ob the object to convert
   * @return the resulting action.
   * @throws JSONConverterException if the conversion failed.
   */
  E fromJSON(final Model mo, final JSONObject ob) throws JSONConverterException;

  /**
   * Get the start moment for the given JSON action.
   *
   * @param json the JSON to analyse.
   * @return the parsed start moment.
   * @throws JSONConverterException in case it was not possible to get the
   *                                moment.
   */
  default int start(final JSONObject json) throws JSONConverterException {
    return JSONs.requiredInt(json, START_LABEL);
  }

  /**
   * Get the end moment for the given JSON action.
   *
   * @param json the JSON to analyse.
   * @return the parsed end moment.
   * @throws JSONConverterException in case it was not possible to get the
   *                                moment.
   */
  default int end(final JSONObject json) throws JSONConverterException {
    return JSONs.requiredInt(json, END_LABEL);
  }

  /**
   * Create a JSON from an action.
   * First, a skeleton is created to mention the action identifier,
   * the start and the end moments. Second, {@link #fillJSON} is called.
   *
   * @param action the action to convert.
   * @return the resulting JSON message.
   */
  default JSONObject toJSON(final E action) {
    final JSONObject ob = new JSONObject();
    ob.put(ID_LABEL, id());
    ob.put(START_LABEL, action.getStart());
    ob.put(END_LABEL, action.getEnd());
    fillJSON(action, ob);
    return ob;
  }
}

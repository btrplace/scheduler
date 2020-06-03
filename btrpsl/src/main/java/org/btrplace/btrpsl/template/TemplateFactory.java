/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.btrpsl.template;

import org.btrplace.btrpsl.Script;
import org.btrplace.model.Element;

import java.util.Map;
import java.util.Set;

/**
 * A factory of templates.
 * Allow to register several templates and to check element from the template name
 * and the element identifier.
 *
 * @author Fabien Hermenier
 */
public interface TemplateFactory {

    /**
     * Get the available templates.
     *
     * @return a set of templates identifier that may be empty
     */
    Set<String> getAvailables();

    /**
     * Test if a template is available.
     *
     * @param id the template identifier
     * @return {@code true} iff the template is available
     */
    boolean isAvailable(String id);

    /**
     * Build an element.
     *
     * @param scr     the script the element belongs to.
     * @param tplName the template name for the element
     * @param e       the element associated to the template
     * @param attrs   the attributes related to the element.  @return the built element if succeed
     * @throws ElementBuilderException if an error occurred
     */
    void check(Script scr, String tplName, Element e, Map<String, String> attrs) throws ElementBuilderException;

    /**
     * Register a template.
     *
     * @param tpl the template to register
     * @return the template that was previously registered in place of {@code tpl}.
     */
    Template register(Template tpl);
}

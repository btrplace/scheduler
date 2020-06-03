/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.btrpsl.template;

import org.btrplace.btrpsl.Script;
import org.btrplace.model.Element;
import org.btrplace.model.Model;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * A default, lazy checker for templates.
 * Accept everything.
 *
 * @author Fabien Hermenier
 */
public class MockTemplateFactory implements TemplateFactory {

  private final Model mo;

    /**
     * Make a new factory that is not strict.
     *
     * @param m the model to rely on
     */
    public MockTemplateFactory(Model m) {
        this.mo = m;
    }

    @Override
    public Set<String> getAvailables() {
        return Collections.emptySet();
    }

    @Override
    public boolean isAvailable(String id) {
        return true;
    }

    @Override
    public void check(Script scr, String tplName, Element e, Map<String, String> attrs) throws ElementBuilderException {
        for (Map.Entry<String, String> attr : attrs.entrySet()) {
            String value = "true";
            if (attr.getValue() != null) {
                value = attr.getValue();
            }
            mo.getAttributes().castAndPut(e, attr.getKey(), value);
        }
        mo.getAttributes().put(e, "template", tplName);
    }

    @Override
    public Template register(Template tpl) {
        throw new UnsupportedOperationException();
    }
}

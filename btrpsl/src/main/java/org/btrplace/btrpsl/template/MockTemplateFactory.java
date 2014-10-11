/*
 * Copyright (c) 2014 University Nice Sophia Antipolis
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

    private Model mo;

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

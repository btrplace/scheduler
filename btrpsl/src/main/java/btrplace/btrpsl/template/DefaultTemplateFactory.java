/*
 * Copyright (c) 2013 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package btrplace.btrpsl.template;

import btrplace.btrpsl.NamingService;
import btrplace.btrpsl.Script;
import btrplace.btrpsl.element.BtrpOperand;
import btrplace.model.Element;
import btrplace.model.Model;
import btrplace.model.Node;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Default implementation for {@link TemplateFactory}.
 * If the factory is strict, the template must be available
 * otherwise, a fake template will be used.
 *
 * @author Fabien Hermenier
 */
public class DefaultTemplateFactory implements TemplateFactory {

    private Map<String, Template> vmTpls;

    private Map<String, Template> nodeTpls;

    private NamingService namingServer;

    private Model mo;

    /**
     * Make a new factory.
     *
     * @param srv the naming service to rely on
     */
    public DefaultTemplateFactory(NamingService srv, Model m) {
        this.namingServer = srv;
        this.mo = m;
        vmTpls = new HashMap<>();
        nodeTpls = new HashMap<>();
    }

    @Override
    public Set<String> getAvailables() {
        return vmTpls.keySet();
    }

    @Override
    public boolean isAvailable(String id) {
        return vmTpls.containsKey(id);
    }

    @Override
    public void check(Script scr, String tplName, Element e, Map<String, String> attrs) throws ElementBuilderException {
        //Check if the current element already has a template, report an error if they differ
        String currentTpl = mo.getAttributes().getString(e, "template");
        if (!tplName.equals(currentTpl)) {
            throw new ElementBuilderException(namingServer.resolve(e) + " already implements '" + currentTpl + "'. Redefinition is not allowed");
        }
        Template tpl;
        if (e instanceof Node) {
            tpl = nodeTpls.get(tplName);
        } else {
            tpl = vmTpls.get(tplName);
        }
        if (tpl == null) {
            throw new ElementBuilderException("Unknown template '" + tplName + "'");
        }
        tpl.check(scr, e, attrs);
    }

    @Override
    public Template register(Template tpl) {
        tpl.setNamingService(namingServer);
        if (tpl.getElementType() == BtrpOperand.Type.VM) {
            return vmTpls.put(tpl.getIdentifier(), tpl);
        } else if (tpl.getElementType() == BtrpOperand.Type.node) {
            return nodeTpls.put(tpl.getIdentifier(), tpl);
        }
        return null;
    }
}

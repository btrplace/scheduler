/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.btrpsl.template;

import org.btrplace.btrpsl.Script;
import org.btrplace.btrpsl.element.BtrpOperand;
import org.btrplace.model.Element;
import org.btrplace.model.Model;
import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.model.view.NamingService;

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

  private final Map<String, Template> vmTpls;

  private final Map<String, Template> nodeTpls;

  private final NamingService<Node> namingServerNodes;

  private final NamingService<VM> namingServerVMs;

  private final Model mo;

    /**
     * Make a new factory.
     *
     * @param srvNodes the nodes naming service to rely on
     * @param srvVMs   the vms naming service to rely on
     * @param m the model we focus on
     */
    public DefaultTemplateFactory(NamingService<Node> srvNodes, NamingService<VM> srvVMs, Model m) {
        this.namingServerNodes = srvNodes;
        this.namingServerVMs = srvVMs;
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
        String currentTpl = mo.getAttributes().get(e, "template", "");
        if (!tplName.equals(currentTpl)) {
            throw new ElementBuilderException(e.id() + " already implements '" + currentTpl + "'. Redefinition is not allowed");
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
        tpl.check();
    }

    @Override
    public Template register(Template tpl) {
        tpl.setNamingServiceNodes(namingServerNodes);
        tpl.setNamingServiceVMs(namingServerVMs);
        if (tpl.getElementType() == BtrpOperand.Type.VM) {
            return vmTpls.put(tpl.getIdentifier(), tpl);
        } else if (tpl.getElementType() == BtrpOperand.Type.NODE) {
            return nodeTpls.put(tpl.getIdentifier(), tpl);
        }
        return null;
    }
}

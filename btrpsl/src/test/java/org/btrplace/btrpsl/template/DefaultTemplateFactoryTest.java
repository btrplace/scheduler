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
import org.btrplace.btrpsl.element.BtrpElement;
import org.btrplace.btrpsl.element.BtrpOperand;
import org.btrplace.model.*;
import org.btrplace.model.view.NamingService;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Unit tests for {@link DefaultTemplateFactory}.
 *
 * @author Fabien Hermenier
 */
public class DefaultTemplateFactoryTest {

    public static class MockVMTemplate implements Template {

        NamingService srvNodes;
        NamingService srvVMs;
        String tplName;

        @Override
        public BtrpOperand.Type getElementType() {
            return BtrpOperand.Type.VM;
        }

        public MockVMTemplate(String n) {
            tplName = n;
        }

        @Override
        public BtrpElement check(Script scr, Element e, Map<String, String> options) throws ElementBuilderException {
            return null;
        }

        @Override
        public String getIdentifier() {
            return tplName;
        }

        @Override
        public void setNamingServiceNodes(NamingService<Node> srvNodes) {
            this.srvNodes = srvNodes;
        }

        @Override
        public void setNamingServiceVMs(NamingService<VM> srvVMs) {
            this.srvVMs = srvVMs;
        }
    }

    public static class MockNodeTemplate implements Template {

        String tplName;

        private NamingService<Node> srvNodes;
        private NamingService<VM> srvVMs;

        public Model mo;

        @Override
        public BtrpOperand.Type getElementType() {
            return BtrpOperand.Type.node;
        }

        public MockNodeTemplate(String n) {
            tplName = n;
        }

        @Override
        public BtrpElement check(Script scr, Element e, Map<String, String> options) throws ElementBuilderException {
            BtrpElement el = new BtrpElement(getElementType(), "foo", mo.newVM());
            mo.getAttributes().put(el.getElement(), "template", getIdentifier());
            return el;
        }

        @Override
        public String getIdentifier() {
            return tplName;
        }

        @Override
        public void setNamingServiceVMs(NamingService<VM> srvVMs) {
            this.srvVMs = srvVMs;
        }

        @Override
        public void setNamingServiceNodes(NamingService<Node> srvNodes) {
            this.srvNodes = srvNodes;
        }
    }

    @Test
    public void testInstantiation() {
        DefaultTemplateFactory tplf = new DefaultTemplateFactory(NamingService.newNodeNS(), NamingService.newVMNS(), new DefaultModel());
        Assert.assertTrue(tplf.getAvailables().isEmpty());
    }

    @Test(dependsOnMethods = {"testInstantiation"})
    public void testRegister() {
        NamingService srvNodes = NamingService.newNodeNS();
        NamingService srvVMs = NamingService.newVMNS();
        DefaultTemplateFactory tplf = new DefaultTemplateFactory(srvNodes, srvVMs, new DefaultModel());
        MockVMTemplate t1 = new MockVMTemplate("mock1");
        Assert.assertNull(tplf.register(t1));
        Assert.assertEquals(t1.srvNodes, srvNodes);
        Assert.assertEquals(t1.srvVMs, srvVMs);
        Assert.assertTrue(tplf.getAvailables().contains("mock1"));
        MockVMTemplate t2 = new MockVMTemplate("mock2");
        Assert.assertNull(tplf.register(t2));
        Assert.assertEquals(t2.srvNodes, srvNodes);
        Assert.assertEquals(t2.srvVMs, srvVMs);
        Assert.assertTrue(tplf.getAvailables().contains("mock2") && tplf.getAvailables().size() == 2);

    }

    /*@Test(dependsOnMethods = {"testInstantiation", "testRegister"})
    public void testAccessibleWithStrict() throws ElementBuilderException {
        Model mo = new DefaultModel();
        DefaultTemplateFactory tplf = new DefaultTemplateFactory(new InMemoryNamingService(mo));
        tplf.register(new MockVMTemplate("mock1"));
        Script scr = new Script();
        tplf.check(scr, "mock1", null, new HashMap<String, String>());
        Assert.assertEquals(mo.getAttributes().get(el.getElement(), "template"), "mock1");
    }         */

    /*@Test(dependsOnMethods = {"testInstantiation", "testRegister"}, expectedExceptions = {ElementBuilderException.class})
    public void testInaccessibleWithStrict() throws ElementBuilderException {
        DefaultTemplateFactory tplf = new DefaultTemplateFactory(new InMemoryNamingService(), new DefaultModel());
        Script scr = new Script();
        tplf.check(scr, "bar", , "foo", new HashMap<String, String>());
    } */

    /*@Test(dependsOnMethods = {"testInstantiation", "testRegister"})
    public void testAccessibleWithoutStrict() throws ElementBuilderException {
        Model mo = new DefaultModel();
        DefaultTemplateFactory tplf = new DefaultTemplateFactory(new InMemoryNamingService(), mo);
        tplf.register(new MockVMTemplate("mock1"));
        Script scr = new Script();
        tplf.check(scr, "mock1", null, new HashMap<String, String>());
        Assert.assertEquals(mo.getAttributes().get(el.getElement(), "template"), "mock1");
    } */

    /*@Test(dependsOnMethods = {"testInstantiation", "testRegister"})
    public void testInaccessibleWithoutStrict() throws ElementBuilderException {
        Model mo = new DefaultModel();
        DefaultTemplateFactory tplf = new DefaultTemplateFactory(new InMemoryNamingService(), mo);
        Map<String, String> m = new HashMap<>();
        m.put("migratable", null);
        m.put("foo", "7.5");
        m.put("bar", "1243");
        m.put("template", "bar");
        Script scr = new Script();
        tplf.check(scr, "bar", null, m);
        Assert.assertEquals(mo.getAttributes().get(el.getElement(), "template"), "bar");
        Assert.assertEquals(el.getName(), "foo");
        Assert.assertTrue(mo.getAttributes().getBoolean(el.getElement(), "migratable"));
        Assert.assertEquals(mo.getAttributes().getInteger(el.getElement(), "bar").longValue(), 1243);
        Assert.assertEquals(mo.getAttributes().getDouble(el.getElement(), "foo"), 7.5);
        Assert.assertEquals(mo.getAttributes().getKeys(el.getElement()), m.keySet());
    }                    */

    @Test(expectedExceptions = {ElementBuilderException.class})
    public void testTypingIssue() throws ElementBuilderException {
        Model mo = new DefaultModel();
        DefaultTemplateFactory tplf = new DefaultTemplateFactory(NamingService.newNodeNS(), NamingService.newVMNS(), mo);
        tplf.register(new MockVMTemplate("mock1"));
        Script scr = new Script();
        tplf.check(scr, "mock1", mo.newNode(), new HashMap<String, String>());
    }

}

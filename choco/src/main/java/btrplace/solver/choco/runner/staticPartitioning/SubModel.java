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

package btrplace.solver.choco.runner.staticPartitioning;

import btrplace.model.*;
import btrplace.model.view.ModelView;

import java.util.Collection;

/**
 * Define a model that is a sub-model of a bigger one.
 * The operation on a sub-model are limited to the elements that belong
 * to the given scope.
 * <p/>
 * <ul>
 * <li>Elements created in a sub-model are integrated automatically into the
 * parent component</li>
 * <li>It is not allowed to attach/detach/clear views</li>
 * <li>It is not possible to set the attributes</li>
 * </ul>
 *
 * @author Fabien Hermenier
 */
public class SubModel implements Model {

    private Model parent;

    private Collection<Node> scope;

    private SubMapping sm;

    private ElementBuilder eb;

    /**
     * Make a new submodel.
     *
     * @param parent the parent model
     * @param eb     the element builder to rely on
     * @param scope  the node to restrict the model on.
     */
    public SubModel(Model parent, ElementBuilder eb, Collection<Node> scope) {
        this.scope = scope;
        this.parent = parent;
        this.eb = eb;
        sm = new SubMapping(parent.getMapping(), scope);
    }

    /**
     * Get the scope of the model.
     *
     * @return a set of nodes
     */
    public Collection<Node> getScope() {
        return scope;
    }

    @Override
    public ModelView getView(String id) {
        return parent.getView(id);
    }

    @Override
    public Collection<ModelView> getViews() {
        return parent.getViews();
    }

    /**
     * Unsupported.
     */
    @Override
    public boolean attach(ModelView v) {
        throw new UnsupportedOperationException();
    }

    /**
     * Unsupported.
     */
    @Override
    public boolean detach(ModelView v) {
        throw new UnsupportedOperationException();
    }

    /**
     * Unsupported.
     */
    @Override
    public void clearViews() {
        throw new UnsupportedOperationException();
    }

    /**
     * Get a mapping that is limited to the given scope.
     *
     * @return a mapping
     */
    @Override
    public SubMapping getMapping() {
        return sm;
    }

    @Override
    public Attributes getAttributes() {
        return parent.getAttributes();
    }

    /**
     * Unsupported.
     */
    @Override
    public void setAttributes(Attributes attrs) {
        throw new UnsupportedOperationException();
    }

    /**
     * Unsupported.
     */
    @Override
    public Model clone() {
        DefaultModel m = new DefaultModel(eb.clone());
        MappingUtils.fill(sm, m.getMapping());
        for (ModelView rc : parent.getViews()) {
            m.attach(rc.clone());
        }
        m.setAttributes(this.getAttributes().clone());
        return m;
    }

    @Override
    public VM newVM() {
        VM v = eb.newVM();
        if (v != null) {
            parent.newVM(v.id());
        }
        return v;
    }

    @Override
    public VM newVM(int id) {
        VM v = eb.newVM(id);
        if (v != null) {
            parent.newVM(id);
        }
        return v;

    }

    @Override
    public Node newNode() {
        Node n = eb.newNode();
        if (n != null) {
            parent.newNode(n.id());
        }
        return n;
    }

    @Override
    public Node newNode(int id) {
        Node n = eb.newNode(id);
        if (n != null) {
            parent.newNode(id);
        }
        return n;
    }

    @Override
    public boolean contains(VM v) {
        return parent.contains(v);
    }

    @Override
    public boolean contains(Node n) {
        return parent.contains(n);
    }
}


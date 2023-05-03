/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.btrpsl;

import gnu.trove.map.TIntLongMap;
import gnu.trove.map.hash.TIntLongHashMap;
import org.antlr.runtime.ANTLRFileStream;
import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.btrplace.btrpsl.antlr.ANTLRBtrplaceSL2Lexer;
import org.btrplace.btrpsl.antlr.ANTLRBtrplaceSL2Parser;
import org.btrplace.btrpsl.constraint.ConstraintsCatalog;
import org.btrplace.btrpsl.constraint.DefaultConstraintsCatalog;
import org.btrplace.btrpsl.includes.Includes;
import org.btrplace.btrpsl.includes.PathBasedIncludes;
import org.btrplace.btrpsl.template.MockTemplateFactory;
import org.btrplace.btrpsl.template.TemplateFactory;
import org.btrplace.btrpsl.tree.BtrPlaceTree;
import org.btrplace.btrpsl.tree.BtrPlaceTreeAdaptor;
import org.btrplace.model.Model;
import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.model.view.NamingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Build scripts from textual descriptions.
 * For file based parsing, a LRU cache is used to prevent useless parsing. If the
 * last modification date of the script has not changed since its last parsing, the cached version
 * is returned.
 *
 * @author Fabien Hermenier
 */
public class ScriptBuilder {

  /**
   * The date of last modification for the file. The key is the hashcode of the file path.
   */
  private final TIntLongMap dates;

    public static final int DEFAULT_CACHE_SIZE = 100;

    private final Map<String, Script> cache;

  public static final Logger LOGGER = LoggerFactory.getLogger("ScriptBuilder");

    private ConstraintsCatalog catalog;

    private Includes includes;

    private TemplateFactory tpls;

    private final Model model;

  public static final String EXTENSION = "btrp";
    /**
     * The builder to use to make ErrorReporter.
     */
    private ErrorReporterBuilder errBuilder = new PlainTextErrorReporterBuilder();

    private NamingService<Node> namingServiceNodes;
    private NamingService<VM> namingServiceVMs;

    /**
     * Make a new builder with a default cache size.
     *
     * @param mo the model to rely on
     */
    public ScriptBuilder(Model mo) {
        this(DEFAULT_CACHE_SIZE, mo);
    }

    /**
     * Make a new builder.
     *
     * @param cacheSize the size of the cache
     * @param mo the model we focus on
     */
    public ScriptBuilder(final int cacheSize, Model mo) {

        this.model = mo;

        namingServiceNodes = NamingService.getNodeNames(mo);
        if (namingServiceNodes == null) {
            namingServiceNodes = NamingService.newNodeNS();
            mo.attach(namingServiceNodes);
        }
        namingServiceVMs = NamingService.getVMNames(mo);
        if (namingServiceVMs == null) {
            namingServiceVMs = NamingService.newVMNS();
            mo.attach(namingServiceVMs);
        }

        catalog = DefaultConstraintsCatalog.newBundle();
        this.tpls = new MockTemplateFactory(mo);
        this.dates = new TIntLongHashMap();
        this.includes = new PathBasedIncludes(this);
        this.cache = new LinkedHashMap<String, Script>() {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, Script> foo) {
                return size() == cacheSize;
            }
        };
    }

    /**
     * Get the possibles scripts that can be included
     *
     * @return an includes.
     */
    public Includes getIncludes() {
        return this.includes;
    }

    /**
     * Set the include library.
     *
     * @param incs the library to add
     */
    public void setIncludes(Includes incs) {
        this.includes = incs;
    }

    /**
     * Build a script from a file.
     *
     * @param f the file to parse
     * @return the resulting script
     * @throws ScriptBuilderException if an error occurred
     */
    public Script build(File f) throws ScriptBuilderException {
        int k = f.getAbsolutePath().hashCode();
        if (dates.containsKey(k) && dates.get(k) == f.lastModified() && cache.containsKey(f.getPath())) {
            LOGGER.debug("get '{}' from the cache", f.getName());
            return cache.get(f.getPath());
        }

        LOGGER.debug("{} is built from the file", f.getName());
        dates.put(k, f.lastModified());
        String name = f.getName();
        try {
            Script v = build(new ANTLRFileStream(f.getAbsolutePath()));
            if (!name.equals(v.getlocalName() + Script.EXTENSION)) {
                throw new ScriptBuilderException("Script '" + v.getlocalName()
                        + "' must be declared in a file named '" + v.getlocalName() + Script.EXTENSION);
            }
            cache.put(f.getPath(), v);
            return v;
        } catch (IOException e) {
            throw new ScriptBuilderException(e.getMessage(), e);
        }

    }

    /**
     * Build a script from a String.
     *
     * @param description the description of the script
     * @return the built script
     * @throws ScriptBuilderException if an error occurred while building the script
     */
    public Script build(String description) throws ScriptBuilderException {
        return build(new ANTLRStringStream(description));
    }

    /**
     * Internal method to check a script from a stream.
     *
     * @param cs the stream to analyze
     * @return the built script
     * @throws ScriptBuilderException in an error occurred while building the script
     */
    @SuppressWarnings("squid:S1166") //For the UnsupportedOperationException
    private Script build(CharStream cs) throws ScriptBuilderException {

        Script v = new Script();

        ANTLRBtrplaceSL2Lexer lexer = new ANTLRBtrplaceSL2Lexer(cs);

        ErrorReporter errorReporter = errBuilder.build(v);

        lexer.setErrorReporter(errorReporter);
        CommonTokenStream tokens = new CommonTokenStream(lexer);

        ANTLRBtrplaceSL2Parser parser = new ANTLRBtrplaceSL2Parser(tokens);
        parser.setErrorReporter(errorReporter);

        SymbolsTable t = new SymbolsTable();

        parser.setTreeAdaptor(new BtrPlaceTreeAdaptor(v, model, namingServiceNodes, namingServiceVMs, tpls, errorReporter, t, includes, catalog));

        try {
            BtrPlaceTree tree = (BtrPlaceTree) parser.script_decl().getTree();
            if (tree != null) {
                if (tree.token != null) {
                        tree.go(tree); //Single instruction
                } else {
                    for (int i = 0; i < tree.getChildCount(); i++) {
                            tree.getChild(i).go(tree);
                        }
                    }
            }
        } catch (RecognitionException e) {
            throw new ScriptBuilderException(e.getMessage(), e);
        } catch (UnsupportedOperationException e) {
            //We only keep the error message
            errorReporter.append(0, 0, e.getMessage());
        }
        if (!errorReporter.getErrors().isEmpty()) {
            throw new ScriptBuilderException(errorReporter);
        }
        return v;
    }

    /**
     * Indicate the {@link ErrorReporter} to instantiate before parsing
     * a script.
     *
     * @param b the builder to use
     */
    public void setErrorReporterBuilder(ErrorReporterBuilder b) {
        this.errBuilder = b;
    }

    /**
     * Get the naming service that is used to create VMs.
     *
     * @return the naming service provided at instantiation
     */
    public NamingService<VM> getNamingServiceVMs() {
        return this.namingServiceVMs;
    }

    /**
     * Get the naming service that is used to create Nodes.
     *
     * @return the naming service provided at instantiation
     */
    public NamingService<Node> getNamingServiceNodes() {
        return this.namingServiceNodes;
    }

    /**
     * Get the template factory used to instantiate elements
     * when needed.
     *
     * @return the current template factory
     */
    public TemplateFactory getTemplateFactory() {
        return tpls;
    }

    /**
     * Set the factory to use to instantiate elements
     * from template.
     *
     * @param f the factory to use
     */
    public void setTemplateFactory(TemplateFactory f) {
        this.tpls = f;
    }

    /**
     * Get the used catalog of constraints.
     *
     * @return the current catalog
     */
    public ConstraintsCatalog getConstraintsCatalog() {
        return catalog;
    }

    /**
     * Set the catalog of constraints to use.
     *
     * @param c the catalog to rely one
     */
    public void setConstraintsCatalog(ConstraintsCatalog c) {
        this.catalog = c;
    }
}

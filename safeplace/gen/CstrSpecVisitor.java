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

// Generated from /Users/fhermeni/Research/Code/Btrplace/solver/safeplace/src/main/antlr/btrplace/solver/api/cstrSpec/CstrSpec.g4 by ANTLR 4.x
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link CstrSpecParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface CstrSpecVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link CstrSpecParser#spec}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSpec(@NotNull CstrSpecParser.SpecContext ctx);
	/**
	 * Visit a parse tree produced by {@link CstrSpecParser#formulaOp}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFormulaOp(@NotNull CstrSpecParser.FormulaOpContext ctx);
	/**
	 * Visit a parse tree produced by {@link CstrSpecParser#call}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCall(@NotNull CstrSpecParser.CallContext ctx);
	/**
	 * Visit a parse tree produced by {@link CstrSpecParser#arrayTerm}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArrayTerm(@NotNull CstrSpecParser.ArrayTermContext ctx);
	/**
	 * Visit a parse tree produced by {@link CstrSpecParser#protectedTerm}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProtectedTerm(@NotNull CstrSpecParser.ProtectedTermContext ctx);
	/**
	 * Visit a parse tree produced by {@link CstrSpecParser#constraint}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstraint(@NotNull CstrSpecParser.ConstraintContext ctx);
	/**
	 * Visit a parse tree produced by {@link CstrSpecParser#protectedFormula}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProtectedFormula(@NotNull CstrSpecParser.ProtectedFormulaContext ctx);
	/**
	 * Visit a parse tree produced by {@link CstrSpecParser#idTerm}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdTerm(@NotNull CstrSpecParser.IdTermContext ctx);
	/**
	 * Visit a parse tree produced by {@link CstrSpecParser#setInExtension}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSetInExtension(@NotNull CstrSpecParser.SetInExtensionContext ctx);
	/**
	 * Visit a parse tree produced by {@link CstrSpecParser#trueFormula}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTrueFormula(@NotNull CstrSpecParser.TrueFormulaContext ctx);
	/**
	 * Visit a parse tree produced by {@link CstrSpecParser#termOp}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTermOp(@NotNull CstrSpecParser.TermOpContext ctx);
	/**
	 * Visit a parse tree produced by {@link CstrSpecParser#all}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAll(@NotNull CstrSpecParser.AllContext ctx);
	/**
	 * Visit a parse tree produced by {@link CstrSpecParser#intTerm}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIntTerm(@NotNull CstrSpecParser.IntTermContext ctx);
	/**
	 * Visit a parse tree produced by {@link CstrSpecParser#listInComprehension}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitListInComprehension(@NotNull CstrSpecParser.ListInComprehensionContext ctx);
	/**
	 * Visit a parse tree produced by {@link CstrSpecParser#stringTerm}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStringTerm(@NotNull CstrSpecParser.StringTermContext ctx);
	/**
	 * Visit a parse tree produced by {@link CstrSpecParser#typedef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypedef(@NotNull CstrSpecParser.TypedefContext ctx);
	/**
	 * Visit a parse tree produced by {@link CstrSpecParser#cstrCall}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCstrCall(@NotNull CstrSpecParser.CstrCallContext ctx);
	/**
	 * Visit a parse tree produced by {@link CstrSpecParser#listInExtension}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitListInExtension(@NotNull CstrSpecParser.ListInExtensionContext ctx);
	/**
	 * Visit a parse tree produced by {@link CstrSpecParser#comparison}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComparison(@NotNull CstrSpecParser.ComparisonContext ctx);
	/**
	 * Visit a parse tree produced by {@link CstrSpecParser#not}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNot(@NotNull CstrSpecParser.NotContext ctx);
	/**
	 * Visit a parse tree produced by {@link CstrSpecParser#termFunc}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTermFunc(@NotNull CstrSpecParser.TermFuncContext ctx);
	/**
	 * Visit a parse tree produced by {@link CstrSpecParser#termComparison}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTermComparison(@NotNull CstrSpecParser.TermComparisonContext ctx);
	/**
	 * Visit a parse tree produced by {@link CstrSpecParser#setTerm}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSetTerm(@NotNull CstrSpecParser.SetTermContext ctx);
	/**
	 * Visit a parse tree produced by {@link CstrSpecParser#falseFormula}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFalseFormula(@NotNull CstrSpecParser.FalseFormulaContext ctx);
	/**
	 * Visit a parse tree produced by {@link CstrSpecParser#exists}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExists(@NotNull CstrSpecParser.ExistsContext ctx);
	/**
	 * Visit a parse tree produced by {@link CstrSpecParser#listTerm}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitListTerm(@NotNull CstrSpecParser.ListTermContext ctx);
	/**
	 * Visit a parse tree produced by {@link CstrSpecParser#setInComprehension}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSetInComprehension(@NotNull CstrSpecParser.SetInComprehensionContext ctx);
}
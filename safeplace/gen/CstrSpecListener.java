// Generated from /Users/fhermeni/Research/Code/Btrplace/solver/safeplace/src/main/antlr/btrplace/solver/api/cstrSpec/CstrSpec.g4 by ANTLR 4.x
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link CstrSpecParser}.
 */
public interface CstrSpecListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link CstrSpecParser#spec}.
	 * @param ctx the parse tree
	 */
	void enterSpec(@NotNull CstrSpecParser.SpecContext ctx);
	/**
	 * Exit a parse tree produced by {@link CstrSpecParser#spec}.
	 * @param ctx the parse tree
	 */
	void exitSpec(@NotNull CstrSpecParser.SpecContext ctx);
	/**
	 * Enter a parse tree produced by {@link CstrSpecParser#formulaOp}.
	 * @param ctx the parse tree
	 */
	void enterFormulaOp(@NotNull CstrSpecParser.FormulaOpContext ctx);
	/**
	 * Exit a parse tree produced by {@link CstrSpecParser#formulaOp}.
	 * @param ctx the parse tree
	 */
	void exitFormulaOp(@NotNull CstrSpecParser.FormulaOpContext ctx);
	/**
	 * Enter a parse tree produced by {@link CstrSpecParser#call}.
	 * @param ctx the parse tree
	 */
	void enterCall(@NotNull CstrSpecParser.CallContext ctx);
	/**
	 * Exit a parse tree produced by {@link CstrSpecParser#call}.
	 * @param ctx the parse tree
	 */
	void exitCall(@NotNull CstrSpecParser.CallContext ctx);
	/**
	 * Enter a parse tree produced by {@link CstrSpecParser#arrayTerm}.
	 * @param ctx the parse tree
	 */
	void enterArrayTerm(@NotNull CstrSpecParser.ArrayTermContext ctx);
	/**
	 * Exit a parse tree produced by {@link CstrSpecParser#arrayTerm}.
	 * @param ctx the parse tree
	 */
	void exitArrayTerm(@NotNull CstrSpecParser.ArrayTermContext ctx);
	/**
	 * Enter a parse tree produced by {@link CstrSpecParser#protectedTerm}.
	 * @param ctx the parse tree
	 */
	void enterProtectedTerm(@NotNull CstrSpecParser.ProtectedTermContext ctx);
	/**
	 * Exit a parse tree produced by {@link CstrSpecParser#protectedTerm}.
	 * @param ctx the parse tree
	 */
	void exitProtectedTerm(@NotNull CstrSpecParser.ProtectedTermContext ctx);
	/**
	 * Enter a parse tree produced by {@link CstrSpecParser#constraint}.
	 * @param ctx the parse tree
	 */
	void enterConstraint(@NotNull CstrSpecParser.ConstraintContext ctx);
	/**
	 * Exit a parse tree produced by {@link CstrSpecParser#constraint}.
	 * @param ctx the parse tree
	 */
	void exitConstraint(@NotNull CstrSpecParser.ConstraintContext ctx);
	/**
	 * Enter a parse tree produced by {@link CstrSpecParser#protectedFormula}.
	 * @param ctx the parse tree
	 */
	void enterProtectedFormula(@NotNull CstrSpecParser.ProtectedFormulaContext ctx);
	/**
	 * Exit a parse tree produced by {@link CstrSpecParser#protectedFormula}.
	 * @param ctx the parse tree
	 */
	void exitProtectedFormula(@NotNull CstrSpecParser.ProtectedFormulaContext ctx);
	/**
	 * Enter a parse tree produced by {@link CstrSpecParser#idTerm}.
	 * @param ctx the parse tree
	 */
	void enterIdTerm(@NotNull CstrSpecParser.IdTermContext ctx);
	/**
	 * Exit a parse tree produced by {@link CstrSpecParser#idTerm}.
	 * @param ctx the parse tree
	 */
	void exitIdTerm(@NotNull CstrSpecParser.IdTermContext ctx);
	/**
	 * Enter a parse tree produced by {@link CstrSpecParser#setInExtension}.
	 * @param ctx the parse tree
	 */
	void enterSetInExtension(@NotNull CstrSpecParser.SetInExtensionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CstrSpecParser#setInExtension}.
	 * @param ctx the parse tree
	 */
	void exitSetInExtension(@NotNull CstrSpecParser.SetInExtensionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CstrSpecParser#trueFormula}.
	 * @param ctx the parse tree
	 */
	void enterTrueFormula(@NotNull CstrSpecParser.TrueFormulaContext ctx);
	/**
	 * Exit a parse tree produced by {@link CstrSpecParser#trueFormula}.
	 * @param ctx the parse tree
	 */
	void exitTrueFormula(@NotNull CstrSpecParser.TrueFormulaContext ctx);
	/**
	 * Enter a parse tree produced by {@link CstrSpecParser#termOp}.
	 * @param ctx the parse tree
	 */
	void enterTermOp(@NotNull CstrSpecParser.TermOpContext ctx);
	/**
	 * Exit a parse tree produced by {@link CstrSpecParser#termOp}.
	 * @param ctx the parse tree
	 */
	void exitTermOp(@NotNull CstrSpecParser.TermOpContext ctx);
	/**
	 * Enter a parse tree produced by {@link CstrSpecParser#all}.
	 * @param ctx the parse tree
	 */
	void enterAll(@NotNull CstrSpecParser.AllContext ctx);
	/**
	 * Exit a parse tree produced by {@link CstrSpecParser#all}.
	 * @param ctx the parse tree
	 */
	void exitAll(@NotNull CstrSpecParser.AllContext ctx);
	/**
	 * Enter a parse tree produced by {@link CstrSpecParser#intTerm}.
	 * @param ctx the parse tree
	 */
	void enterIntTerm(@NotNull CstrSpecParser.IntTermContext ctx);
	/**
	 * Exit a parse tree produced by {@link CstrSpecParser#intTerm}.
	 * @param ctx the parse tree
	 */
	void exitIntTerm(@NotNull CstrSpecParser.IntTermContext ctx);
	/**
	 * Enter a parse tree produced by {@link CstrSpecParser#listInComprehension}.
	 * @param ctx the parse tree
	 */
	void enterListInComprehension(@NotNull CstrSpecParser.ListInComprehensionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CstrSpecParser#listInComprehension}.
	 * @param ctx the parse tree
	 */
	void exitListInComprehension(@NotNull CstrSpecParser.ListInComprehensionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CstrSpecParser#stringTerm}.
	 * @param ctx the parse tree
	 */
	void enterStringTerm(@NotNull CstrSpecParser.StringTermContext ctx);
	/**
	 * Exit a parse tree produced by {@link CstrSpecParser#stringTerm}.
	 * @param ctx the parse tree
	 */
	void exitStringTerm(@NotNull CstrSpecParser.StringTermContext ctx);
	/**
	 * Enter a parse tree produced by {@link CstrSpecParser#typedef}.
	 * @param ctx the parse tree
	 */
	void enterTypedef(@NotNull CstrSpecParser.TypedefContext ctx);
	/**
	 * Exit a parse tree produced by {@link CstrSpecParser#typedef}.
	 * @param ctx the parse tree
	 */
	void exitTypedef(@NotNull CstrSpecParser.TypedefContext ctx);
	/**
	 * Enter a parse tree produced by {@link CstrSpecParser#cstrCall}.
	 * @param ctx the parse tree
	 */
	void enterCstrCall(@NotNull CstrSpecParser.CstrCallContext ctx);
	/**
	 * Exit a parse tree produced by {@link CstrSpecParser#cstrCall}.
	 * @param ctx the parse tree
	 */
	void exitCstrCall(@NotNull CstrSpecParser.CstrCallContext ctx);
	/**
	 * Enter a parse tree produced by {@link CstrSpecParser#listInExtension}.
	 * @param ctx the parse tree
	 */
	void enterListInExtension(@NotNull CstrSpecParser.ListInExtensionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CstrSpecParser#listInExtension}.
	 * @param ctx the parse tree
	 */
	void exitListInExtension(@NotNull CstrSpecParser.ListInExtensionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CstrSpecParser#comparison}.
	 * @param ctx the parse tree
	 */
	void enterComparison(@NotNull CstrSpecParser.ComparisonContext ctx);
	/**
	 * Exit a parse tree produced by {@link CstrSpecParser#comparison}.
	 * @param ctx the parse tree
	 */
	void exitComparison(@NotNull CstrSpecParser.ComparisonContext ctx);
	/**
	 * Enter a parse tree produced by {@link CstrSpecParser#not}.
	 * @param ctx the parse tree
	 */
	void enterNot(@NotNull CstrSpecParser.NotContext ctx);
	/**
	 * Exit a parse tree produced by {@link CstrSpecParser#not}.
	 * @param ctx the parse tree
	 */
	void exitNot(@NotNull CstrSpecParser.NotContext ctx);
	/**
	 * Enter a parse tree produced by {@link CstrSpecParser#termFunc}.
	 * @param ctx the parse tree
	 */
	void enterTermFunc(@NotNull CstrSpecParser.TermFuncContext ctx);
	/**
	 * Exit a parse tree produced by {@link CstrSpecParser#termFunc}.
	 * @param ctx the parse tree
	 */
	void exitTermFunc(@NotNull CstrSpecParser.TermFuncContext ctx);
	/**
	 * Enter a parse tree produced by {@link CstrSpecParser#termComparison}.
	 * @param ctx the parse tree
	 */
	void enterTermComparison(@NotNull CstrSpecParser.TermComparisonContext ctx);
	/**
	 * Exit a parse tree produced by {@link CstrSpecParser#termComparison}.
	 * @param ctx the parse tree
	 */
	void exitTermComparison(@NotNull CstrSpecParser.TermComparisonContext ctx);
	/**
	 * Enter a parse tree produced by {@link CstrSpecParser#setTerm}.
	 * @param ctx the parse tree
	 */
	void enterSetTerm(@NotNull CstrSpecParser.SetTermContext ctx);
	/**
	 * Exit a parse tree produced by {@link CstrSpecParser#setTerm}.
	 * @param ctx the parse tree
	 */
	void exitSetTerm(@NotNull CstrSpecParser.SetTermContext ctx);
	/**
	 * Enter a parse tree produced by {@link CstrSpecParser#falseFormula}.
	 * @param ctx the parse tree
	 */
	void enterFalseFormula(@NotNull CstrSpecParser.FalseFormulaContext ctx);
	/**
	 * Exit a parse tree produced by {@link CstrSpecParser#falseFormula}.
	 * @param ctx the parse tree
	 */
	void exitFalseFormula(@NotNull CstrSpecParser.FalseFormulaContext ctx);
	/**
	 * Enter a parse tree produced by {@link CstrSpecParser#exists}.
	 * @param ctx the parse tree
	 */
	void enterExists(@NotNull CstrSpecParser.ExistsContext ctx);
	/**
	 * Exit a parse tree produced by {@link CstrSpecParser#exists}.
	 * @param ctx the parse tree
	 */
	void exitExists(@NotNull CstrSpecParser.ExistsContext ctx);
	/**
	 * Enter a parse tree produced by {@link CstrSpecParser#listTerm}.
	 * @param ctx the parse tree
	 */
	void enterListTerm(@NotNull CstrSpecParser.ListTermContext ctx);
	/**
	 * Exit a parse tree produced by {@link CstrSpecParser#listTerm}.
	 * @param ctx the parse tree
	 */
	void exitListTerm(@NotNull CstrSpecParser.ListTermContext ctx);
	/**
	 * Enter a parse tree produced by {@link CstrSpecParser#setInComprehension}.
	 * @param ctx the parse tree
	 */
	void enterSetInComprehension(@NotNull CstrSpecParser.SetInComprehensionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CstrSpecParser#setInComprehension}.
	 * @param ctx the parse tree
	 */
	void exitSetInComprehension(@NotNull CstrSpecParser.SetInComprehensionContext ctx);
}
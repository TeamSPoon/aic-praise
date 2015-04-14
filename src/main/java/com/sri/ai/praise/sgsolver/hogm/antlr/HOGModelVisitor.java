/*
 * Copyright (c) 2015, SRI International
 * All rights reserved.
 * Licensed under the The BSD 3-Clause License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 * 
 * http://opensource.org/licenses/BSD-3-Clause
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 
 * Neither the name of the aic-praise nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) 
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.sri.ai.praise.sgsolver.hogm.antlr;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.NotNull;
import org.apache.commons.lang3.StringEscapeUtils;

import com.google.common.annotations.Beta;
import com.sri.ai.expresso.api.Expression;
import com.sri.ai.expresso.core.ExtensionalIndexExpressionsSet;
import com.sri.ai.expresso.helper.Expressions;
import com.sri.ai.grinder.library.FunctorConstants;
import com.sri.ai.grinder.library.boole.And;
import com.sri.ai.grinder.library.boole.ForAll;
import com.sri.ai.grinder.library.boole.Not;
import com.sri.ai.grinder.library.boole.Or;
import com.sri.ai.grinder.library.boole.ThereExists;
import com.sri.ai.grinder.library.controlflow.IfThenElse;
import com.sri.ai.grinder.library.number.Minus;
import com.sri.ai.grinder.library.set.extensional.ExtensionalSet;
import com.sri.ai.grinder.library.set.tuple.Tuple;
import com.sri.ai.praise.model.RandomVariableDeclaration;
import com.sri.ai.praise.model.SortDeclaration;
import com.sri.ai.praise.sgsolver.model.HOGModel;
import com.sri.ai.praise.sgsolver.model.StatementInfo;

@Beta
public class HOGModelVisitor extends HOGMBaseVisitor<Expression> {
	// Note track bracketed expressions based on identity to ensure no accidental overwrite by value.
	private Map<Expression, Expression> parenthesizedExpressions = new IdentityHashMap<Expression, Expression>(); 
	//
	private List<StatementInfo> sortDeclarations           = new ArrayList<>();
	private List<StatementInfo> randomVariableDeclarations = new ArrayList<>();
	private List<StatementInfo> terms                      = new ArrayList<>();

	// model : statements+=statement* EOF
	@Override 
	public Expression visitModel(@NotNull HOGMParser.ModelContext ctx) { 

		sortDeclarations.clear();
		randomVariableDeclarations.clear();
		terms.clear();
		
		ctx.statements.forEach(s -> visit(s));
		
		Expression result = HOGModel.validateAndConstruct(sortDeclarations, randomVariableDeclarations, terms);

		return result;
	}
	
	// aterm : term EOF
	@Override 
	public Expression visitAterm(@NotNull HOGMParser.AtermContext ctx) { 
		Expression result = visit(ctx.term());	
		return result;
	}
	
	@Override 
	public Expression visitStatement(@NotNull HOGMParser.StatementContext ctx) { 
		Expression result;
		
		if (ctx.declaration() != null) {
			result = visit(ctx.declaration());
		}
		else {
			result = visit(ctx.term());
			terms.add(newStatementInfo(result, ctx));
		}
		
		return result;
	}
	
	// sort_decl : SORT name=sort_name (COLON size=(INTEGER | UNKNOWN) (COMMA constants+=constant_name)*)? (SEMICOLON)?
	@Override 
	public Expression visitSort_decl(@NotNull HOGMParser.Sort_declContext ctx) { 
		Expression name = newSymbol(ctx.name.getText());
		Expression size = SortDeclaration.UNKNOWN_SIZE;
		if (ctx.size != null) {
			size = newSymbol(ctx.size.getText());
		}
		List<Expression> constants = new ArrayList<Expression>();
		if (ctx.constants != null) {
			ctx.constants.forEach(c -> constants.add(newSymbol(c.getText())));
		}
		
		Expression result = Expressions.makeExpressionOnSyntaxTreeWithLabelAndSubTrees(
				SortDeclaration.FUNCTOR_SORT_DECLARATION, name, size,
				ExtensionalSet.makeUniSet(constants));
		
		sortDeclarations.add(newStatementInfo(result, ctx));
		
		return result;
	}
	
	// random_variable_decl 
    // : RANDOM name=constant_name COLON range=sort_name (SEMICOLON)?
	@Override 
	public Expression visitPropositionalRandomVariableDeclaration(@NotNull HOGMParser.PropositionalRandomVariableDeclarationContext ctx) { 
		Expression name  = newSymbol(ctx.name.getText());
		Expression arity = Expressions.ZERO;
		Expression range = newSymbol(ctx.range.getText());

		List<Expression> declarationArgs = new ArrayList<Expression>();
		declarationArgs.add(name);
		declarationArgs.add(arity);
		declarationArgs.add(range);

		Expression result = Expressions.makeExpressionOnSyntaxTreeWithLabelAndSubTrees(
				RandomVariableDeclaration.FUNCTOR_RANDOM_VARIABLE_DECLARATION,
				declarationArgs.toArray());
		
		randomVariableDeclarations.add(newStatementInfo(result, ctx));

		return result; 
	}

	// random_variable_decl 
    // | RANDOM name=constant_name COLON parameters+=sort_name (X parameters+=sort_name)* MAPPING_RIGHT_ARROW range=sort_name (SEMICOLON)?
	@Override 
	public Expression visitRelationalRandomVariableDeclaration(@NotNull HOGMParser.RelationalRandomVariableDeclarationContext ctx) { 
		Expression name = newSymbol(ctx.name.getText());
		List<Expression> parameters = expressionsList(ctx.parameters);
		Expression arity = Expressions.makeSymbol(parameters.size());
		Expression range = newSymbol(ctx.range.getText());

		List<Expression> declarationArgs = new ArrayList<Expression>();
		declarationArgs.add(name);
		declarationArgs.add(arity);
		declarationArgs.addAll(parameters);
		declarationArgs.add(range);

		Expression result = Expressions.makeExpressionOnSyntaxTreeWithLabelAndSubTrees(
				RandomVariableDeclaration.FUNCTOR_RANDOM_VARIABLE_DECLARATION,
				declarationArgs.toArray());
		
		randomVariableDeclarations.add(newStatementInfo(result, ctx));

		return result;
	}
	
	// term
	// : OPEN_PAREN term CLOSE_PAREN
	@Override 
	public Expression visitParentheses(@NotNull HOGMParser.ParenthesesContext ctx) { 
		Expression result = visit(ctx.term());
		
		// Keep track of explicitly bracketed expressions
		// so that the are not flattened as part of the 
		// possiblyFlatten()
		// call for some expressions, e.g.: 1 + 2 + 3.
		parenthesizedExpressions.put(result, result);
		
		return result;
	}
	
	// term
	// | function_application
	// function_application
	// : functor=functor_name OPEN_PAREN ( args+=term (COMMA args+=term)* )? CLOSE_PAREN
 	@Override 
 	public Expression visitFunction_application(@NotNull HOGMParser.Function_applicationContext ctx) {
 		Expression result = Expressions.makeExpressionOnSyntaxTreeWithLabelAndSubTrees(visit(ctx.functor), expressions(ctx.args));
		
		return result; 
	}
 	
 	// term
 	// | NOT term
 	public Expression visitNot(@NotNull HOGMParser.NotContext ctx) { 
 		Expression result = Expressions.makeExpressionOnSyntaxTreeWithLabelAndSubTrees(Not.FUNCTOR, visit(ctx.term()));
		return result; 
 	}
 	
 	// term
 	// | SUBTRACT term
 	@Override 
 	public Expression visitUnaryMinus(@NotNull HOGMParser.UnaryMinusContext ctx) { 
		Expression argument = visit(ctx.term());
		Expression result;
		if (argument.getValue() instanceof Number) {
			result = Expressions.makeSymbol(argument.rationalValue().negate());
		}
		else {
			result = Expressions.makeExpressionOnSyntaxTreeWithLabelAndSubTrees(FunctorConstants.MINUS, argument);
		}
		return result;
 	}
 	
 	// term
 	// |  <assoc=right> base=term EXPONENTIATION exponent=term
 	@Override 
 	public Expression visitExponentiation(@NotNull HOGMParser.ExponentiationContext ctx) { 
 		Expression result = Expressions.makeExpressionOnSyntaxTreeWithLabelAndSubTrees(FunctorConstants.EXPONENTIATION, visit(ctx.base), visit(ctx.exponent));
		return result;
 	}
 	
 	// term
 	// | leftop=term op=(TIMES | DIVIDE) rightop=term
 	@Override 
 	public Expression visitMultiplicationOrDivision(@NotNull HOGMParser.MultiplicationOrDivisionContext ctx) { 
		Expression result = Expressions.makeExpressionOnSyntaxTreeWithLabelAndSubTrees(ctx.op.getText(), visit(ctx.leftop), visit(ctx.rightop));
		result = possiblyFlatten(result);
		return result; 
 	}
 	
 	// term
 	// | leftop=term op=(PLUS | SUBTRACT) rightop=term
 	@Override 
 	public Expression visitAdditionOrSubtraction(@NotNull HOGMParser.AdditionOrSubtractionContext ctx) { 
		Expression result = Expressions.makeExpressionOnSyntaxTreeWithLabelAndSubTrees(ctx.op.getText(), visit(ctx.leftop), visit(ctx.rightop));
		result = possiblyFlatten(result);
		return result; 
 	}
 	
 	// term
 	// | leftop=term op=(LESS_THAN | LESS_THAN_EQUAL | EQUAL | NOT_EQUAL | GREATER_THAN_EQUAL | GREATER_THAN) rightop=term
 	@Override 
 	public Expression visitComparison(@NotNull HOGMParser.ComparisonContext ctx) { 
		Expression result = Expressions.makeExpressionOnSyntaxTreeWithLabelAndSubTrees(ctx.op.getText(), visit(ctx.leftop), visit(ctx.rightop));
		result = possiblyFlatten(result);
		return result; 
 	}
 	
 	// term
 	// | leftconj=term AND rightconj=term
 	@Override 
 	public Expression visitConjunction(@NotNull HOGMParser.ConjunctionContext ctx) { 
		Expression result = Expressions.makeExpressionOnSyntaxTreeWithLabelAndSubTrees(And.FUNCTOR, visit(ctx.leftconj), visit(ctx.rightconj));
		result = possiblyFlatten(result);
		return result; 
 	}
 	
 	// term
 	// | leftdisj=term OR rightdisj=term
 	@Override 
 	public Expression visitDisjunction(@NotNull HOGMParser.DisjunctionContext ctx) { 
 		Expression result = Expressions.makeExpressionOnSyntaxTreeWithLabelAndSubTrees(Or.FUNCTOR, visit(ctx.leftdisj), visit(ctx.rightdisj));
		result = possiblyFlatten(result);
		return result; 
 	}
 	
 	// term
 	// |<assoc=right> antecedent=term IMPLICATION consequent=term
 	@Override 
 	public Expression visitImplication(@NotNull HOGMParser.ImplicationContext ctx) { 
		Expression result = Expressions.makeExpressionOnSyntaxTreeWithLabelAndSubTrees(FunctorConstants.IMPLICATION, visit(ctx.antecedent), visit(ctx.consequent));
		return result; 
 	}
 	
 	// term
 	// |<assoc=right> leftop=term BICONDITIONAL rightop=term
 	@Override 
 	public Expression visitBiconditional(@NotNull HOGMParser.BiconditionalContext ctx) { 
		Expression result = Expressions.makeExpressionOnSyntaxTreeWithLabelAndSubTrees(FunctorConstants.EQUIVALENCE, visit(ctx.leftop), visit(ctx.rightop));
		return result; 
 	}
 	
 	// term
 	// | FOR ALL index=quantifier_index COLON body=term
 	@Override 
 	public Expression visitForAll(@NotNull HOGMParser.ForAllContext ctx) { 
 		Expression result = ForAll.make(new ExtensionalIndexExpressionsSet(Tuple.getElements(visit(ctx.index))), visit(ctx.body));
		return result; 
 	}
 	
 	// term
 	// | THERE EXISTS index=quantifier_index COLON body=term
 	@Override 
 	public Expression visitThereExists(@NotNull HOGMParser.ThereExistsContext ctx) { 
 		Expression result = ThereExists.make(new ExtensionalIndexExpressionsSet(Tuple.getElements(visit(ctx.index))), visit(ctx.body));
		return result; 
 	}
 	
 	// term
 	// | term term
 	@Override 
 	public Expression visitShorthandConditionedPotential(@NotNull HOGMParser.ShorthandConditionedPotentialContext ctx) { 
 		Expression condition = visit(ctx.term(0));
 		Expression potential = visit(ctx.term(1));
 		
 		Expression result = IfThenElse.make(condition, potential, Minus.make(Expressions.ONE, potential));
 		return result;
 	}
 	
 	// term
 	// | IF condition=term THEN thenbranch=term ELSE elsebranch=term
 	@Override 
 	public Expression visitConditional(@NotNull HOGMParser.ConditionalContext ctx) { 
 		Expression result = IfThenElse.make(visit(ctx.condition), visit(ctx.thenbranch), visit(ctx.elsebranch));
 		return result;
 	}
 	
 	// term
 	// | IF condition=term THEN thenbranch=term
 	@Override 
 	public Expression visitConditionalUnknownElseBranch(@NotNull HOGMParser.ConditionalUnknownElseBranchContext ctx) { 
 		Expression result = IfThenElse.make(visit(ctx.condition), visit(ctx.thenbranch), Expressions.ZERO_POINT_FIVE);
 		return result;
 	}
 	
 	// quantifier_index_term
    // : function_application #quantifierIndexTermFunctionApplication
    // | VARIABLE #quantifierIndexTermVariable
    // | variable=VARIABLE IN sort=sort_name #quantifierIndexTermVariableInSort
 	@Override 
 	public Expression visitQuantifier_index(@NotNull HOGMParser.Quantifier_indexContext ctx) { 
 		Expression result = Tuple.make(expressions(ctx.indexes));
 		return result;
 	}
 	
 	// | VARIABLE #quantifierIndexTermVariable
 	@Override 
 	public Expression visitQuantifierIndexTermVariable(@NotNull HOGMParser.QuantifierIndexTermVariableContext ctx) { 
 		Expression result = newSymbol(ctx.VARIABLE().getText());
 		return result;
 	}
 	
 	// | variable=VARIABLE IN sort=sort_name #quantifierIndexTermVariableInSort
 	@Override 
 	public Expression visitQuantifierIndexTermVariableInSort(@NotNull HOGMParser.QuantifierIndexTermVariableInSortContext ctx) {
 		Expression variable = newSymbol(ctx.variable.getText());
 		Expression sortName = newSymbol(ctx.variable.getText());
 		
 		Expression result = Expressions.makeExpressionOnSyntaxTreeWithLabelAndSubTrees(FunctorConstants.IN, variable, sortName);
 		
 		return result; 
 	}

 	// sort_name
    // : VARIABLE
 	@Override 
 	public Expression visitSort_name(@NotNull HOGMParser.Sort_nameContext ctx) { 
 		Expression result = newSymbol(ctx.getText());
 		return result;
 	}
 	
 	// functor_name
    // : VARIABLE
    // | constant_name
 	@Override 
 	public Expression visitFunctor_name(@NotNull HOGMParser.Functor_nameContext ctx) { 
 		Expression result = newSymbol(ctx.getText());
 		return result;
 	}
 	
 	// symbol
 	// : VARIABLE
    // | constant_name
    // | constant_number
 	@Override 
 	public Expression visitSymbol(@NotNull HOGMParser.SymbolContext ctx) { 
 		Expression result = newSymbol(ctx.getText());
 		return result;
 	}
 	
 	// constant_name
    // : X
    // | CONSTANT
    // | QUOTED_CONSTANT
 	@Override 
 	public Expression visitConstant_name(@NotNull HOGMParser.Constant_nameContext ctx) { 
 		Expression result = newSymbol(ctx.getText());
 		return result;
 	}
 	
 	// constant_number
    // : INTEGER
    // | RATIONAL
 	@Override 
 	public Expression visitConstant_number(@NotNull HOGMParser.Constant_numberContext ctx) { 
 		Expression result = newSymbol(ctx.getText());
 		return result; 
 	}
 	
	//
	// PROTECTED
	//
 	protected StatementInfo newStatementInfo(Expression statement, ParserRuleContext ctx) {
 		StatementInfo result = new StatementInfo(statement, ctx.getText(), ctx.getStart().getLine(), ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex());
 		return result;
 	}
 	
	protected Expression newSymbol(String text) {
		// Remove quotes from around quoted strings
		if ((text.startsWith("'") && text.endsWith("'"))
				|| (text.startsWith("\"") && text.endsWith("\""))) {
			text = text.substring(1, text.length() - 1);
		}

		// Ensure escapes are applied.
		text = StringEscapeUtils.unescapeJava(text);

		text = new String(text);

		Expression result = Expressions.makeSymbol(text);
		return result;
	}
	
	protected Object[] expressions(List<? extends ParserRuleContext> exprContexts) {
		Object[] result = expressionsList(exprContexts).toArray();
		return result;
	}
	
	protected List<Expression> expressionsList(List<? extends ParserRuleContext> exprContexts) {
		List<Expression> result = new ArrayList<Expression>();
		exprContexts.forEach(exprContext -> result.add(visit(exprContext)));
		return result;
	}
	
	protected Expression possiblyFlatten(Expression expression) {
		Expression result = expression;
		
		Object functor = expression.getFunctor();
		if (functor != null) {
			if (functor.equals(FunctorConstants.TIMES) || 
			    functor.equals(FunctorConstants.PLUS)  || 
			    functor.equals(FunctorConstants.EQUAL) ||
			    functor.equals(FunctorConstants.AND)   || 
			    functor.equals(FunctorConstants.OR)) {
				List<Expression> args = new ArrayList<Expression>();
				for (Expression arg : expression.getArguments()) {
					if (arg.getFunctor() != null && functor.equals(arg.getFunctor()) && !parenthesizedExpressions.containsKey(arg)) {
						args.addAll(arg.getArguments());
					}
					else {
						args.add(arg);
					}
				}
				result = Expressions.makeExpressionOnSyntaxTreeWithLabelAndSubTrees(functor, args.toArray());
			}
		}
		
		// Clear in order manage memory
		parenthesizedExpressions.clear();
		
		return result;
	}
}

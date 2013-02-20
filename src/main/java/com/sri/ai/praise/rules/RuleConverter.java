/*
 * Copyright (c) 2013, SRI International
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
package com.sri.ai.praise.rules;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.annotations.Beta;
import com.sri.ai.expresso.api.Expression;
import com.sri.ai.expresso.api.ReplacementFunctionWithContextuallyUpdatedProcess;
import com.sri.ai.expresso.api.Symbol;
import com.sri.ai.expresso.core.DefaultCompoundSyntaxTree;
import com.sri.ai.expresso.core.DefaultSymbol;
import com.sri.ai.expresso.helper.Expressions;
import com.sri.ai.grinder.api.RewritingProcess;
import com.sri.ai.grinder.helper.GrinderUtil;
import com.sri.ai.grinder.library.Disequality;
import com.sri.ai.grinder.library.FunctorConstants;
import com.sri.ai.grinder.library.Variables;
import com.sri.ai.grinder.library.boole.And;
import com.sri.ai.grinder.library.boole.Not;
import com.sri.ai.grinder.library.controlflow.IfThenElse;
import com.sri.ai.grinder.library.set.intensional.IntensionalSet;
import com.sri.ai.praise.LPIUtil;
import com.sri.ai.praise.rules.antlr.RuleParserWrapper;
import com.sri.ai.praise.lbp.LBPFactory;
import com.sri.ai.praise.lbp.LBPRewriter;
import com.sri.ai.praise.model.Model;
import com.sri.ai.praise.model.RandomVariableDeclaration;
import com.sri.ai.praise.model.SortDeclaration;
import com.sri.ai.util.base.Pair;

@Beta
public class RuleConverter {

	public static final String FUNCTOR_IF_THEN_ELSE                = IfThenElse.FUNCTOR;
	public static final String FUNCTOR_FOR_ALL                     = FunctorConstants.FOR_ALL;
	public static final String FUNCTOR_THERE_EXISTS                = FunctorConstants.THERE_EXISTS;

	public static final String FUNCTOR_RANDOM_VARIABLE_DECLARATION = RandomVariableDeclaration.FUNCTOR_RANDOM_VARIABLE_DECLARATION;
	public static final String FUNCTOR_SORT                        = SortDeclaration.FUNCTOR_SORT_DECLARATION;

	public static final String FUNCTOR_ATOMIC_RULE                 = "atomic rule";
	public static final String FUNCTOR_CONDITIONAL_RULE            = "conditional rule";
	public static final String FUNCTOR_PROLOG_RULE                 = "prolog rule";

	public static final String FUNCTOR_MAY_BE_SAME_AS              = "may be same as";


	private RuleParserWrapper         ruleParser       = null;
//	private AntlrGrinderParserWrapper grinderParser    = new AntlrGrinderParserWrapper();
	private RewritingProcess          rewritingProcess = null;

	private ReplaceConstraintWithConstant positiveEmbeddedConstraintReplacementFunction = new ReplaceConstraintWithConstant(Expressions.TRUE);
	private ReplaceConstraintWithConstant negativeEmbeddedConstraintReplacementFunction = new ReplaceConstraintWithConstant(Expressions.FALSE);


//	public class RulesConversionProcess {
//		public List<Expression>                   parfactors;
//		public List<Expression>                   sorts;
//		public List<Expression>                   randomVariables;
//
//		public Map<String, Set<Integer>>          randomVariableIndex;
//	}

	/**
	 * Replacement function for use by function translator.
	 * @author etsai
	 *
	 */
	private class ReplaceFunctionFunction implements ReplacementFunctionWithContextuallyUpdatedProcess {
		public Map<String, Set<Integer>> randomVariableIndex;
		public Map<String, Set<Integer>> functionsFound;
		public Expression                currentExpression;
		public int                       uniqueCount = 0;

		public ReplaceFunctionFunction(Expression currentExpression,
				Map<String, Set<Integer>> randomVariableIndex, 
				Map<String, Set<Integer>> functionsFound) {
			this.currentExpression = currentExpression;
			this.randomVariableIndex = randomVariableIndex;
			this.functionsFound = functionsFound;
		}

		@Override
		public Expression apply(Expression expression) {
			throw new UnsupportedOperationException("evaluate(Object expression) should not be called.");
		}

		@Override
		public Expression apply(Expression expression, RewritingProcess process) {
			if (expression.getArguments().size() > 0) {
//				System.out.println("inspectNode: " + expression);
				if (expression.getFunctor().equals(FunctorConstants.EQUAL) || expression.getFunctor().equals(FunctorConstants.INEQUALITY) ||
						isRandomFunctionApplication(expression)) {
					boolean isReplace = false;
					List<Expression> arguments = new ArrayList<Expression>(); 
					List<Expression> andArgs   = new ArrayList<Expression>();
					for (Expression argument : expression.getArguments()) {
						if (isRandomVariableValue(argument, randomVariableIndex)) {
							isReplace = true;
							Expression unique = Expressions.makeUniqueVariable(
									"X" + uniqueCount, currentExpression, 
									rewritingProcess);
//							System.out.println("Incrementing unique count: " + converterContext.uniqueCount + ",  " + parent + " | " + argument);
							uniqueCount++;
							arguments.add(unique);
							List<Expression> newArgumentArgs = new ArrayList<Expression>(argument.getArguments());
							newArgumentArgs.add(unique);

							String name;
							if (argument.getArguments().size() == 0)
								name = argument.toString();
							else
								name = argument.getFunctor().toString();
							andArgs.add(Expressions.make(name, newArgumentArgs));

							// Note the function name and param count so can add additional rule enforcing functional
							// relation later.
							Set<Integer> paramCount;
							paramCount = functionsFound.get(name);
							if (paramCount == null) {
								paramCount = new HashSet<Integer>();
								functionsFound.put(name, paramCount);
							}
							paramCount.add(argument.getArguments().size());
						}
						else {
							arguments.add(argument);
						}
					}
					if(isReplace) {
//						System.out.println("arguments: " + arguments);
//						System.out.println("andArgs:   " + andArgs);
						andArgs.add(Expressions.make(expression.getFunctor(), arguments));
						Expression replacement = And.make(andArgs);
//						System.out.println("Replace <" + parent + ">  with <" + replacement + ">");
						return replacement;
					}

				}
			}
			return expression;
		}
	}

	/**
	 * Replacement function for use by quantifier translator.
	 * @author etsai
	 *
	 */
	private class ReplaceQuantifierFunction implements ReplacementFunctionWithContextuallyUpdatedProcess {
		public List<Expression> result;

		public ReplaceQuantifierFunction(List<Expression> result) {
			this.result = result;
		}

		@Override
		public Expression apply(Expression expression) {
			throw new UnsupportedOperationException("evaluate(Object expression) should not be called.");
		}

		@Override
		public Expression apply(Expression expression, RewritingProcess process) {
			if (expression.getArguments().size() > 0) {
//				System.out.println("inspectNode: " + parent);
					if (expression.getFunctor().equals(FunctorConstants.FOR_ALL) || 
							expression.getFunctor().equals(FunctorConstants.THERE_EXISTS)) {
						Symbol newFunctor = DefaultSymbol.createSymbol(expression.toString());
						Set<Expression> variables = Variables.freeVariables(expression, rewritingProcess);
						Expression newExpression = Expressions.make(newFunctor, variables);
//						System.out.println("Generated expression: " + newExpression);
						if (expression.getFunctor().equals(FunctorConstants.THERE_EXISTS)) {
							result.add(translateConditionalRule(
									Expressions.make(RuleConverter.FUNCTOR_CONDITIONAL_RULE, expression.getArguments().get(0), 
											Expressions.make(RuleConverter.FUNCTOR_ATOMIC_RULE, newExpression, 1))));
						}
						else {
							result.add(translateConditionalRule(
									Expressions.make(RuleConverter.FUNCTOR_CONDITIONAL_RULE, 
											Not.make(expression.getArguments().get(0)), 
											Expressions.make(RuleConverter.FUNCTOR_ATOMIC_RULE, 
													Not.make(newExpression), 1))));
						}
//						System.out.println("Replacing: " + parent + " with " + newExpression);
//						System.out.println(converterContext.currentExpression);
						return newExpression;
					}
				}
				return expression;
		}
	}

	/**
	 * Replacement function for use by "may be same as" extractor.
	 * @author etsai
	 *
	 */
	private class ReplaceMayBeSameAsFunction implements ReplacementFunctionWithContextuallyUpdatedProcess {
		public Set<Pair<Expression, Expression>>  mayBeSameAsSet;

		public ReplaceMayBeSameAsFunction(Set<Pair<Expression, Expression>> mayBeSameAsSet) {
			this.mayBeSameAsSet = mayBeSameAsSet;
		}

		@Override
		public Expression apply(Expression expression) {
			throw new UnsupportedOperationException("evaluate(Object expression) should not be called.");
		}

		@Override
		public Expression apply(Expression expression, RewritingProcess process) {
			if (expression.getArguments().size() > 0) {
				if (expression.getFunctor().equals(RuleConverter.FUNCTOR_MAY_BE_SAME_AS)) {
//					System.out.println("Found 'may be same as'");

					// Add both variants of the pair.
					mayBeSameAsSet.add(
							new Pair<Expression, Expression>(
									expression.getArguments().get(0), expression.getArguments().get(1)));
					mayBeSameAsSet.add(
							new Pair<Expression, Expression>(
									expression.getArguments().get(1), expression.getArguments().get(0)));
//					System.out.println(converterContext.mayBeSameAsSet);

					// Replace the "may be same as" expressions with true.
					System.out.println("Replace with true.");
					return Expressions.TRUE;
				}
			}
			return expression;
		}
	}

	/**
	 * Replacement function for use by embedded constraint extractor.
	 * @author etsai
	 *
	 */
	private class ReplaceConstraintWithConstant implements ReplacementFunctionWithContextuallyUpdatedProcess {
		private Expression constant;

		public Expression constraint;

		public ReplaceConstraintWithConstant(Expression constant) {
			this.constant = constant;
		}

		@Override
		public Expression apply(Expression expression) {
			throw new UnsupportedOperationException("evaluate(Object expression) should not be called.");
		}

		@Override
		public Expression apply(Expression expression, RewritingProcess process) {
			if (LPIUtil.isConstraint(expression, process)) {
//				System.out.println("Found constraint: " + expression);
				constraint = expression;
				return constant;
			}
			return expression;
		}
	}



	/*===================================================================================
	 * PUBLIC METHODS
	 *=================================================================================*/
	
	public RuleConverter() {
		// Ensure these are instantiated straight away and not when first referenced.
		// This helps ensure any global dependencies are setup correctly.
		rewritingProcess = LBPFactory.newLBPProcess(DefaultSymbol.createSymbol("true"));
		ruleParser       = new RuleParserWrapper();
	}
	
	public Model parseModel (String modelString) {
		return parseModel("", "", modelString);
	}

	public Model parseModel (String name, String description, String modelString) {
		return parseModel(name, description, ruleParser.parseAll(modelString));
	}
	
	public Model parseModel (List<Expression> inputRules) {
		return parseModel("", "", inputRules);
	}

	public Model parseModel (String name, String description, List<Expression> inputRules) {
//		RulesConversionProcess context = new RulesConversionProcess();
		List<Expression> potentialExpressions         = new ArrayList<Expression>();
		List<Expression> sorts                        = new ArrayList<Expression>();
		List<Expression> randomVariables              = new ArrayList<Expression>();
		Map<String, Set<Integer>> randomVariableIndex = new HashMap<String, Set<Integer>>();
		
		// Sort and convert the rules to their if-then-else forms.
		for (Expression rule : inputRules) {
			if (rule.getFunctor().equals(FUNCTOR_ATOMIC_RULE)) {
				potentialExpressions.add(translateAtomicRule(rule));
			}
			else if (rule.getFunctor().equals(FUNCTOR_PROLOG_RULE)) {
				potentialExpressions.add(translatePrologRule(rule));
			}
			else if (rule.getFunctor().equals(FUNCTOR_CONDITIONAL_RULE)) {
				potentialExpressions.add(translateConditionalRule(rule));
			}
			else if (rule.getFunctor().equals(FUNCTOR_RANDOM_VARIABLE_DECLARATION)) {
				randomVariables.add(this.updateRandomVariableDeclaration(rule));
				String varName = rule.get(0).toString();
				if (randomVariableIndex.get(varName) == null) {
					Set<Integer> set = new HashSet<Integer>();
					set.add(rule.get(1).intValue());
					randomVariableIndex.put(varName, set);
				}
				else {
					randomVariableIndex.get(varName).add(rule.get(1).intValue());
				}
//				randomVariableNames.put(rule.get(0).toString(), rule);
			}
			else if (rule.getFunctor().equals(FUNCTOR_SORT)) {
				sorts.add(rule);
			}
		}
//		System.out.println("var names: " + randomVariableNames.toString());
//		System.out.println("var index: " + context.randomVariableIndex.toString());
//		System.out.println("parfactors: " + context.parfactors.toString());
//		System.out.println("random variables: " + context.randomVariables.toString());
//		System.out.println("sorts: " + context.sorts.toString());

		// Translate the functions.
		potentialExpressions = translateFunctions(potentialExpressions, randomVariableIndex);

		// Translate the quantifiers.
		potentialExpressions = translateQuantifiers(potentialExpressions);

		// Extract the embedded constraints.
		List<Pair<Expression, Expression>> potentialExpressionAndConstraintList = 
				disembedConstraints(potentialExpressions);

		// Translate the potential expression/constraint pair into a parfactor.
		potentialExpressions = new ArrayList<Expression>();
		for (Pair<Expression, Expression> pair : potentialExpressionAndConstraintList) {
			potentialExpressions.add(createParfactor(pair.first, pair.second));
		}
		
		// Create the model object output.
		ArrayList<Expression> modelArgs = new ArrayList<Expression>();
		modelArgs.add(DefaultSymbol.createSymbol(name));
		modelArgs.add(DefaultSymbol.createSymbol(description));
		for (Expression sort : sorts)
			modelArgs.add(sort);

		Set<String> randomVariableNames = new HashSet<String>();
		for (Expression randomVariable : randomVariables) {
			modelArgs.add(randomVariable);
			randomVariableNames.add(randomVariable.get(0).toString());
		}
		modelArgs.add(Expressions.apply("parfactors", potentialExpressions));
		Expression modelExpression = Expressions.apply("model", modelArgs);

		return new Model(modelExpression, randomVariableNames);
	}
	



	public Set<Expression> translateRules (List<Expression> rules) {
		Set<Expression> result = new HashSet<Expression>();
		for (Expression rule : rules) {
			result.add(translateRule(rule));
		}
		return result;
	}

	public Expression translateRule (Expression rule) {
		if (rule.getFunctor().equals(FUNCTOR_ATOMIC_RULE)) {
			return translateAtomicRule(rule);
		}
		else if (rule.getFunctor().equals(FUNCTOR_PROLOG_RULE)) {
			return translatePrologRule(rule);
		}
		else if (rule.getFunctor().equals(FUNCTOR_CONDITIONAL_RULE)) {
			return translateConditionalRule(rule);
		}
		return rule;
	}
	
	public Expression translateAtomicRule (Expression rule) {
		List<Expression> args = rule.getArguments();
		if (args.size() != 2)
			return null;

		return new DefaultCompoundSyntaxTree(FUNCTOR_IF_THEN_ELSE, args.get(0), 
				args.get(1), oneMinusPotential(args.get(1)));
	}
	
	public Expression translatePrologRule (Expression rule) {
		List<Expression> args = rule.getArguments();
		
		if (args.size() == 2) {
			return new DefaultCompoundSyntaxTree(FUNCTOR_IF_THEN_ELSE, args.get(1), 
					args.get(0), oneMinusPotential(args.get(0)));
		}
		else if (args.size() == 3){
			return new DefaultCompoundSyntaxTree(FUNCTOR_IF_THEN_ELSE, args.get(2), 
					new DefaultCompoundSyntaxTree(FUNCTOR_IF_THEN_ELSE, args.get(1), 
							args.get(0), oneMinusPotential(args.get(0))),
					0.5);
		}

		return null;
	}
	
	public Expression translateConditionalRule (Expression rule) {
		List<Expression> args = rule.getArguments();
		if (args.size() == 2) {
			return new DefaultCompoundSyntaxTree(FUNCTOR_IF_THEN_ELSE, args.get(0), 
					this.translateRule(args.get(1)),
					0.5);
		}
		else if (args.size() == 3) {
			return new DefaultCompoundSyntaxTree(FUNCTOR_IF_THEN_ELSE, args.get(0), 
					this.translateRule(args.get(1)),
					this.translateRule(args.get(2)));
		}
		return null;
	}


	public List<Expression> translateFunctions (List<Expression> potentialExpressions, 
			Map<String, Set<Integer>> randomVariableIndex) {
		List<Expression> result = new ArrayList<Expression>();
		Map<String, Set<Integer>> functionsFound = new HashMap<String, Set<Integer>>();

		for (Expression parfactor : potentialExpressions) {
			Expression toReplace = parfactor;
			Expression replaced  = parfactor;

			ReplaceFunctionFunction replacementFunction = 
					new ReplaceFunctionFunction(toReplace, randomVariableIndex, functionsFound);
			do {
			    toReplace = replaced;
			    replaced = toReplace.replaceAllOccurrences(
			    		replacementFunction,
			    		rewritingProcess);
			           
			    // Update the reference for generating unique constant names.
			    replacementFunction.currentExpression = replaced;
			} while (replaced != toReplace);

			// Save the result.
	        result.add(replaced);
		}

//		System.out.println("Functions found: " + context.functionsFound);
		for (String functor : functionsFound.keySet()) {
			Set<Integer> counts = functionsFound.get(functor);
			for (Integer count : counts) {
				this.createTransformedFunctionConstraints(functor, count, result);
			}
		}

		return result;
	}

	public boolean isRandomFunctionApplication (Expression e) {
		if (!e.getSyntacticFormType().equals("Function application")) {
//			System.out.println(e + " is not function application");
			return false;
		}

		Expression functor = e.getFunctor();
		if (functor.equals(FunctorConstants.AND) || functor.equals(FunctorConstants.OR) || 
				functor.equals(FunctorConstants.NOT) || functor.equals(FunctorConstants.EQUIVALENCE) ||
				functor.equals(FunctorConstants.IMPLICATION) || functor.equals(FUNCTOR_IF_THEN_ELSE) ||
				functor.equals(FUNCTOR_THERE_EXISTS) || functor.equals(FUNCTOR_FOR_ALL) ||
				functor.equals(FUNCTOR_MAY_BE_SAME_AS)) {
			return false;
		}

		return true;
	}

	public boolean isRandomVariableValue (Expression e, Map<String, Set<Integer>> randomVariableIndex) {
		if (isRandomFunctionApplication(e))
			return true;

		if (e.getSyntacticFormType().equals("Symbol")) {
			if (randomVariableIndex == null) {
				return false;
			}

			Set<Integer> paramCounts = randomVariableIndex.get(e.toString());
			if (paramCounts != null && paramCounts.contains(0)) {
				return true;
			}
		}

		return false;
	}

	public void createTransformedFunctionConstraints (String functionName, int numArgs, List<Expression> parfactors) {
		StringBuilder rule = new StringBuilder();
		int ii;
		rule.append("if ");
		rule.append(functionName);
		rule.append('(');
		for (ii = 0; ii < numArgs-1; ii++) {
			rule.append('X');
			rule.append(ii);
			rule.append(',');
		}
		rule.append("Y) then not ");
		rule.append(functionName);
		rule.append('(');
		for (ii = 0; ii < numArgs-1; ii++) {
			rule.append('X');
			rule.append(ii);
			rule.append(',');
		}
		rule.append("Z);");

//		System.out.println(rule.toString());
		parfactors.add(translateConditionalRule(ruleParser.parse(rule.toString())));

		rule = new StringBuilder();
		rule.append("there exists Y : " + functionName + "(");
		for (ii = 0; ii < numArgs-1; ii++) {
			rule.append('X');
			rule.append(ii);
			rule.append(',');
		}
		rule.append("Y);");

//		System.out.println(rule.toString());
		parfactors.add(translateAtomicRule(ruleParser.parse(rule.toString())));
	}

	public Expression updateRandomVariableDeclaration (Expression randomVariableDecl) {
		if (!randomVariableDecl.getFunctor().equals(FUNCTOR_RANDOM_VARIABLE_DECLARATION))
			return null;

		// If the return type is Boolean, don't update the declaration.
		List<Expression> oldArgs = randomVariableDecl.getArguments();
		if (oldArgs.get(oldArgs.size()-1).equals("Boolean"))
			return randomVariableDecl;

		List<Expression> newArgs = new ArrayList<Expression>(oldArgs.size()+1);
		for (int ii = 0; ii < oldArgs.size(); ii++) {
			if (ii == 1)
				newArgs.add(DefaultSymbol.createSymbol(oldArgs.size() - 2));
			else
				newArgs.add(oldArgs.get(ii));
		}
		return new DefaultCompoundSyntaxTree(randomVariableDecl.getFunctor(), newArgs);
	}

	/**
	 * Removes the quantifiers from potential expressions.
	 * @param potentialExpressions  The potential expressions to check for quantifiers.
	 * @return  Quantifier free versions of the expressions.
	 */
	public List<Expression> translateQuantifiers (List<Expression> potentialExpressions) {
		List<Expression> result = new ArrayList<Expression>();
		List<Expression> expressionCopy = new ArrayList<Expression>(potentialExpressions);
		for (int ii = 0; ii < expressionCopy.size(); ii++) {
			Expression toReplace = expressionCopy.get(ii);
			Expression replaced  = toReplace;

			ReplaceQuantifierFunction replacementFunction = new ReplaceQuantifierFunction(expressionCopy);
			do {
			    toReplace = replaced;
			    replaced = toReplace.replaceAllOccurrences(
			    		replacementFunction,
			    		rewritingProcess);
			} while (replaced != toReplace);

			// Save the result.
			result.add(replaced);
		}
		return result;
	}

	/**
	 * Removes the constraints embedded in the given potential expressions.
	 * @param potentialExpressions  The potential expressions to check for embedded constraints.
	 * @return  A list of pairs of constraint-free potential expressions and the extracted constraints.
	 */
	public List<Pair<Expression, Expression>> disembedConstraints (List<Expression> potentialExpressions) {
		List<Pair<Expression, Expression>> setOfConstrainedPotentialExpressions = 
				new ArrayList<Pair<Expression, Expression>>();
		List<Pair<Expression, Expression>> result = new ArrayList<Pair<Expression, Expression>>();

		for (Expression potentialExpression : potentialExpressions) {
			Set<Pair<Expression, Expression>> mayBeSameAsSet = new HashSet<Pair<Expression, Expression>>();
//			System.out.println("Searching for 'may be same as': " + parfactor);

			// Gather instances of "may be same as".
			Expression toReplace = potentialExpression;
			Expression replaced  = toReplace;

			ReplaceMayBeSameAsFunction replacementFunction = new ReplaceMayBeSameAsFunction(mayBeSameAsSet);
			do {
			    toReplace = replaced;
			    replaced = toReplace.replaceAllOccurrences(
			    		replacementFunction,
			    		rewritingProcess);
			} while (replaced != toReplace);

			potentialExpression = replaced;

			// Simplify the updated expression.
			potentialExpression = rewritingProcess.rewrite(LBPRewriter.R_simplify, potentialExpression);

//			System.out.println("Completed search for may be same as expressions: " + context.mayBeSameAsSet);
//			System.out.println("Potential expression: " + context.currentExpression);

			// Get free variables and create inequality constraints on all pairs except those
			// pairs stated to be "may be same as".
			List<Expression> constraints = new ArrayList<Expression>();
			Set<Expression> variables = Variables.freeVariables(potentialExpression, rewritingProcess);
//			System.out.println("Free variables: " + variables);
			Expression[] variableArray = new Expression[variables.size()];
			variables.toArray(variableArray);
			for (int ii = 0; ii < variables.size() - 1; ii++) {
				for (int jj = ii+1; jj < variables.size(); jj++) {
					// Check if this pair is in the "may be same as" set.
					Expression arg1 = variableArray[ii];
					Expression arg2 = variableArray[jj];
					if (!mayBeSameAsSet.contains(new Pair<Expression, Expression>(arg1, arg2))) {
						// If the pair is not in the "may be same as" set, then add it to the list of constraints.
						constraints.add(Disequality.make(arg1, arg2));
					}
				}
			}

//			System.out.println("Generated constraints: " + constraints);
			setOfConstrainedPotentialExpressions.add(
					new Pair<Expression, Expression>(potentialExpression, And.make(constraints)));
		}

		// Extract the embedded constraints from the potential expressions.
		for (int ii = 0; ii < setOfConstrainedPotentialExpressions.size(); ii++) {

			// Check if the potential expression has any more embedded constraints.
			Pair<Expression, Expression> pair = setOfConstrainedPotentialExpressions.get(ii);
//			System.out.println("Searching for embedded constraints " + ii + ": " + pair.first);
			List<Expression> replacements = getReplacementsIfAny(pair.first, rewritingProcess);

			// If the result is null, then were no more embedded constraints found.  If the
			// result is non-null, then we add the true and false substituted versions of the
			// potential expression to the end of the list of potential expressions to be process,
			// so that we can check if there are more embedded constraints to extract.
			if (replacements == null) {
				// Add the complete parfactor to the completely-processed parfactor list.
				result.add(pair);
//				context.setOfConstrainedPotentialExpressions.add(pair);
//				context.processedParfactors.add(createParfactor(pair.first, pair.second));
			}
			else {
				// Add the positive case to the list of potential expressions for further processing.
				Expression constraints = pair.second;
				Expression additionalConstraint = replacements.get(2);
				Expression potentialExpression = replacements.get(0);
				addFurtherConstrainedPotentialExpression(setOfConstrainedPotentialExpressions, potentialExpression, constraints, additionalConstraint);

				// Add the negative case to the list of potential expressions for further processing.
				constraints = pair.second;
				additionalConstraint = Not.make(replacements.get(2));
				potentialExpression = replacements.get(1);
				addFurtherConstrainedPotentialExpression(setOfConstrainedPotentialExpressions, potentialExpression, constraints, additionalConstraint);
			}

		}
		return result;
		
	}

	private void addFurtherConstrainedPotentialExpression(List<Pair<Expression, Expression>> setOfConstrainedPotentialExpressions, Expression potentialExpression, Expression constraints, Expression additionalConstraint) {
		Expression cPrime;
		constraints = And.make(constraints, additionalConstraint);
		cPrime = rewritingProcess.rewrite(LBPRewriter.R_simplify, constraints);
		if (!cPrime.equals(Expressions.FALSE)) {
			RewritingProcess processUnderAssumption  = GrinderUtil.extendContextualConstraint(cPrime, rewritingProcess);
			Expression pPrime = processUnderAssumption.rewrite(LBPRewriter.R_simplify, potentialExpression);
			if (!Expressions.isNumber(pPrime)) {
				setOfConstrainedPotentialExpressions.add(new Pair<Expression, Expression>(pPrime, cPrime));
			}
		}
	}

	public Expression createParfactor (Expression potentialExpression, List<Expression> constraints) {
		return createParfactor(potentialExpression, And.make(constraints));
	}

	public Expression createParfactor (Expression potentialExpression, Expression constraints) {
		Set<Expression> variableSet = Variables.freeVariables(potentialExpression, rewritingProcess);
		List<Expression> variableList = new ArrayList<Expression>();
		for (Expression variable : variableSet) {
			variableList.add(variable);
		}
		return IntensionalSet.makeMultiSetFromIndexExpressionsList(
				variableList, 
				potentialExpression, constraints);
	}


	/*===================================================================================
	 * PRIVATE METHODS
	 *=================================================================================*/
	/**
	 * Recursively walks the tree subnodes of the given expression, calling the node inspector at
	 * every subnode, including the given expression itself.
	 * @param node       The current expression/subexpression to inspect.
	 * @param context    Contextual information to pass to the node inspector.
	 * @param inspector  The node inspector to call on each subnode.
	 */
//	private void walkNode (Expression node, Object context, NodeInspector inspector) {
//		List<Expression> children = node.getArguments();
//		boolean isContinue = inspector.inspectNode(node, /*child,*/ context);
//		if (isContinue) {
//			for (Expression child : children) {
//				walkNode(child, context, inspector);
//			}
//		}
//	}

	/**
	 * Generates an expression representing one minus the value given.
	 * @param potential   The reference value.
	 * @return An expression representing 1 minus the given potential value.
	 */
	private Expression oneMinusPotential (Expression potential) {

		if (potential instanceof DefaultSymbol) {
			try {
				NumberFormat format = NumberFormat.getNumberInstance();
				Number number = format.parse(potential.toString());
				return DefaultSymbol.createSymbol(1 - number.doubleValue());
			}
			catch(ParseException e) {
				
			}
		}
		return new DefaultCompoundSyntaxTree("-", 1, potential);
	}

	/**
	 * Returns list (Pt, Pf, Constraint) if potentialExpression (P, C) contains a constraint Constraint, and Pt and Pf are P[Constraint/true] and P[Constraint/false] respectively,
	 * or null if there is no such constraint.
	 */
	private List<Expression> getReplacementsIfAny(Expression potentialExpression, RewritingProcess process) {
		Expression pT = potentialExpression.replaceFirstOccurrence(positiveEmbeddedConstraintReplacementFunction, process);
		if (pT == potentialExpression) {
			return null;
		}

		Expression pF = potentialExpression.replaceFirstOccurrence(negativeEmbeddedConstraintReplacementFunction, process);
		List<Expression> result = new ArrayList<Expression>();
		result.add(pT);
		result.add(pF);
		result.add(negativeEmbeddedConstraintReplacementFunction.constraint);
//		System.out.println("Positive replacement: " + pT);
//		System.out.println("Negative replacement: " + pF);
//		System.out.println("Constraint: " + negativeEmbeddedConstraintReplacementFunction.constraint);
		return result;
	}

	/**
	 * Returns list of fours elements ( (P,C), Pt, Pf, Constraint) ) where (P,C) is the first constrained potential expression that has a constraint Constraint,
	 * and Pt and Pf are P[Constraint/true] and P[Constraint/false] respectively,
	 * or null if there is no such constrained potential expression.
	 */
//	private List<Object> getConstrainedPotentialExpressionReplacementsIfAny(List<Pair<Expression,Expression>> constrainedPotentialExpressions, RewritingProcess process) {
//		for (Pair<Expression, Expression> constrainedPotentialExpression : constrainedPotentialExpressions) {
//			List<Object> result= getReplacementsIfAny(constrainedPotentialExpression.first , process);
//			if (result!= null) {
//				((ArrayList<Object>)result).add(0, constrainedPotentialExpression);
//				return result;
//			}
//		}
//		return null;
//	}


}

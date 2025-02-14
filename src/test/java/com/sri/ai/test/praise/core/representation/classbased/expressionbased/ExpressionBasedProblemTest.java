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
package com.sri.ai.test.praise.core.representation.classbased.expressionbased;

import static com.sri.ai.expresso.helper.Expressions.parse;
import static com.sri.ai.util.Util.println;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.sri.ai.expresso.api.Expression;
import com.sri.ai.praise.core.representation.classbased.expressionbased.api.ExpressionBasedProblem;
import com.sri.ai.praise.core.representation.classbased.expressionbased.core.DefaultExpressionBasedProblem;
import com.sri.ai.praise.core.representation.classbased.hogm.components.HOGMExpressionBasedModel;

public class ExpressionBasedProblemTest {

	@Test
	public void test() {
		
		String modelString;
		String queryString;

		String expectedFactors;
		String expectedQuerySymbolString;
		boolean expectedQueryIsCompound;
		String expectedSymbolsAndTypesString;
		
		modelString = ""
				+  
				"random damage : 1..10;" +
				"random earthquake: Boolean;" + 
				"" + 
				"earthquake 0.01;" + 
				"if earthquake then if damage > 8 then 0.4 else 0.25 else 0.1;";
		
		queryString = "earthquake";
		expectedFactors = "[if earthquake then 0.01 else 0.99, if earthquake then if damage > 8 then 0.4 else 0.25 else 0.1]";
		expectedQuerySymbolString = "earthquake";
		expectedQueryIsCompound = false;
		expectedSymbolsAndTypesString = "{damage=1..10, earthquake=Boolean, false=Boolean, true=Boolean}";
		runTest(modelString, queryString, expectedFactors, expectedQuerySymbolString, expectedQueryIsCompound, expectedSymbolsAndTypesString);
		
		queryString = "damage > 5";
		expectedFactors = "[if earthquake then 0.01 else 0.99, if earthquake then if damage > 8 then 0.4 else 0.25 else 0.1, if query <=> (damage > 5) then 1 else 0]"; 
		expectedQuerySymbolString = "query";
		expectedQueryIsCompound = true;
		expectedSymbolsAndTypesString = "{query=Boolean, damage=1..10, earthquake=Boolean, false=Boolean, true=Boolean}";
		runTest(modelString, queryString, expectedFactors, expectedQuerySymbolString, expectedQueryIsCompound, expectedSymbolsAndTypesString);
		
		queryString = "damage + 1";
		expectedFactors = "[if earthquake then 0.01 else 0.99, if earthquake then if damage > 8 then 0.4 else 0.25 else 0.1, if query = damage + 1 then 1 else 0]"; 
		expectedQuerySymbolString = "query";
		expectedQueryIsCompound = true;
		expectedSymbolsAndTypesString = "{query=Integer, damage=1..10, earthquake=Boolean, false=Boolean, true=Boolean}";
		runTest(modelString, queryString, expectedFactors, expectedQuerySymbolString, expectedQueryIsCompound, expectedSymbolsAndTypesString);
	}

	private void runTest(String modelString, String queryString,
			String expectedFactorExpressionsIncludingQueryDefinitionIfAnyString, String expectedQuerySymbolString,
			boolean expectedQueryIsCompound, String expectedSymbolsAndTypesString) {
		
		HOGMExpressionBasedModel model = new HOGMExpressionBasedModel(modelString);
		Expression queryExpression = parse(queryString);
		ExpressionBasedProblem problem = new DefaultExpressionBasedProblem(queryExpression, model);

		String actualFactorExpressionsIncludingQueryDefinitionIfAnyString = problem.getFactorExpressionsIncludingQueryDefinitionIfAny().toString();
		println(expectedFactorExpressionsIncludingQueryDefinitionIfAnyString);
		println(actualFactorExpressionsIncludingQueryDefinitionIfAnyString);
		assertEquals(expectedFactorExpressionsIncludingQueryDefinitionIfAnyString, actualFactorExpressionsIncludingQueryDefinitionIfAnyString);
		
		String actualQuerySymbol = problem.getQuerySymbol().toString();
		println("expected querySymbol: " + expectedQuerySymbolString);
		println("actual   querySymbol: " + actualQuerySymbol);
		assertEquals(expectedQuerySymbolString, actualQuerySymbol);
		
		boolean actualQueryIsCompound = problem.getQueryIsCompound();
		println("expected queryIsCompound: " + expectedQueryIsCompound);
		println("actual   queryIsCompound: " + actualQueryIsCompound);
		assertEquals(expectedQueryIsCompound, actualQueryIsCompound);
		
		String actualSymbolsAndTypesString = problem.getContext().getSymbolsAndTypes().toString();
		println("expected context string: " + expectedSymbolsAndTypesString);
		println("actual   context string: " + actualSymbolsAndTypesString);
		assertEquals(expectedSymbolsAndTypesString, actualSymbolsAndTypesString);
	}

}

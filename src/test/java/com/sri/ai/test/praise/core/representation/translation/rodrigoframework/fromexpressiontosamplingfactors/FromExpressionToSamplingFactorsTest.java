package com.sri.ai.test.praise.core.representation.translation.rodrigoframework.fromexpressiontosamplingfactors;

import static com.sri.ai.expresso.helper.Expressions.parse;
import static com.sri.ai.util.Util.list;
import static com.sri.ai.util.Util.println;
import static org.junit.Assert.assertEquals;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.Test;

import com.sri.ai.expresso.api.Expression;
import com.sri.ai.praise.core.representation.interfacebased.factor.api.Factor;
import com.sri.ai.praise.core.representation.interfacebased.factor.core.sampling.core.distribution.EqualitySamplingFactor;
import com.sri.ai.praise.core.representation.interfacebased.factor.core.sampling.core.distribution.NormalWithFixedStandardDeviation;
import com.sri.ai.praise.core.representation.interfacebased.factor.core.sampling.core.factor.ConstantSamplingFactor;
import com.sri.ai.praise.core.representation.translation.rodrigoframework.fromexpressionstosamplingfactors.FromExpressionToSamplingFactors;

class FromExpressionToSamplingFactorsTest {
	
	private Random random = new Random();
	
	private static LinkedList<Expression> variables = list(parse("X"), parse("Y"), parse("Z"));

	@Test
	void equalityOfTwoVariablesTest() {
		Expression expression;
		List<? extends Factor> factors;
		FromExpressionToSamplingFactors compiler = new FromExpressionToSamplingFactors(v -> variables.contains(v), random);
		
		expression = parse("X = Y");
		factors = compiler.factorCompilation(expression);
		
		println(factors);

		assertEquals(1, factors.size());
		assertEquals(EqualitySamplingFactor.class, factors.get(0).getClass());
		
		Expression firstCompiledVariable = (Expression) factors.get(0).getVariables().get(0);
		Expression secondCompiledVariable = (Expression) factors.get(0).getVariables().get(1);
		
		assertEquals(parse("X"), firstCompiledVariable);
		assertEquals(parse("Y"), secondCompiledVariable);
	}

	@Test
	void equalityOfVariableAndConstantTest() {
		Expression expression;
		List<? extends Factor> factors;
		FromExpressionToSamplingFactors compiler = new FromExpressionToSamplingFactors(v -> variables.contains(v), random);
		
		expression = parse("X = 10");
		factors = compiler.factorCompilation(expression);
		
		println(factors);

		assertEquals(1, factors.size());
		assertEquals(ConstantSamplingFactor.class, factors.get(0).getClass());
		
		ConstantSamplingFactor constantSamplingFactor = (ConstantSamplingFactor) factors.get(0);
		Expression variableConstrainedToBeingConstant = (Expression) constantSamplingFactor.getVariables().get(0);
		
		assertEquals(parse("X"), variableConstrainedToBeingConstant);
		assertEquals(new Double(10.0), constantSamplingFactor.getConstant());
	}

	@Test
	void equalityOfVariableAndNormalTest() {
		Expression expression;
		List<? extends Factor> factors;
		FromExpressionToSamplingFactors compiler = new FromExpressionToSamplingFactors(v -> variables.contains(v), random);
		
		expression = parse("X = Normal(Y, 0.1)");
		factors = compiler.factorCompilation(expression);
		
		println(factors);
		
		assertEquals(1, factors.size());
		assertEquals(NormalWithFixedStandardDeviation.class, factors.get(0).getClass());
		
		NormalWithFixedStandardDeviation normalSamplingFactor = (NormalWithFixedStandardDeviation) factors.get(0);
		Expression variableConstrainedToBeingNormal = (Expression) normalSamplingFactor.getVariables().get(0);
		
		assertEquals(parse("X"), variableConstrainedToBeingNormal);
		
		Expression meanVariable = (Expression) normalSamplingFactor.getVariables().get(1);
		assertEquals(parse("Y"), meanVariable);
		
		assertEquals(0.1, normalSamplingFactor.getStandardDeviation(), 0.0);
	}

}
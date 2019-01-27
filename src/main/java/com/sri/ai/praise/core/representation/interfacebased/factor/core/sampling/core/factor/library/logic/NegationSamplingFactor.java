package com.sri.ai.praise.core.representation.interfacebased.factor.core.sampling.core.factor.library.logic;

import java.util.Random;

import com.sri.ai.praise.core.representation.interfacebased.factor.api.Variable;
import com.sri.ai.praise.core.representation.interfacebased.factor.core.sampling.core.factor.library.base.AbstractDeterministicInvertibleUnaryFunctionSamplingFactor;
import com.sri.ai.praise.core.representation.interfacebased.factor.core.sampling.core.factor.library.number.AbstractDeterministicNumericUnaryFunctionSamplingFactor;

/**
 * An extension of {@link AbstractDeterministicNumericUnaryFunctionSamplingFactor} for negation.
 *
 * @author braz
 *
 */
public class NegationSamplingFactor extends AbstractDeterministicInvertibleUnaryFunctionSamplingFactor<Boolean> {

	public NegationSamplingFactor(Variable result, Variable argument, Random random) {
		super(result, argument, random);
	}

	////////////////////

	@Override
	protected Boolean operation(Boolean argumentValue) {
		return ! argumentValue;
	}

	@Override
	protected String operatorSymbol() {
		return "not";
	}

	@Override
	protected Boolean computeArgumentFromResult(Boolean functionResultValue) {
		return ! functionResultValue;
	}

	@Override
	protected String getFunctionName() {
		return "not";
	}

	@Override
	protected boolean isValidValue(Boolean value) {
		return true;
	}

}
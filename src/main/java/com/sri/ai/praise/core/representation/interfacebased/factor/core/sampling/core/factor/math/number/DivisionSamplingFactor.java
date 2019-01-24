package com.sri.ai.praise.core.representation.interfacebased.factor.core.sampling.core.factor.math.number;

import static com.sri.ai.util.Util.list;
import static com.sri.ai.util.Util.normalizeDoubleZeroToPositiveZero;

import java.util.List;
import java.util.Random;

import com.sri.ai.praise.core.representation.interfacebased.factor.api.Variable;
import com.sri.ai.praise.core.representation.interfacebased.factor.core.sampling.core.factor.math.AbstractDeterministicNumericBinaryFunctionSamplingFactor;

/**
 * An extension of {@link AbstractDeterministicNumericBinaryFunctionSamplingFactor} for division.
 *
 * @author braz
 *
 */
public class DivisionSamplingFactor extends AbstractDeterministicNumericBinaryFunctionSamplingFactor {

	public DivisionSamplingFactor(Variable result, List<? extends Variable> arguments, Random random) {
		super(result, arguments, random);
	}

	public DivisionSamplingFactor(Variable result, Variable base, Variable exponent, Random random) {
		this(result, list(base, exponent), random);
	}

	////////////////////

	@Override
	protected double operation(Double firstValue, Double secondValue) {
		return normalizeDoubleZeroToPositiveZero(firstValue / secondValue);
	}

	@Override
	protected String operatorSymbol() {
		return "/";
	}

	@Override
	protected double computeFirstFromOthers(Double secondValue, Double functionResultValue) {
		return normalizeDoubleZeroToPositiveZero(functionResultValue * secondValue);
	}

	@Override
	protected double computeSecondFromOthers(Double firstValue, Double functionResultValue) {
		return normalizeDoubleZeroToPositiveZero(functionResultValue * firstValue);
	}

	@Override
	protected String getFunctionName() {
		return "/";
	}

}
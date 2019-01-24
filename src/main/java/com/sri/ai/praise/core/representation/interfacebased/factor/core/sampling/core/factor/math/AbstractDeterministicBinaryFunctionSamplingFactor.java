package com.sri.ai.praise.core.representation.interfacebased.factor.core.sampling.core.factor.math;

import static com.sri.ai.util.Util.iterator;

import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

import com.sri.ai.praise.core.representation.interfacebased.factor.api.Variable;
import com.sri.ai.praise.core.representation.interfacebased.factor.core.sampling.core.factor.AbstractDeterministicFunctionSamplingFactor;
import com.sri.ai.util.base.NullaryFunction;

/**
 * A specialization of {@link AbstractDeterministicFunctionSamplingFactor} for numeric operators with two arguments.
 * 
 * @author braz
 *
 */
public abstract class AbstractDeterministicBinaryFunctionSamplingFactor<A, B, R>
		extends AbstractDeterministicFunctionSamplingFactor {

	protected abstract String operatorSymbol();

	protected abstract double operation(A firstValue, B secondValue);

	protected abstract double computeFirstFromOthers(B secondValue, R functionResultValue);

	protected abstract double computeSecondFromOthers(A firstValue, R functionResultValue);

	abstract protected boolean isValidResult(R value);

	abstract protected boolean isValidFirstArgument(A value);

	abstract protected boolean isValidSecondArgument(B value);

	////////////////////
	
	private Variable first;
	private Variable second;

	////////////////////
	
	public AbstractDeterministicBinaryFunctionSamplingFactor(Variable result, List<? extends Variable> arguments, Random random) {
		super(result, arguments, random);
		this.first = arguments.get(0);
		this.second = arguments.get(1);
	}

	////////////////////
	
	@SuppressWarnings("unchecked")
	@Override
	protected Object evaluateFunctionFromAllArguments(Function<Variable, Object> fromVariableToValue) {
		A firstValue = getFirstValue(fromVariableToValue);
		B secondValue = getSecondValue(fromVariableToValue);
		Object result = computeWithErrorChecking(fromVariableToValue, () -> operation(firstValue, secondValue));
		check(isValidResult((R)result), getFunctionResult(), result, fromVariableToValue);
		return result;
	}

	@Override
	protected Iterator<? extends Integer> argumentsWithInverseFunctionIterator() {
		return iterator(0, 1); 
	}

	@Override
	protected Object computeMissingArgumentValue(Function<Variable, Object> fromVariableToValue, int missingArgumentIndex) {
		Object result = 
				computeWithErrorChecking(
						fromVariableToValue, 
						() -> computeMissingArgumentValueWithoutOperationErrorChecking(
								fromVariableToValue, 
								missingArgumentIndex));
		
		checkArgument(missingArgumentIndex, result, fromVariableToValue);
		
		return result;
	}

	private Object computeMissingArgumentValueWithoutOperationErrorChecking(Function<Variable, Object> fromVariableToValue, int missingArgumentIndex) throws Error {
		if (missingArgumentIndex == 0) {
			return computeFirstFromOthers(fromVariableToValue);
		}
		else if (missingArgumentIndex == 1) {
			return computeSecondFromOthers(fromVariableToValue);
		}
		else {
			throw new Error("computeMissingArgumentValue got invalid argument index " + missingArgumentIndex + " while solving" + problemDescription(fromVariableToValue));
		}
	}

	private Object computeFirstFromOthers(Function<Variable, Object> fromVariableToValue) {
		B secondValue = getSecondValue(fromVariableToValue);
		R functionResultValue = getResultValue(fromVariableToValue);
		double result = computeFirstFromOthers(secondValue, functionResultValue);
		return result;
	}

	private Object computeSecondFromOthers(Function<Variable, Object> fromVariableToValue) {
		A firstValue = getFirstValue(fromVariableToValue);
		R functionResultValue = getResultValue(fromVariableToValue);
		double result = computeSecondFromOthers(firstValue, functionResultValue);
		return result;
	}

	private Object computeWithErrorChecking(Function<Variable, Object> fromVariableToValue, NullaryFunction<Object> calculation) throws Error {
		Object result;
		try {
			result = calculation.apply();
		}
		catch (Throwable e) {
			throw new Error("Error solving " + problemDescription(fromVariableToValue) + ": " + e.getMessage(), e);
		}
		
		return result;
	}

	////////////////////
	
	@SuppressWarnings("unchecked")
	private R getResultValue(Function<Variable, Object> fromVariableToValue) {
		R resultValue = (R) fromVariableToValue.apply(getFunctionResult());
		return resultValue;
	}

	@SuppressWarnings("unchecked")
	private A getFirstValue(Function<Variable, Object> fromVariableToValue) {
		A firstValue = (A) fromVariableToValue.apply(first);
		return firstValue;
	}

	@SuppressWarnings("unchecked")
	private B getSecondValue(Function<Variable, Object> fromVariableToValue) {
		B secondValue = (B) fromVariableToValue.apply(second);
		return secondValue;
	}

	////////////////////
	
	private String problemDescription(Function<Variable, Object> fromVariableToValue) {
		return 
				valueOrVariable(getFunctionResult(), fromVariableToValue) 
				+ " = " 
				+ valueOrVariable(first, fromVariableToValue) 
				+ operatorSymbol() 
				+ valueOrVariable(second, fromVariableToValue);
	}

	private Object valueOrVariable(Variable variable, Function<Variable, Object> fromVariableToValue) {
		Object value = fromVariableToValue.apply(variable);
		if (value == null) {
			return variable;
		}
		else {
			return value;
		}
	}

	@SuppressWarnings("unchecked")
	private void checkArgument(int missingArgumentIndex, Object value, Function<Variable, Object> fromVariableToValue) {
		if (missingArgumentIndex == 0) {
			check(isValidFirstArgument((A) value), getArguments().get(0), value, fromVariableToValue);
		}
		else {
			check(isValidSecondArgument((B) value), getArguments().get(1), value, fromVariableToValue);
		}
	}

	private void check(boolean isValid, Variable variable, Object checkedValue, Function<Variable, Object> fromVariableToValue) {
		if (isValid) {
			throw new Error("Error solving " + problemDescription(fromVariableToValue) + ": illegal arguments resulting in " + variable + " = " + checkedValue);
		}
	}

}
package com.sri.ai.praise.core.representation.translation.rodrigoframework.samplinggraph2d;

import java.util.ArrayList;

import com.sri.ai.praise.core.representation.interfacebased.factor.core.sampling.api.factor.SamplingFactor;
import com.sri.ai.praise.core.representation.interfacebased.factor.core.sampling.api.sample.Sample;
import com.sri.ai.util.function.api.values.Value;
import com.sri.ai.util.function.api.variables.Assignment;
import com.sri.ai.util.function.api.variables.Variable;
import com.sri.ai.util.function.core.functions.FunctionToSingleInputFunctionAdapter;

public class SamplingFactorDiscretizedProbabilityDistributionSingleInputFunctionAdapter 
extends FunctionToSingleInputFunctionAdapter
implements SamplingFactorDiscretizedProbabilityDistributionSingleInputFunction {
	
	public SamplingFactorDiscretizedProbabilityDistributionSingleInputFunctionAdapter(
			SamplingFactorDiscretizedProbabilityDistributionFunction base) {
		
		super(base);
	}
	
	@Override
	public DefaultSamplingFactorDiscretizedProbabilityDistributionFunction getBase() {
		return (DefaultSamplingFactorDiscretizedProbabilityDistributionFunction) super.getBase();
	}
	
	@Override
	public void sample() {
		getBase().sample();
	}

	@Override
	public SamplingFactor getSamplingFactor() {
		return getBase().getSamplingFactor();
	}

	@Override
	public SamplingFactorDiscretizedProbabilityDistribution getConditionalDistribution() {
		return getBase().getConditionalDistribution();
	}

	@Override
	public SamplingFactorDiscretizedProbabilityDistributionSingleInputFunction 
	project(Variable variable, Assignment assignmentToRemainingVariables) {
		
		return getBase().project(variable, assignmentToRemainingVariables);
	}

	@Override
	public SamplingFactorDiscretizedProbabilityDistributionFunction condition(Sample conditioningSample) {
		return getBase().condition(conditioningSample);
	}

	@Override
	public Value apply(ArrayList<Object> values) {
		return getBase().apply(values);
	}

	@Override
	public double getTotalWeight() {
		return getBase().getTotalWeight();
	}

}

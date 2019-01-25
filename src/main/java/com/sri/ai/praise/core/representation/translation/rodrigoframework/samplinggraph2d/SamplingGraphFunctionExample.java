package com.sri.ai.praise.core.representation.translation.rodrigoframework.samplinggraph2d;

import static com.sri.ai.util.Util.arrayList;
import static com.sri.ai.util.Util.getIndexOf;
import static com.sri.ai.util.Util.list;
import static com.sri.ai.util.Util.println;
import static com.sri.ai.util.function.api.functions.Functions.functions;
import static com.sri.ai.util.function.api.variables.Variable.realVariable;

import java.util.Random;

import com.sri.ai.praise.core.representation.interfacebased.factor.core.base.DefaultVariable;
import com.sri.ai.praise.core.representation.interfacebased.factor.core.sampling.api.factor.SamplingFactor;
import com.sri.ai.praise.core.representation.interfacebased.factor.core.sampling.core.distribution.NormalWithFixedMeanAndStandardDeviation;
import com.sri.ai.praise.core.representation.interfacebased.factor.core.sampling.core.distribution.NormalWithFixedStandardDeviation;
import com.sri.ai.praise.core.representation.interfacebased.factor.core.sampling.core.factor.SamplingProductFactor;
import com.sri.ai.util.function.api.functions.Functions;
import com.sri.ai.util.function.api.variables.Unit;
import com.sri.ai.util.function.api.variables.Variable;
import com.sri.ai.util.function.core.variables.DefaultSetOfVariables;
import com.sri.ai.util.graph2d.api.GraphSet;

public class SamplingGraphFunctionExample {

	public static void main(String[] args) {

		boolean forDavid = false;
		
		Random random = new Random();
		int numberOfSamples = 500000;

		double mean = 50.0;
		double standardDeviation = 5.0;
		int numberOfPoints = 25;

		double axisStart = mean - standardDeviation*5;
		double axisEnd   = mean + standardDeviation*5;
		double step = (axisEnd - axisStart)/(numberOfPoints - 1);
		Variable x = realVariable("x", Unit.NONE, axisStart + "", step + "", axisEnd + "");
		Variable y = realVariable("y", Unit.NONE, axisStart + "", step + "", axisEnd + "");

		DefaultVariable xV = new DefaultVariable("x");
		DefaultVariable yV = new DefaultVariable("y");
		SamplingFactor xFromY = new NormalWithFixedStandardDeviation(
				xV,
				yV,
				standardDeviation, 
				random);
		SamplingFactor yPrior = new NormalWithFixedMeanAndStandardDeviation(
				yV,
				mean,
				15.0, 
				random);
		
		
		
		SamplingFactor factorToBeShown;
		int queryIndex;
		DefaultSamplingFactorDiscretizedProbabilityDistributionFunction function;
		
		println("Sampling...");
		if (forDavid) {
			factorToBeShown = new NormalWithFixedMeanAndStandardDeviation(
					xV,
					50.0,
					standardDeviation, 
					random);
			queryIndex = getIndexOf(factorToBeShown.getVariables(), xV);
			function = new DefaultSamplingFactorDiscretizedProbabilityDistributionFunction(factorToBeShown, new DefaultSetOfVariables(list(x)), queryIndex, numberOfSamples);
		}
		else {
			factorToBeShown = new SamplingProductFactor(arrayList(xFromY, yPrior), random);
			queryIndex = getIndexOf(factorToBeShown.getVariables(), xV);
			function = new DefaultSamplingFactorDiscretizedProbabilityDistributionFunction(factorToBeShown, new DefaultSetOfVariables(list(x, y)), queryIndex, numberOfSamples);
		}
		println("Sampling done");

		Functions functions = functions(function);
		
		GraphSet graphSet = GraphSet.plot(functions, x);

		boolean deleteFilesForCleanUp = false; // change this to clean up files.
		if (deleteFilesForCleanUp) {
			graphSet.deleteFiles(f -> println("Deleted: " + f.getName()));
		}
	}
}
package com.sri.ai.test.praise.core.inference.byinputrepresentation.classbased.hogm.sampling;

import static com.sri.ai.expresso.helper.Expressions.areEqualUpToNumericDifference;
import static com.sri.ai.expresso.helper.Expressions.parse;
import static com.sri.ai.util.Util.getFirst;
import static com.sri.ai.util.Util.list;
import static com.sri.ai.util.Util.println;
import static com.sri.ai.util.Util.repeat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.Test;

import com.sri.ai.expresso.ExpressoConfiguration;
import com.sri.ai.expresso.api.Expression;
import com.sri.ai.praise.core.inference.byinputrepresentation.classbased.hogm.sampling.HOGMMultiQuerySamplingProblemSolver;
import com.sri.ai.praise.core.inference.byinputrepresentation.classbased.hogm.solver.HOGMProblemResult;
import com.sri.ai.praise.core.representation.interfacebased.factor.core.expressionsampling.ExpressionSamplingFactor;
import com.sri.ai.praise.core.representation.translation.rodrigoframework.samplinggraph2d.SamplingFactorDiscretizedProbabilityDistributionFunction;
import com.sri.ai.util.Util;
import com.sri.ai.util.function.api.functions.Functions;
import com.sri.ai.util.graph2d.api.GraphSet;

public class HOGMMultiQuerySamplingProblemSolverTest {

	/**
	 * If property receives any value, a graph file named <code>GRAPH_FILENAME_WITHOUT_EXTENSION + ".png"</code>
	 * is generated in the current directory with the test's result graph.
	 */
	private static final String PROPERTY_KEY_GENERATING_GRAPH_FILE = "generateGraphFileForHOGMSamplingTest";
	private static final String GRAPH_FILENAME_WITHOUT_EXTENSION = "HOGMMultiQuerySamplingProblemSolverTest";

	@Test
	public void normalSamplingTest() {

		String model = "" +
				"random x : [-10;10];" +
				"x = Normal(0.0, 3.0);"
				;

		String query = "x";
		Expression expected = parse("if x < -9.583 then 0.001 else if x < -8.75 then 0.003 else if x < -7.917 then 0.004 else if x < -7.083 then 0.005 else if x < -6.25 then 0.011 else if x < -5.417 then 0.017 else if x < -4.583 then 0.031 else if x < -3.75 then 0.047 else if x < -2.917 then 0.046 else if x < -2.083 then 0.088 else if x < -1.25 then 0.08 else if x < -0.417 then 0.088 else if x < 0.417 then 0.103 else if x < 1.25 then 0.117 else if x < 2.083 then 0.088 else if x < 2.917 then 0.089 else if x < 3.75 then 0.052 else if x < 4.583 then 0.041 else if x < 5.417 then 0.027 else if x < 6.25 then 0.023 else if x < 7.083 then 0.015 else if x < 7.917 then 0.014 else if x < 8.75 then 0.003 else if x < 9.583 then 0.001 else 0");
		int numberOfInitialSamples = 1000;
		int numberOfDiscreteValues = 25;

		runTest(model, query, expected, numberOfInitialSamples, numberOfDiscreteValues);
	}

	@Test
	public void normalAndEqualitySamplingTest() {

		String model = "" +
				"random x : [-10;10];" +
				"random y : [-10;10];" +
				"y = Normal(0.0, 3);" +
				"x = y;"
				;

		String query = "x";
		Expression expected = parse("if x < -9.583 then 0.001 else if x < -8.75 then 0.001 else if x < -7.917 then 0.003 else if x < -7.083 then 0.006 else if x < -6.25 then 0.007 else if x < -5.417 then 0.014 else if x < -4.583 then 0.029 else if x < -3.75 then 0.036 else if x < -2.917 then 0.059 else if x < -2.083 then 0.098 else if x < -1.25 then 0.085 else if x < -0.417 then 0.106 else if x < 0.417 then 0.106 else if x < 1.25 then 0.106 else if x < 2.083 then 0.087 else if x < 2.917 then 0.078 else if x < 3.75 then 0.058 else if x < 4.583 then 0.051 else if x < 5.417 then 0.027 else if x < 6.25 then 0.016 else if x < 7.083 then 0.011 else if x < 7.917 then 0.004 else if x < 8.75 then 0.005 else if x < 9.583 then 0.002 else 0");
		int numberOfInitialSamples = 1000;
		int numberOfDiscreteValues = 25;

		runTest(model, query, expected, numberOfInitialSamples, numberOfDiscreteValues);
	}

	@Test
	public void normalWithVariableMeanSamplingTest() {

		String model = "" +
				"random x : [-10;10];" +
				"random y : [-10;10];" +
				"y = Normal(0.0, 1);" +
				"x = Normal(y, 3);"
				;

		String query = "x";
		Expression expected = parse("if x < -9.583 then 0.001 else if x < -8.75 then 0.001 else if x < -7.917 then 0.003 else if x < -7.083 then 0.006 else if x < -6.25 then 0.007 else if x < -5.417 then 0.014 else if x < -4.583 then 0.029 else if x < -3.75 then 0.036 else if x < -2.917 then 0.059 else if x < -2.083 then 0.098 else if x < -1.25 then 0.085 else if x < -0.417 then 0.106 else if x < 0.417 then 0.106 else if x < 1.25 then 0.106 else if x < 2.083 then 0.087 else if x < 2.917 then 0.078 else if x < 3.75 then 0.058 else if x < 4.583 then 0.051 else if x < 5.417 then 0.027 else if x < 6.25 then 0.016 else if x < 7.083 then 0.011 else if x < 7.917 then 0.004 else if x < 8.75 then 0.005 else if x < 9.583 then 0.002 else 0");
		int numberOfInitialSamples = 1000;
		int numberOfDiscreteValues = 25;

		runTest(model, query, expected, numberOfInitialSamples, numberOfDiscreteValues);
	}

	@Test
	public void doubleNormalSamplingTest() {

		String model = "" +
				"random x : [-10;10];" +
				"random y : [-10;10];" +
				"y = Normal(-9, 0.01);" +
				"x = Normal(y, 1);"
				;

		String query = "x";
		Expression expected = parse("if x < -9.96 then 0.012 else if x < -9.88 then 0.024 else if x < -9.799 then 0.026 else if x < -9.719 then 0.028 else if x < -9.639 then 0.029 else if x < -9.558 then 0.03 else if x < -9.478 then 0.032 else if x < -9.398 then 0.034 else if x < -9.317 then 0.035 else if x < -9.237 then 0.037 else if x < -9.157 then 0.037 else if x < -9.076 then 0.038 else if x < -8.996 then 0.038 else if x < -8.916 then 0.038 else if x < -8.835 then 0.037 else if x < -8.755 then 0.037 else if x < -8.675 then 0.036 else if x < -8.594 then 0.035 else if x < -8.514 then 0.034 else if x < -8.434 then 0.034 else if x < -8.353 then 0.032 else if x < -8.273 then 0.031 else if x < -8.193 then 0.028 else if x < -8.112 then 0.027 else if x < -8.032 then 0.024 else if x < -7.952 then 0.022 else if x < -7.871 then 0.02 else if x < -7.791 then 0.019 else if x < -7.711 then 0.018 else if x < -7.631 then 0.016 else if x < -7.55 then 0.014 else if x < -7.47 then 0.014 else if x < -7.39 then 0.011 else if x < -7.309 then 0.009 else if x < -7.229 then 0.009 else if x < -7.149 then 0.007 else if x < -7.068 then 0.007 else if x < -6.988 then 0.005 else if x < -6.908 then 0.005 else if x < -6.827 then 0.004 else if x < -6.747 then 0.003 else if x < -6.667 then 0.003 else if x < -6.586 then 0.002 else if x < -6.506 then 0.002 else if x < -6.426 then 0.002 else if x < -6.345 then 0.001 else if x < -6.265 then 0.001 else if x < -6.185 then 0.001 else if x < -6.104 then 0.001 else if x < -6.024 then 0.001 else if x < -5.944 then 0 else if x < -5.863 then 0 else if x < -5.783 then 0 else if x < -5.703 then 0 else if x < -5.622 then 0 else if x < -5.542 then 0 else if x < -5.462 then 0 else if x < -5.382 then 0 else if x < -5.301 then 0 else if x < -5.221 then 0 else if x < -5.141 then 0 else if x < -5.06 then 0 else if x < -4.98 then 0 else if x < -4.9 then 0 else if x < -4.819 then 0 else if x < -4.739 then 0 else if x < -4.659 then 0 else if x < -4.578 then 0 else if x < -4.498 then 0 else if x < -4.418 then 0 else if x < -4.337 then 0 else if x < -4.257 then 0 else if x < -4.177 then 0 else if x < -4.096 then 0 else if x < -4.016 then 0 else if x < -3.936 then 0 else if x < -3.855 then 0 else if x < -3.775 then 0 else if x < -3.695 then 0 else if x < -3.614 then 0 else if x < -3.534 then 0 else if x < -3.454 then 0 else if x < -3.373 then 0 else if x < -3.293 then 0 else if x < -3.213 then 0 else if x < -3.133 then 0 else if x < -3.052 then 0 else if x < -2.972 then 0 else if x < -2.892 then 0 else if x < -2.811 then 0 else if x < -2.731 then 0 else if x < -2.651 then 0 else if x < -2.57 then 0 else if x < -2.49 then 0 else if x < -2.41 then 0 else if x < -2.329 then 0 else if x < -2.249 then 0 else if x < -2.169 then 0 else if x < -2.088 then 0 else if x < -2.008 then 0 else if x < -1.928 then 0 else if x < -1.847 then 0 else if x < -1.767 then 0 else if x < -1.687 then 0 else if x < -1.606 then 0 else if x < -1.526 then 0 else if x < -1.446 then 0 else if x < -1.365 then 0 else if x < -1.285 then 0 else if x < -1.205 then 0 else if x < -1.124 then 0 else if x < -1.044 then 0 else if x < -0.964 then 0 else if x < -0.884 then 0 else if x < -0.803 then 0 else if x < -0.723 then 0 else if x < -0.643 then 0 else if x < -0.562 then 0 else if x < -0.482 then 0 else if x < -0.402 then 0 else if x < -0.321 then 0 else if x < -0.241 then 0 else if x < -0.161 then 0 else if x < -0.08 then 0 else if x < -1.12E-15 then 0 else if x < 0.08 then 0 else if x < 0.161 then 0 else if x < 0.241 then 0 else if x < 0.321 then 0 else if x < 0.402 then 0 else if x < 0.482 then 0 else if x < 0.562 then 0 else if x < 0.643 then 0 else if x < 0.723 then 0 else if x < 0.803 then 0 else if x < 0.884 then 0 else if x < 0.964 then 0 else if x < 1.044 then 0 else if x < 1.124 then 0 else if x < 1.205 then 0 else if x < 1.285 then 0 else if x < 1.365 then 0 else if x < 1.446 then 0 else if x < 1.526 then 0 else if x < 1.606 then 0 else if x < 1.687 then 0 else if x < 1.767 then 0 else if x < 1.847 then 0 else if x < 1.928 then 0 else if x < 2.008 then 0 else if x < 2.088 then 0 else if x < 2.169 then 0 else if x < 2.249 then 0 else if x < 2.329 then 0 else if x < 2.41 then 0 else if x < 2.49 then 0 else if x < 2.57 then 0 else if x < 2.651 then 0 else if x < 2.731 then 0 else if x < 2.811 then 0 else if x < 2.892 then 0 else if x < 2.972 then 0 else if x < 3.052 then 0 else if x < 3.133 then 0 else if x < 3.213 then 0 else if x < 3.293 then 0 else if x < 3.373 then 0 else if x < 3.454 then 0 else if x < 3.534 then 0 else if x < 3.614 then 0 else if x < 3.695 then 0 else if x < 3.775 then 0 else if x < 3.855 then 0 else if x < 3.936 then 0 else if x < 4.016 then 0 else if x < 4.096 then 0 else if x < 4.177 then 0 else if x < 4.257 then 0 else if x < 4.337 then 0 else if x < 4.418 then 0 else if x < 4.498 then 0 else if x < 4.578 then 0 else if x < 4.659 then 0 else if x < 4.739 then 0 else if x < 4.819 then 0 else if x < 4.9 then 0 else if x < 4.98 then 0 else if x < 5.06 then 0 else if x < 5.141 then 0 else if x < 5.221 then 0 else if x < 5.301 then 0 else if x < 5.382 then 0 else if x < 5.462 then 0 else if x < 5.542 then 0 else if x < 5.622 then 0 else if x < 5.703 then 0 else if x < 5.783 then 0 else if x < 5.863 then 0 else if x < 5.944 then 0 else if x < 6.024 then 0 else if x < 6.104 then 0 else if x < 6.185 then 0 else if x < 6.265 then 0 else if x < 6.345 then 0 else if x < 6.426 then 0 else if x < 6.506 then 0 else if x < 6.586 then 0 else if x < 6.667 then 0 else if x < 6.747 then 0 else if x < 6.827 then 0 else if x < 6.908 then 0 else if x < 6.988 then 0 else if x < 7.068 then 0 else if x < 7.149 then 0 else if x < 7.229 then 0 else if x < 7.309 then 0 else if x < 7.39 then 0 else if x < 7.47 then 0 else if x < 7.55 then 0 else if x < 7.631 then 0 else if x < 7.711 then 0 else if x < 7.791 then 0 else if x < 7.871 then 0 else if x < 7.952 then 0 else if x < 8.032 then 0 else if x < 8.112 then 0 else if x < 8.193 then 0 else if x < 8.273 then 0 else if x < 8.353 then 0 else if x < 8.434 then 0 else if x < 8.514 then 0 else if x < 8.594 then 0 else if x < 8.675 then 0 else if x < 8.755 then 0 else if x < 8.835 then 0 else if x < 8.916 then 0 else if x < 8.996 then 0 else if x < 9.076 then 0 else if x < 9.157 then 0 else if x < 9.237 then 0 else if x < 9.317 then 0 else if x < 9.398 then 0 else if x < 9.478 then 0 else if x < 9.558 then 0 else if x < 9.639 then 0 else if x < 9.719 then 0 else if x < 9.799 then 0 else if x < 9.88 then 0 else if x < 9.96 then 0 else 0");
		int numberOfInitialSamples = 1000;
		int numberOfDiscreteValues = 250;

		runTest(model, query, expected, numberOfInitialSamples, numberOfDiscreteValues);
	}

	@Test
	public void equalitySamplingTest() {

		String model = "" +
				"random x : [-10;10];" +
				"random y : [-10;10];" +
				"x = y;" +
				"y = 1.0;" +
				"";

		String query = "x";
		Expression expected = parse("if x < -9.5 then 0 else if x < -8.5 then 0 else if x < -7.5 then 0 else if x < -6.5 then 0 else if x < -5.5 then 0 else if x < -4.5 then 0 else if x < -3.5 then 0 else if x < -2.5 then 0 else if x < -1.5 then 0 else if x < -0.5 then 0 else if x < 0.5 then 0 else if x < 1.5 then 0.991 else if x < 2.5 then 0 else if x < 3.5 then 0 else if x < 4.5 then 0 else if x < 5.5 then 0 else if x < 6.5 then 0 else if x < 7.5 then 0 else if x < 8.5 then 0 else if x < 9.5 then 0 else 0");
//		Expression expected = parse("if x = 1.0 then 1 else 0");
		int numberOfInitialSamples = 1000;
		int numberOfDiscreteValues = 21;

		runTest(model, query, expected, numberOfInitialSamples, numberOfDiscreteValues);
	}

	@Test
	public void longEqualitySamplingTest() {

		String model = "" +
				"random a : [-10;10];" +
				"random b : [-10;10];" +
				"random c : [-10;10];" +
				"random d : [-10;10];" +
				"random e : [-10;10];" +
				"random f : [-10;10];" +
				"a = b;" +
				"b = c;" +
				"c = d;" +
				"d = e;" +
				"e = f;" +
				"f = 1.0;" +
				"";

		String query = "a";
		Expression expected = parse("if a < -9.5 then 0 else if a < -8.5 then 0 else if a < -7.5 then 0 else if a < -6.5 then 0 else if a < -5.5 then 0 else if a < -4.5 then 0 else if a < -3.5 then 0 else if a < -2.5 then 0 else if a < -1.5 then 0 else if a < -0.5 then 0 else if a < 0.5 then 0 else if a < 1.5 then 0.991 else if a < 2.5 then 0 else if a < 3.5 then 0 else if a < 4.5 then 0 else if a < 5.5 then 0 else if a < 6.5 then 0 else if a < 7.5 then 0 else if a < 8.5 then 0 else if a < 9.5 then 0 else 0");
//		Expression expected = parse("if a = 1.0 then 1 else 0");
		int numberOfInitialSamples = 1000;
		int numberOfDiscreteValues = 21;

		runTest(model, query, expected, numberOfInitialSamples, numberOfDiscreteValues);
	}
	
	@Test
	public void sumSamplingTest() {

		String model = "" +
				"random x : [-10;10];" +
				"random y : [-10;10];" +
				"random z : [-10;10];" +
				"x = 2;" +
				"y = 3;" +
				"z = x + y;" +
				"";

		String query = "z";
		Expression expected = parse("if z < -9.5 then 0 else if z < -8.5 then 0 else if z < -7.5 then 0 else if z < -6.5 then 0 else if z < -5.5 then 0 else if z < -4.5 then 0 else if z < -3.5 then 0 else if z < -2.5 then 0 else if z < -1.5 then 0 else if z < -0.5 then 0 else if z < 0.5 then 0 else if z < 1.5 then 0 else if z < 2.5 then 0 else if z < 3.5 then 0 else if z < 4.5 then 0 else if z < 5.5 then 0.991 else if z < 6.5 then 0 else if z < 7.5 then 0 else if z < 8.5 then 0 else if z < 9.5 then 0 else 0");
		int numberOfInitialSamples = 1000;
		int numberOfDiscreteValues = 21;

		runTest(model, query, expected, numberOfInitialSamples, numberOfDiscreteValues);
	}
	
	@Test
	public void sumInverseSamplingTest() {
	
		String model = "" +
				"random x : [-10;10];" +
				"random y : [-10;10];" +
				"random z : [-10;10];" +
				"x = 5;" +
				"z = 1;" +
				"z + 2.0 = x + y + 1.0;" +
				"";
	
		String query = "y";
		Expression expected = parse("if y < -9.5 then 0 else if y < -8.5 then 0 else if y < -7.5 then 0 else if y < -6.5 then 0 else if y < -5.5 then 0 else if y < -4.5 then 0 else if y < -3.5 then 0 else if y < -2.5 then 0.991 else if y < -1.5 then 0 else if y < -0.5 then 0 else if y < 0.5 then 0 else if y < 1.5 then 0 else if y < 2.5 then 0 else if y < 3.5 then 0 else if y < 4.5 then 0 else if y < 5.5 then 0 else if y < 6.5 then 0 else if y < 7.5 then 0 else if y < 8.5 then 0 else if y < 9.5 then 0 else 0");
		int numberOfInitialSamples = 1000;
		int numberOfDiscreteValues = 21;
	
		runTest(model, query, expected, numberOfInitialSamples, numberOfDiscreteValues);
	}

	@Test
	public void sumOfNormalsSamplingTest() {
	
		String model = "" +
				"random x : [-10;10];" +
				"random y : [-10;10];" +
				"random z : [-10;10];" +
				"x = Normal(5,2);" +
				"y = Normal(-4,2);" +
				"z = x + y;" +
				"";
	
		String query = "z";
		Expression expected = parse("if z < -9.5 then 0 else if z < -8.5 then 0.001 else if z < -7.5 then 0.001 else if z < -6.5 then 0.003 else if z < -5.5 then 0.007 else if z < -4.5 then 0.015 else if z < -3.5 then 0.031 else if z < -2.5 then 0.052 else if z < -1.5 then 0.081 else if z < -0.5 then 0.108 else if z < 0.5 then 0.132 else if z < 1.5 then 0.139 else if z < 2.5 then 0.131 else if z < 3.5 then 0.108 else if z < 4.5 then 0.081 else if z < 5.5 then 0.052 else if z < 6.5 then 0.029 else if z < 7.5 then 0.016 else if z < 8.5 then 0.007 else if z < 9.5 then 0.003 else 0.001");
		int numberOfInitialSamples = 1000;
		int numberOfDiscreteValues = 21;
	
		runTest(model, query, expected, numberOfInitialSamples, numberOfDiscreteValues);
	}

	@Test
	public void subtractionSamplingTest() {

		String model = "" +
				"random x : [-10;10];" +
				"random y : [-10;10];" +
				"random z : [-10;10];" +
				"x = 5;" +
				"y = 2;" +
				"z = x - y;" +
				"";

		String query = "z";
		Expression expected = parse("if z < -9.5 then 0 else if z < -8.5 then 0 else if z < -7.5 then 0 else if z < -6.5 then 0 else if z < -5.5 then 0 else if z < -4.5 then 0 else if z < -3.5 then 0 else if z < -2.5 then 0 else if z < -1.5 then 0 else if z < -0.5 then 0 else if z < 0.5 then 0 else if z < 1.5 then 0 else if z < 2.5 then 0 else if z < 3.5 then 0.991 else if z < 4.5 then 0 else if z < 5.5 then 0 else if z < 6.5 then 0 else if z < 7.5 then 0 else if z < 8.5 then 0 else if z < 9.5 then 0 else 0");
		int numberOfInitialSamples = 1000;
		int numberOfDiscreteValues = 21;

		runTest(model, query, expected, numberOfInitialSamples, numberOfDiscreteValues);
	}
	
	@Test
	public void subtractionInverseSamplingTest() {

		String model = "" +
				"random x : [-10;10];" +
				"random y : [-10;10];" +
				"random z : [-10;10];" +
				"x = 4;" +
				"y = 2;" +
				"x = z - y;" +
				"";

		String query = "z";
		Expression expected = parse("if z < -9.5 then 0 else if z < -8.5 then 0 else if z < -7.5 then 0 else if z < -6.5 then 0 else if z < -5.5 then 0 else if z < -4.5 then 0 else if z < -3.5 then 0 else if z < -2.5 then 0 else if z < -1.5 then 0 else if z < -0.5 then 0 else if z < 0.5 then 0 else if z < 1.5 then 0 else if z < 2.5 then 0 else if z < 3.5 then 0 else if z < 4.5 then 0 else if z < 5.5 then 0 else if z < 6.5 then 0.991 else if z < 7.5 then 0 else if z < 8.5 then 0 else if z < 9.5 then 0 else 0");
		int numberOfInitialSamples = 1000;
		int numberOfDiscreteValues = 21;

		runTest(model, query, expected, numberOfInitialSamples, numberOfDiscreteValues);
	}

	@Test
	public void multiplicationSamplingTest() {

		String model = "" +
				"random x : [-10;10];" +
				"random y : [-10;10];" +
				"random z : [-10;10];" +
				"x = 2;" +
				"y = 3;" +
				"z = x * y;" +
				"";

		String query = "z";
		Expression expected = parse("if z < -9.5 then 0 else if z < -8.5 then 0 else if z < -7.5 then 0 else if z < -6.5 then 0 else if z < -5.5 then 0 else if z < -4.5 then 0 else if z < -3.5 then 0 else if z < -2.5 then 0 else if z < -1.5 then 0 else if z < -0.5 then 0 else if z < 0.5 then 0 else if z < 1.5 then 0 else if z < 2.5 then 0 else if z < 3.5 then 0 else if z < 4.5 then 0 else if z < 5.5 then 0 else if z < 6.5 then 0.991 else if z < 7.5 then 0 else if z < 8.5 then 0 else if z < 9.5 then 0 else 0");
		int numberOfInitialSamples = 1000;
		int numberOfDiscreteValues = 21;

		runTest(model, query, expected, numberOfInitialSamples, numberOfDiscreteValues);
	}
	
	@Test
	public void multiplicationInverseSamplingTest() {

		String model = "" +
				"random x : [-10;10];" +
//				"random y : [-10;10];" +
//				"random z : [-10;10];" +
				"x = Normal(4, 2);" +
//				"y = 2;" +
//				"z = 6;" +
				"6 = x * 2;" +
				"";

		// x must be 3 with probability 1 in spite of its normal prior because x = z/y = 3/2.
		String query = "x";
		Expression expected = parse("if x < -9.5 then 0 else if x < -8.5 then 0 else if x < -7.5 then 0 else if x < -6.5 then 0 else if x < -5.5 then 0 else if x < -4.5 then 0 else if x < -3.5 then 0 else if x < -2.5 then 0 else if x < -1.5 then 0 else if x < -0.5 then 0 else if x < 0.5 then 0 else if x < 1.5 then 0 else if x < 2.5 then 0 else if x < 3.5 then 1 else if x < 4.5 then 0 else if x < 5.5 then 0 else if x < 6.5 then 0 else if x < 7.5 then 0 else if x < 8.5 then 0 else if x < 9.5 then 0 else 0");
		int numberOfInitialSamples = 1;
		int numberOfDiscreteValues = 21;

		runTest(model, query, expected, numberOfInitialSamples, numberOfDiscreteValues);
	}
	
	@Test
	public void multiplicationInverseNotKickingInWhenOtherArgumentsIsZeroSamplingTest() {

		String model = "" +
				"random x : [-10;10];" +
				"random y : [-10;10];" +
				"random z : [-10;10];" +
				"random w : [-10;10];" +
				"x = Normal(4, 2);" +
				"y = 0;" +
				"z = Normal(6, 2);" +
				"w = Normal(-4, 2);" + // making w negative so that y*w turns into -0.0 which is different from 0.0 but must be detected as absorbing element
				"z = x * y * w;" + // y is zero so z will also be zero and we cannot tell anything about the value of x, which will follow its prior.
				"";

		String query = "x";
		Expression expected = parse("if x < -9.5 then 0 else if x < -8.5 then 0 else if x < -7.5 then 0 else if x < -6.5 then 0 else if x < -5.5 then 0 else if x < -4.5 then 0 else if x < -3.5 then 0 else if x < -2.5 then 0 else if x < -1.5 then 0.003 else if x < -0.5 then 0.009 else if x < 0.5 then 0.028 else if x < 1.5 then 0.065 else if x < 2.5 then 0.122 else if x < 3.5 then 0.174 else if x < 4.5 then 0.196 else if x < 5.5 then 0.176 else if x < 6.5 then 0.121 else if x < 7.5 then 0.067 else if x < 8.5 then 0.028 else if x < 9.5 then 0.01 else 0.002");
		int numberOfInitialSamples = 1000;
		int numberOfDiscreteValues = 21;
		
		runTest(model, query, expected, numberOfInitialSamples, numberOfDiscreteValues);
	}
	
	@Test
	public void multiplicationResultIsZeroIfJustOneArgumentIs0Test() {

		String model = "" +
				"random x : [-10;10];" +
				"random y : [-10;10];" +
				"random z : [-10;10];" +
				"random w : [-10;10];" +
				"x = Normal(4, 2);" +
				"y = 0;" +
				"z = Normal(6, 2);" +
				"w = Normal(6, 2);" +
				"z = x * y * w;" + // y is zero so z will also be zero
				"";

		String query = "z";
		Expression expected = parse("if z < -9.5 then 0 else if z < -8.5 then 0 else if z < -7.5 then 0 else if z < -6.5 then 0 else if z < -5.5 then 0 else if z < -4.5 then 0 else if z < -3.5 then 0 else if z < -2.5 then 0 else if z < -1.5 then 0 else if z < -0.5 then 0 else if z < 0.5 then 1 else if z < 1.5 then 0 else if z < 2.5 then 0 else if z < 3.5 then 0 else if z < 4.5 then 0 else if z < 5.5 then 0 else if z < 6.5 then 0 else if z < 7.5 then 0 else if z < 8.5 then 0 else if z < 9.5 then 0 else 0");
		int numberOfInitialSamples = 1;
		int numberOfDiscreteValues = 21;
		
		runTest(model, query, expected, numberOfInitialSamples, numberOfDiscreteValues);
	}

	@Test
	public void divisionSamplingTest() {

		String model = "" +
				"random x : [-10;10];" +
				"random y : [-10;10];" +
				"random z : [-10;10];" +
				"x = 8;" +
				"y = 2;" +
				"z = x / y;" +
				"";

		String query = "z";
		Expression expected = parse("if z < -9.5 then 0 else if z < -8.5 then 0 else if z < -7.5 then 0 else if z < -6.5 then 0 else if z < -5.5 then 0 else if z < -4.5 then 0 else if z < -3.5 then 0 else if z < -2.5 then 0 else if z < -1.5 then 0 else if z < -0.5 then 0 else if z < 0.5 then 0 else if z < 1.5 then 0 else if z < 2.5 then 0 else if z < 3.5 then 0 else if z < 4.5 then 0.991 else if z < 5.5 then 0 else if z < 6.5 then 0 else if z < 7.5 then 0 else if z < 8.5 then 0 else if z < 9.5 then 0 else 0");
		int numberOfInitialSamples = 1;
		int numberOfDiscreteValues = 21;

		runTest(model, query, expected, numberOfInitialSamples, numberOfDiscreteValues);
	}
	
	@Test
	public void divisionInverseSamplingTest() {

		String model = "" +
				"random x : [-10;10];" +
				"random y : [-10;10];" +
				"random z : [-10;10];" +
				"x = 4;" +
				"y = 2;" +
				"x = z / y;" +
				"";

		String query = "z";
		Expression expected = parse("if z < -9.5 then 0 else if z < -8.5 then 0 else if z < -7.5 then 0 else if z < -6.5 then 0 else if z < -5.5 then 0 else if z < -4.5 then 0 else if z < -3.5 then 0 else if z < -2.5 then 0 else if z < -1.5 then 0 else if z < -0.5 then 0 else if z < 0.5 then 0 else if z < 1.5 then 0 else if z < 2.5 then 0 else if z < 3.5 then 0 else if z < 4.5 then 0 else if z < 5.5 then 0 else if z < 6.5 then 0 else if z < 7.5 then 0 else if z < 8.5 then 0.991 else if z < 9.5 then 0 else 0");
		int numberOfInitialSamples = 1;
		int numberOfDiscreteValues = 21;

		runTest(model, query, expected, numberOfInitialSamples, numberOfDiscreteValues);
	}

	@Test
	public void divisionByZeroSamplingTest() {

		String model = "" +
				"random x : [-10;10];" +
				"random y : [-10;10];" +
				"random z : [-10;10];" +
				"x = 4;" +
				"y = -4;" +
				"z = x / (x + y);" +
				"";

		String query = "z";
		Expression expected = parse("if z < -9.5 then 0 else if z < -8.5 then 0 else if z < -7.5 then 0 else if z < -6.5 then 0 else if z < -5.5 then 0 else if z < -4.5 then 0 else if z < -3.5 then 0 else if z < -2.5 then 0 else if z < -1.5 then 0 else if z < -0.5 then 0 else if z < 0.5 then 0 else if z < 1.5 then 0 else if z < 2.5 then 0 else if z < 3.5 then 0 else if z < 4.5 then 0 else if z < 5.5 then 0 else if z < 6.5 then 0 else if z < 7.5 then 0 else if z < 8.5 then 0.991 else if z < 9.5 then 0 else 0");
		int numberOfInitialSamples = 1000;
		int numberOfDiscreteValues = 21;

		try {
			runTest(model, query, expected, numberOfInitialSamples, numberOfDiscreteValues);
		}
		catch (Error e) {
			println(e.getMessage());
			if (!Util.containsAllCaseInsensitive(e.getMessage(), "illegal", "arguments")) {
				throw new AssertionError("Should have thrown 'illegal arguments' error, but threw " + e.getMessage(), e);
			}
		}
	}
	
	
	@Test
	public void exponentiationSamplingTest() {

		String model = "" +
				"random x : [-10;10];" +
				"random y : [-10;10];" +
				"random z : [-10;10];" +
				"x = 2;" +
				"y = 3;" +
				"z = x ^ y;" +
				"";

		String query = "z";
		Expression expected = parse("if z < -9.5 then 0 else if z < -8.5 then 0 else if z < -7.5 then 0 else if z < -6.5 then 0 else if z < -5.5 then 0 else if z < -4.5 then 0 else if z < -3.5 then 0 else if z < -2.5 then 0 else if z < -1.5 then 0 else if z < -0.5 then 0 else if z < 0.5 then 0 else if z < 1.5 then 0 else if z < 2.5 then 0 else if z < 3.5 then 0 else if z < 4.5 then 0 else if z < 5.5 then 0 else if z < 6.5 then 0 else if z < 7.5 then 0 else if z < 8.5 then 0.991 else if z < 9.5 then 0 else 0");
		int numberOfInitialSamples = 1;
		int numberOfDiscreteValues = 21;

		runTest(model, query, expected, numberOfInitialSamples, numberOfDiscreteValues);
	}
	
	@Test
	public void exponentiationInverseSamplingTest() {

		String model = "" +
				"random x : [-10;10];" +
				"random y : [-10;10];" +
				"random z : [-10;10];" +
				"x = 9;" +
				"y = 2;" +
				"x = z ^ y;" +
				"";

		String query = "z";
		Expression expected = parse("if z < -9.5 then 0 else if z < -8.5 then 0 else if z < -7.5 then 0 else if z < -6.5 then 0 else if z < -5.5 then 0 else if z < -4.5 then 0 else if z < -3.5 then 0 else if z < -2.5 then 0 else if z < -1.5 then 0 else if z < -0.5 then 0 else if z < 0.5 then 0 else if z < 1.5 then 0 else if z < 2.5 then 0 else if z < 3.5 then 0.991 else if z < 4.5 then 0 else if z < 5.5 then 0 else if z < 6.5 then 0 else if z < 7.5 then 0 else if z < 8.5 then 0 else if z < 9.5 then 0 else 0");
		int numberOfInitialSamples = 1;
		int numberOfDiscreteValues = 21;

		runTest(model, query, expected, numberOfInitialSamples, numberOfDiscreteValues);
	}

	@Test
	public void exponentiationIllegalSamplingTest() {

		String model = "" +
				"random x : [-10;10];" +
				"random y : [-10;10];" +
				"random z : [-10;10];" +
				"x = 0.5;" +
				"y = (-4)^x;" + // illegal square root
				"";

		String query = "y";
		Expression expected = parse("if z < -9.5 then 0 else if z < -8.5 then 0 else if z < -7.5 then 0 else if z < -6.5 then 0 else if z < -5.5 then 0 else if z < -4.5 then 0 else if z < -3.5 then 0 else if z < -2.5 then 0 else if z < -1.5 then 0 else if z < -0.5 then 0 else if z < 0.5 then 0 else if z < 1.5 then 0 else if z < 2.5 then 0 else if z < 3.5 then 0 else if z < 4.5 then 0 else if z < 5.5 then 0 else if z < 6.5 then 0 else if z < 7.5 then 0 else if z < 8.5 then 0.991 else if z < 9.5 then 0 else 0");
		int numberOfInitialSamples = 1;
		int numberOfDiscreteValues = 21;

		try {
			runTest(model, query, expected, numberOfInitialSamples, numberOfDiscreteValues);
		}
		catch (Error e) {
			println(e.getMessage());
			if (!Util.containsAllCaseInsensitive(e.getMessage(), "error", "solving")) {
				throw new AssertionError("Should have thrown 'Error solving...' error, but threw " + e.getMessage(), e);
			}
		}
	}

	@Test
	public void exponentiationIllegalInverseSamplingTest() {

		String model = "" +
				"random x : [-10;10];" +
				"random y : [-10;10];" +
				"random z : [-10;10];" +
				"-4 = x^2;" + // inverse sampling tries (-4)^0.5 = x, which is an illegal square root.
				"";

		String query = "x";
		Expression expected = parse("if z < -9.5 then 0 else if z < -8.5 then 0 else if z < -7.5 then 0 else if z < -6.5 then 0 else if z < -5.5 then 0 else if z < -4.5 then 0 else if z < -3.5 then 0 else if z < -2.5 then 0 else if z < -1.5 then 0 else if z < -0.5 then 0 else if z < 0.5 then 0 else if z < 1.5 then 0 else if z < 2.5 then 0 else if z < 3.5 then 0 else if z < 4.5 then 0 else if z < 5.5 then 0 else if z < 6.5 then 0 else if z < 7.5 then 0 else if z < 8.5 then 0.991 else if z < 9.5 then 0 else 0");
		int numberOfInitialSamples = 1;
		int numberOfDiscreteValues = 21;

		try {
			runTest(model, query, expected, numberOfInitialSamples, numberOfDiscreteValues);
		}
		catch (Error e) {
			println(e.getMessage());
			if (!Util.containsAllCaseInsensitive(e.getMessage(), "error", "solving")) {
				throw new AssertionError("Should have thrown 'Error solving...' error, but threw " + e.getMessage(), e);
			}
		}
	}
	
	@Test
	public void unaryMinusSamplingTest() {
	
		String model = "" +
				"random x : [-10;10];" +
				"random y : [-10;10];" +
				"random z : [-10;10];" +
				"x = 2;" +
				"y = -x;" +
				"";
	
		String query = "y";
		Expression expected = parse("if y < -9.5 then 0 else if y < -8.5 then 0 else if y < -7.5 then 0 else if y < -6.5 then 0 else if y < -5.5 then 0 else if y < -4.5 then 0 else if y < -3.5 then 0 else if y < -2.5 then 0 else if y < -1.5 then 0.991 else if y < -0.5 then 0 else if y < 0.5 then 0 else if y < 1.5 then 0 else if y < 2.5 then 0 else if y < 3.5 then 0 else if y < 4.5 then 0 else if y < 5.5 then 0 else if y < 6.5 then 0 else if y < 7.5 then 0 else if y < 8.5 then 0 else if y < 9.5 then 0 else 0");
		int numberOfInitialSamples = 1;
		int numberOfDiscreteValues = 21;
	
		runTest(model, query, expected, numberOfInitialSamples, numberOfDiscreteValues);
	}

	@Test
	public void unaryMinusInverseSamplingTest() {
	
		String model = "" +
				"random x : [-10;10];" +
				"-2 = -x;" +
				"";
	
		String query = "x";
		Expression expected = parse("if x < -9.5 then 0 else if x < -8.5 then 0 else if x < -7.5 then 0 else if x < -6.5 then 0 else if x < -5.5 then 0 else if x < -4.5 then 0 else if x < -3.5 then 0 else if x < -2.5 then 0 else if x < -1.5 then 0 else if x < -0.5 then 0 else if x < 0.5 then 0 else if x < 1.5 then 0 else if x < 2.5 then 0.991 else if x < 3.5 then 0 else if x < 4.5 then 0 else if x < 5.5 then 0 else if x < 6.5 then 0 else if x < 7.5 then 0 else if x < 8.5 then 0 else if x < 9.5 then 0 else 0");
		int numberOfInitialSamples = 1;
		int numberOfDiscreteValues = 21;
	
		runTest(model, query, expected, numberOfInitialSamples, numberOfDiscreteValues);
	}

	@Test
	public void booleanSamplingTest() {
	
		String model = "" +
				"random p : Boolean;" +
				"p = true;" +
				"";
	
		String query = "p";
		Expression expected = parse("if p then 1 else 0");
		int numberOfInitialSamples = 1;
		int numberOfDiscreteValues = 21;
	
		runTest(model, query, expected, numberOfInitialSamples, numberOfDiscreteValues, /* no graph for discrete values for now */ false);
	}
	
	@Test
	public void booleanFalseSamplingTest() {
	
		String model = "" +
				"random p : Boolean;" +
				"p = false;" +
				"";
	
		String query = "p";
		Expression expected = parse("if p then 0 else 1");
		int numberOfInitialSamples = 1;
		int numberOfDiscreteValues = 21;
	
		runTest(model, query, expected, numberOfInitialSamples, numberOfDiscreteValues, /* no graph for discrete values for now */ false);
	}
	
	@Test
	public void conjunctionSamplingTest() {
	
		String model = "" +
				"random p : Boolean;" +
				"random q : Boolean;" +
				"random r : Boolean;" +
				"r = (p and q);" + // TODO: replace by equivalence, as = has a parsing issue that = has greater precedence than and
				"p = true;" +      // TODO: replace by simply "p"
				"q = false;" +
				"";
		
		String query = "r";
		Expression expected = parse("if r then 0 else 1");
		int numberOfInitialSamples = 1;
		int numberOfDiscreteValues = 21;
	
		runTest(model, query, expected, numberOfInitialSamples, numberOfDiscreteValues, /* no graph for discrete values for now */ false);
		
		query = "p";
		expected = parse("if p then 1 else 0");
		numberOfInitialSamples = 1;
		numberOfDiscreteValues = 21;
	
		runTest(model, query, expected, numberOfInitialSamples, numberOfDiscreteValues, /* no graph for discrete values for now */ false);
		
		query = "q";
		expected = parse("if q then 0 else 1");
		numberOfInitialSamples = 1;
		numberOfDiscreteValues = 21;
	
		runTest(model, query, expected, numberOfInitialSamples, numberOfDiscreteValues, /* no graph for discrete values for now */ false);
	}
	
	@Test
	public void conjunctionInverseSamplingTest() {
	
		String model = "" +
				"random p : Boolean;" +
				"random q : Boolean;" +
				"random r : Boolean;" +
				"r = (p and q);" + // TODO: replace by equivalence, as = has a parsing issue that = has greater precedence than and
				"r = true;" +      // TODO: replace by simply "r"
				"q = true;" +
				"";
		// p should be true by inversion in r = (p and q) when r and q are true
		
		String query = "r";
		Expression expected = parse("if r then 1 else 0");
		int numberOfInitialSamples = 1;
		int numberOfDiscreteValues = 21;
	
		runTest(model, query, expected, numberOfInitialSamples, numberOfDiscreteValues, /* no graph for discrete values for now */ false);
		
		query = "p";
		expected = parse("if p then 1 else 0");
		numberOfInitialSamples = 1;
		numberOfDiscreteValues = 21;
	
		runTest(model, query, expected, numberOfInitialSamples, numberOfDiscreteValues, /* no graph for discrete values for now */ false);
		
		query = "q";
		expected = parse("if q then 1 else 0");
		numberOfInitialSamples = 1;
		numberOfDiscreteValues = 21;
	
		runTest(model, query, expected, numberOfInitialSamples, numberOfDiscreteValues, /* no graph for discrete values for now */ false);
	}

	@Test
	public void conjunctionResultIsFalseIfOnlyOneArgumentIsFalseSamplingTest() {
	
		String model = "" +
				"random p : Boolean;" +
				"random q : Boolean;" +
				"random r : Boolean;" +
				"r = (p and q);" + // TODO: replace by equivalence, as = has a parsing issue that = has greater precedence than and
				"q = false;" +
				"";
		// note that p has no specified distribution
		// this is on purpose so that it does not get instantiated and allows r to be determined in absence of its information
		// However, this model will raise an error if p is to be sampled, as tested below.
		
		String query = "r";
		Expression expected = parse("if r then 0 else 1");
		int numberOfInitialSamples = 1;
		int numberOfDiscreteValues = 21;
	
		runTest(model, query, expected, numberOfInitialSamples, numberOfDiscreteValues, /* no graph for discrete values for now */ false);
		
		query = "q";
		expected = parse("if q then 0 else 1");
		numberOfInitialSamples = 1;
		numberOfDiscreteValues = 21;
	
		runTest(model, query, expected, numberOfInitialSamples, numberOfDiscreteValues, /* no graph for discrete values for now */ false);

		query = "p";
		expected = null; // should cause error
		numberOfInitialSamples = 1;
		numberOfDiscreteValues = 21;
	
		try {
			runTest(model, query, expected, numberOfInitialSamples, numberOfDiscreteValues, /* no graph for discrete values for now */ false);
		}
		catch (Throwable e) {
			println(e.getMessage());
			if (!Util.containsAllCaseInsensitive(e.getMessage(), "could not sample p")) {
				throw new AssertionError("Should have thrown \"Could not sample p\" error, but threw " + e.getMessage(), e);
			}
		}
	}

	@Test
	public void disjunctionSamplingTest() {
	
		String model = "" +
				"random p : Boolean;" +
				"random q : Boolean;" +
				"random r : Boolean;" +
				"r = (p or q);" + // TODO: replace by equivalence, as = has a parsing issue that = has greater precedence than and
				"p = true;" +      // TODO: replace by simply "p"
				"q = false;" +
				"";
		
		String query = "r";
		Expression expected = parse("if r then 1 else 0");
		int numberOfInitialSamples = 1;
		int numberOfDiscreteValues = 21;
	
		runTest(model, query, expected, numberOfInitialSamples, numberOfDiscreteValues, /* no graph for discrete values for now */ false);
		
		query = "p";
		expected = parse("if p then 1 else 0");
		numberOfInitialSamples = 1;
		numberOfDiscreteValues = 21;
	
		runTest(model, query, expected, numberOfInitialSamples, numberOfDiscreteValues, /* no graph for discrete values for now */ false);
		
		query = "q";
		expected = parse("if q then 0 else 1");
		numberOfInitialSamples = 1;
		numberOfDiscreteValues = 21;
	
		runTest(model, query, expected, numberOfInitialSamples, numberOfDiscreteValues, /* no graph for discrete values for now */ false);
	}

	@Test
	public void disjunctionInverseSamplingTest() {
	
		String model = "" +
				"random p : Boolean;" +
				"random q : Boolean;" +
				"random r : Boolean;" +
				"r = (p or q);" + // TODO: replace by equivalence, as = has a parsing issue that = has greater precedence than and
				"r = true;" +      // TODO: replace by simply "r"
				"q = false;" +
				"";
		// p should be true by inversion in r = (p or q) when r is true and q is false
		
		String query = "r";
		Expression expected = parse("if r then 1 else 0");
		int numberOfInitialSamples = 1;
		int numberOfDiscreteValues = 21;
	
		runTest(model, query, expected, numberOfInitialSamples, numberOfDiscreteValues, /* no graph for discrete values for now */ false);
		
		query = "p";
		expected = parse("if p then 1 else 0");
		numberOfInitialSamples = 1;
		numberOfDiscreteValues = 21;
	
		runTest(model, query, expected, numberOfInitialSamples, numberOfDiscreteValues, /* no graph for discrete values for now */ false);
		
		query = "q";
		expected = parse("if q then 0 else 1");
		numberOfInitialSamples = 1;
		numberOfDiscreteValues = 21;
	
		runTest(model, query, expected, numberOfInitialSamples, numberOfDiscreteValues, /* no graph for discrete values for now */ false);
	}

	@Test
	public void disjunctionResultIsFalseIfOnlyOneArgumentIsFalseSamplingTest() {
	
		String model = "" +
				"random p : Boolean;" +
				"random q : Boolean;" +
				"random r : Boolean;" +
				"r = (p or q);" + // TODO: replace by equivalence, as = has a parsing issue that = has greater precedence than and
				"q = true;" +
				"";
		// note that p has no specified distribution
		// this is on purpose so that it does not get instantiated and allows r to be determined in absence of its information
		// However, this model will raise an error if p is to be sampled, as tested below.
		
		String query = "r";
		Expression expected = parse("if r then 1 else 0");
		int numberOfInitialSamples = 1;
		int numberOfDiscreteValues = 21;
	
		runTest(model, query, expected, numberOfInitialSamples, numberOfDiscreteValues, /* no graph for discrete values for now */ false);
		
		query = "q";
		expected = parse("if q then 1 else 0");
		numberOfInitialSamples = 1;
		numberOfDiscreteValues = 21;
	
		runTest(model, query, expected, numberOfInitialSamples, numberOfDiscreteValues, /* no graph for discrete values for now */ false);
	
		query = "p";
		expected = null; // should cause error
		numberOfInitialSamples = 1;
		numberOfDiscreteValues = 21;
	
		try {
			runTest(model, query, expected, numberOfInitialSamples, numberOfDiscreteValues, /* no graph for discrete values for now */ false);
		}
		catch (Throwable e) {
			println(e.getMessage());
			if (!Util.containsAllCaseInsensitive(e.getMessage(), "could not sample p")) {
				throw new AssertionError("Should have thrown \"Could not sample p\" error, but threw " + e.getMessage(), e);
			}
		}
	}

	

	///////////////////////////////

	@Test
	public void negationSamplingTest() {
	
		String model = "" +
				"random p : Boolean;" +
				"random q : Boolean;" +
				"p = (not true);" +
				"q = (not not p);" +
				"";
	
		String query = "q";
		Expression expected = parse("if q then 0 else 1");
		int numberOfInitialSamples = 1;
		int numberOfDiscreteValues = 21;
	
		runTest(model, query, expected, numberOfInitialSamples, numberOfDiscreteValues);

		query = "p";
		expected = parse("if p then 0 else 1");
		numberOfInitialSamples = 1;
		numberOfDiscreteValues = 21;
	
		runTest(model, query, expected, numberOfInitialSamples, numberOfDiscreteValues);
	}

	@Test
	public void negationInverseSamplingTest() {
	
		String model = "" +
				"random p : Boolean;" +
				"random q : Boolean;" +
				"q = (not not not p);" +
				"p = false;" +
				"";
		
		String query = "q";
		Expression expected = parse("if q then 1 else 0");
		int numberOfInitialSamples = 1;
		int numberOfDiscreteValues = 21;
	
		runTest(model, query, expected, numberOfInitialSamples, numberOfDiscreteValues);
		
		query = "p";
		expected = parse("if p then 0 else 1");
		numberOfInitialSamples = 1;
		numberOfDiscreteValues = 21;
	
		runTest(model, query, expected, numberOfInitialSamples, numberOfDiscreteValues);
	}


	@Test
	public void formulaPropagationSamplingTest() {
	
		String model = "" +
				"random playTennis : Boolean;" +
				"random raining : Boolean;" +
				"random partnerIsAvailable : Boolean;" +
				"random playByMyself : Boolean;" +
				"playTennis = (((partnerIsAvailable and not raining)) or playByMyself);" +
				"raining = true;" +
				"playByMyself = false;" +
				"";
		
		String query = "playTennis";
		Expression expected = parse("if playTennis then 0 else 1");
		int numberOfInitialSamples = 1;
		int numberOfDiscreteValues = 21;
	
		runTest(model, query, expected, numberOfInitialSamples, numberOfDiscreteValues);
	}

	private void runTest(String model, String query, Expression expected, int numberOfInitialSamples, int numberOfDiscreteValues) {
		runTest(model, query, expected, numberOfInitialSamples, numberOfDiscreteValues, /* generate graph */ true);
	}
	
	private void runTest(String model, String query, Expression expected, int numberOfInitialSamples, int numberOfDiscreteValues, boolean generateGraph) {
		HOGMMultiQuerySamplingProblemSolver solver = 
				new HOGMMultiQuerySamplingProblemSolver(
						model, 
						list(query),
						v -> numberOfDiscreteValues, 
						numberOfInitialSamples, 
						new Random());
		
		List<? extends HOGMProblemResult> results = solver.getResults();

		if (generateGraph) generateGraph(results);
		
		// if the number of samples is just one, the test problem should be deterministic
		// (the user is betting on getting the right answer in one shot)
		// but we still run the complete test several times to increase the changes of detecting randomness.
		boolean testProblemIsMeantToBeDeterministic = numberOfInitialSamples == 1;
		int numberOfCompletTests = testProblemIsMeantToBeDeterministic? 10 : 1;
		repeat(
				numberOfCompletTests, 
				() -> setPrecisionAndCheckResult(query, expected, results, testProblemIsMeantToBeDeterministic));
	}

	private void generateGraph(List<? extends HOGMProblemResult> results) {
		assertEquals(1, results.size());
		HOGMProblemResult result = getFirst(results);
		if (System.getProperty(PROPERTY_KEY_GENERATING_GRAPH_FILE) != null) {
			ExpressionSamplingFactor expressionSamplingFactor = (ExpressionSamplingFactor) result.getResult();
			SamplingFactorDiscretizedProbabilityDistributionFunction function = expressionSamplingFactor.getSamplingFactorDiscretizedProbabilityDistributionFunction();
			Functions functions = Functions.functions(function);
			GraphSet.plot(functions, 0, GRAPH_FILENAME_WITHOUT_EXTENSION);
		}
	}

	private void setPrecisionAndCheckResult(String query, Expression expected, List<? extends HOGMProblemResult> results, boolean deterministic) {

		int oldPrecision = ExpressoConfiguration.setDisplayNumericsMostDecimalPlacesInExactRepresentationOfNumericalSymbols(3);
		try {
			checkResult(query, expected, results, deterministic);
		} catch (Throwable t) {
			throw new Error(t);
		}
		finally {
			ExpressoConfiguration.setDisplayNumericsMostDecimalPlacesInExactRepresentationOfNumericalSymbols(oldPrecision);
		}
	}

	private void checkResult(String query, Expression expected, List<? extends HOGMProblemResult> results, boolean deterministic) {
		assertEquals(1, results.size());
		HOGMProblemResult result = getFirst(results);
		assertNoErrors(result);
		printAndCompare(query, result.getResult(), expected, deterministic);
	}

	private void assertNoErrors(HOGMProblemResult result) {
		result.getErrors().stream().forEach(e -> println(e));
		assertFalse(result.hasErrors());
	}

	private void printAndCompare(String query, Expression resultValue, Expression expected, boolean deterministic) {
		println("query: " + query);
		println("expected: " + expected);
		println("actual  : " + resultValue);
		if (resultValue instanceof ExpressionSamplingFactor && ((ExpressionSamplingFactor) resultValue).getTotalWeight() == 0.0) {
			println("All collected samples had weight equal to 0!");
			fail("All collected samples had weight equal to 0!");
		}
		String reasonForDifference = areEqualUpToNumericDifference(expected, resultValue, 0.1);
		if (reasonForDifference != "") {
			println("Failure: " + reasonForDifference);
		}
		assertEquals("", reasonForDifference);
	}
}

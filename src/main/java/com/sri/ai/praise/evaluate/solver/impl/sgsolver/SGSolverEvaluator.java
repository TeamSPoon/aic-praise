/*
 * Copyright (c) 2016, SRI International
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
package com.sri.ai.praise.evaluate.solver.impl.sgsolver;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.sri.ai.expresso.api.Expression;
import com.sri.ai.expresso.helper.Expressions;
import com.sri.ai.grinder.library.boole.Not;
import com.sri.ai.grinder.library.controlflow.IfThenElse;
import com.sri.ai.praise.evaluate.solver.SolverEvaluatorProbabilityEvidenceResult;
import com.sri.ai.praise.evaluate.solver.impl.AbstractSolverEvaluator;
import com.sri.ai.praise.lang.ModelLanguage;
import com.sri.ai.praise.model.common.io.PagedModelContainer;
import com.sri.ai.praise.sgsolver.cli.SGSolverCLI;
import com.sri.ai.util.base.Pair;
import com.sri.ai.util.math.Rational;

public class SGSolverEvaluator extends AbstractSolverEvaluator {

	@Override
	public ModelLanguage getExpectedModelLanguage() {
		return ModelLanguage.HOGMv1;
	}
	
	@Override
	public SolverEvaluatorProbabilityEvidenceResult solveProbabilityEvidence(String solveRequestId, ModelLanguage modelLanguage, String model, String evidenceQuery) 
		throws Exception {
		
		if (modelLanguage != ModelLanguage.HOGMv1) {
			throw new UnsupportedOperationException(modelLanguage.name() + " is currently not supported by this solver.");
		}
		
		SGSolverCallResult prResult = prCallSGSolverCLI(model, evidenceQuery);
		
		Rational probabilityEvidence = null; // NOTE: null indicates failure to solve/compute.
		if (prResult.resultExpression != null) {
			Expression queryExpr  = Expressions.parse(evidenceQuery);
			Expression resultExpr = Expressions.parse(prResult.resultExpression);
			if (IfThenElse.isIfThenElse(resultExpr)) {
				Expression condition = IfThenElse.condition(resultExpr);
				if (condition.equals(queryExpr)) {
					probabilityEvidence = IfThenElse.thenBranch(resultExpr).rationalValue();
				}
				else if (Not.isNegation(condition) && condition.get(0).equals(queryExpr)) {
					probabilityEvidence = IfThenElse.elseBranch(resultExpr).rationalValue();
				}
			}
			else if (resultExpr.equals(queryExpr)) {
				probabilityEvidence = Rational.ONE;
			}
			else if (Not.isNegation(resultExpr)) {
				if (resultExpr.get(0).equals(queryExpr)) {
					probabilityEvidence = Rational.ZERO;
				}
			}
			else if (Expressions.isNumber(resultExpr)) {
				probabilityEvidence = resultExpr.rationalValue();
			}
			
			if (probabilityEvidence == null) {
				throw new UnsupportedOperationException("Unable to extract result: "+resultExpr);
			}
		}
		
		SolverEvaluatorProbabilityEvidenceResult result = new SolverEvaluatorProbabilityEvidenceResult(0, prResult.sgSolverProcessTookMS, probabilityEvidence);
		return result;
	}
	
	//
	// PRIVATE
	private SGSolverCallResult prCallSGSolverCLI(String model, String evidenceQuery) throws Exception {
		
		String tempPagedModelContainer = PagedModelContainer.toInternalContainerRepresentation(ModelLanguage.HOGMv1, 
				Arrays.asList(new Pair<String, List<String>>(model, Arrays.asList(evidenceQuery))));
		
		File tempInput = File.createTempFile("sgsolver", PagedModelContainer.DEFAULT_CONTAINER_FILE_EXTENSION, getConfiguration().getWorkingDirectory());
		Files.write(tempInput.toPath(), tempPagedModelContainer.getBytes());
		//
		File tempSTDERR = File.createTempFile("sgsolver", ".stderr", getConfiguration().getWorkingDirectory());
		File tempSTDOUT = File.createTempFile("sgsolver", ".stdout", getConfiguration().getWorkingDirectory());
		
		ProcessBuilder processBuilder = new ProcessBuilder();
		processBuilder.directory(getConfiguration().getWorkingDirectory());
// TODO - add option to SGSolverCLI to indicate a timeout.		
		processBuilder.command("java", "-classpath", System.getProperty("java.class.path"), 
				"-Xms"+getConfiguration().getTotalMemoryLimitInMegabytesPerSolveAttempt()+"M",      
				"-Xmx"+getConfiguration().getTotalMemoryLimitInMegabytesPerSolveAttempt()+"M",   
				SGSolverCLI.class.getName(),
				tempInput.getAbsolutePath()
				);
		processBuilder.redirectError(ProcessBuilder.Redirect.to(tempSTDERR));
		processBuilder.redirectOutput(ProcessBuilder.Redirect.to(tempSTDOUT));
		
		long sgSolverStart = System.currentTimeMillis();
		Process sgSolverProcess = processBuilder.start();
		if (!sgSolverProcess.waitFor(getConfiguration().getTotalCPURuntimeLimitSecondsPerSolveAttempt()+5, TimeUnit.SECONDS)) {
			// waiting time elapsed
			sgSolverProcess.destroyForcibly();
		}
		long sgSolverEnd = System.currentTimeMillis();
		
		List<String> sgsolverOutputs = Files.readAllLines(tempSTDOUT.toPath(), StandardCharsets.UTF_8);
		
		tempInput.delete();
		//
		tempSTDOUT.delete();
		tempSTDERR.delete();	
		
		SGSolverCallResult result = new SGSolverCallResult();
		
		result.sgSolverProcessTookMS = sgSolverEnd - sgSolverStart;
		result.resultExpression      = sgsolverOutputs.stream().filter(line -> line.startsWith(SGSolverCLI.RESULT_PREFIX)).findFirst().orElse(null);
		if (result.resultExpression != null) {
			result.resultExpression = result.resultExpression.substring(SGSolverCLI.RESULT_PREFIX.length());
		}
		
		return result;
	}
	
	class SGSolverCallResult {
	    public long sgSolverProcessTookMS;
		public String resultExpression;
	}
}

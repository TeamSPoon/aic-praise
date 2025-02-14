/*
 * Copyright (c) 2015, SRI International
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
package com.sri.ai.praise.other.empiricalevaluation.output;

import static com.sri.ai.util.Util.toHoursMinutesAndSecondsString;

import java.io.PrintStream;

import com.sri.ai.praise.core.representation.classbased.modelscontainer.PagedModelContainer;
import com.sri.ai.praise.other.empiricalevaluation.Problem;
import com.sri.ai.praise.other.empiricalevaluation.solverevaluation.SolverEvaluationResult;

/**
 * A class encapsulating evaluation notification messages.
 * 
 * @author oreilly
 * @author braz
 *
 */
public class Notifier {	
	
	String domainSizesOfCurrentModel;
	
	private PrintStream notificationOut;

	public Notifier(PrintStream notificationOut) {
		super();
		this.notificationOut = notificationOut;
	}
	
	public void notify(String notification) {
		notificationOut.println(notification);
	}

	public void notifyAboutSolverTime(SolverEvaluationResult solverEvaluationResult) {
		String duration = toHoursMinutesAndSecondsString(solverEvaluationResult.averageInferenceTimeInMilliseconds);
		String solverName = solverEvaluationResult.solver.getName();
		String problemName = solverEvaluationResult.problem.name;
		notify("ExternalProcessSolver " + solverName + " took an average inference time of " + duration + " to solve " + problemName);
	}

	public void notifyAboutTotalEvaluationTime(long evaluationStart, long evaluationEnd) {
		String duration = toHoursMinutesAndSecondsString(evaluationEnd - evaluationStart);
		notify("Evaluation took " + duration + " to run to completion.");
	}

	public void notifyAboutBeginningOfBurnInForAllSolvers(PagedModelContainer modelsToEvaluateContainer, Problem problem) {
		String modelContainerName = modelsToEvaluateContainer.getName();
		String modelName = problem.model.getName();
		notify("Starting burn in for all solvers based on '" + modelContainerName + " - " + modelName + " : " + problem.query + "'");
	}

	public void notifyAboutBurnIn(String solverName, SolverEvaluationResult result) {
		String duration = toHoursMinutesAndSecondsString(result.averageInferenceTimeInMilliseconds);
		notify("Burn in for " + solverName + " complete. Average inference time = " + duration);
	}
}
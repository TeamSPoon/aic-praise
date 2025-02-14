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
package com.sri.ai.praise.core.inference.byinputrepresentation.interfacebased.core.exactbp.fulltime.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.sri.ai.praise.core.representation.interfacebased.factor.api.Factor;
import com.sri.ai.praise.core.representation.interfacebased.factor.api.Variable;
import com.sri.ai.util.computation.treecomputation.api.TreeComputation;

public interface ExactBPNode<RootType,SubRootType> extends TreeComputation<Factor> {
	
	SubRootType getParent();

	RootType getRoot();
	
	/**
	 * Returns the {@link Variable} over which the message coming from this algorithm is defined;
	 * effectively, this is the root if this is rooted on a variable, and the parent, if any, otherwise.
	 * @return
	 */
	Variable getMessageVariable();
	
	/**
	 * Given the product of incoming messages and factor at root,
	 * returns a list of indices being summed out at the root level,
	 * based on the overall tree computation constructed so far
	 * (this determines which indices are external cutset indices and which ones are internal ones,
	 * which in turn determines which ones must be summed out).
	 * @return
	 */
	List<? extends Variable> determinedVariablesToBeSummedOut(Collection<? extends Variable> allFreeVariablesInSummand);
	
	/**
	 * The factors residing at the root; typically the root itself if it is a factor, and an empty list otherwise.
	 */
	List<? extends Factor> getFactorsAtRoot();

	Factor sumOutWithBookkeeping(List<? extends Variable> variablesToBeSummedOut, Factor factor);

	@Override
	ArrayList<ExactBPNode<SubRootType,RootType>> getSubs();

}
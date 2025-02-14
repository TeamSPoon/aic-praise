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
package com.sri.ai.praise.core.representation.translation.ciaranframework.core.uai;

import java.util.List;

import com.google.common.annotations.Beta;
import com.sri.ai.expresso.api.Expression;
import com.sri.ai.praise.core.representation.classbased.expressionbased.core.DefaultExpressionBasedModel;
import com.sri.ai.praise.core.representation.classbased.hogm.components.HOGMSortDeclaration;
import com.sri.ai.praise.core.representation.classbased.table.api.GraphicalNetwork;
import com.sri.ai.praise.core.representation.classbased.table.core.uai.UAIUtil;

@Beta
public class UAI_to_ExpressionBased_Translator extends DefaultExpressionBasedModel {

	public UAI_to_ExpressionBased_Translator(List<Expression> tables, GraphicalNetwork network) {
		super(makeParameters(tables, network));
	}
	
	private static Parameters makeParameters(List<Expression> tables, GraphicalNetwork network) {
		Parameters parameters = new Parameters();
		parameters.factors.addAll(tables);
		for (int variableIndex = 0; variableIndex < network.numberVariables(); variableIndex++) {
			int variableCardinality = network.cardinality(variableIndex);
			String variableTypeName = UAIUtil.instanceTypeNameForVariable(variableIndex, variableCardinality);
			parameters.mapFromRandomVariableNameToTypeName.put(UAIUtil.instanceVariableName(variableIndex), variableTypeName);
			if (!variableTypeName.equals(HOGMSortDeclaration.IN_BUILT_BOOLEAN.getName().toString())) {
				for (int valueIndex = 0; valueIndex < variableCardinality; valueIndex++) {
					parameters.mapFromUniquelyNamedConstantNameToTypeName.put(
							UAIUtil.instanceConstantValueForVariable(valueIndex, variableIndex, variableCardinality), variableTypeName);
				}
			}
			parameters.mapFromCategoricalTypeNameToSizeString.put(variableTypeName, Integer.toString(variableCardinality));
		}
		return parameters;
	}
	
	
	@Override
	public UAI_to_ExpressionBased_Translator clone() {
		return (UAI_to_ExpressionBased_Translator) super.clone();
	}

	@Override
	public UAI_to_ExpressionBased_Translator getConditionedModel(Expression evidence) {
		return (UAI_to_ExpressionBased_Translator) super.getConditionedModel(evidence);
	}
}

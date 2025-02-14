package com.sri.ai.praise.learning.parameterlearning;

import java.util.List;

import com.sri.ai.praise.core.representation.interfacebased.factor.api.Factor;
import com.sri.ai.praise.core.representation.interfacebased.factor.api.Variable;

public interface BayesianNode extends Factor {
	
	public Variable getChildVariable();
	
	public List<? extends Variable> getParentsVariables();
	
	public List<? extends Variable> getAllVariables();
	
	// "Parameter" bellow is understood as the conditional probability P (childVariable = childValue | parentsVariables = parentsValues)
	
	default public void setParametersGivenCompleteData(Dataset dataset) {
		List<? extends Variable> childAndParentsVariables = this.getAllVariables();
		
		this.setInitialCountsForAllPossibleChildAndParentsAssignments();
		
		for(Datapoint datapoint : dataset.getDatapoints()) {
			List<? extends Object> childAndParentsValues = datapoint.getValuesOfVariables(childAndParentsVariables);
			this.incrementCountForChildAndParentsAssignment(childAndParentsValues);
		}
		
		this.normalizeParameters();
	}
	
	public void setInitialCountsForAllPossibleChildAndParentsAssignments();
	
	public void incrementCountForChildAndParentsAssignment(List<? extends Object> childAndParentsValues);
	
	public void normalizeParameters();
	
	public BayesianNode copy();
	
}

package com.sri.ai.praise.inference.gabrielstry.representation.core;

import static com.sri.ai.util.base.IdentityWrapper.identityWrapper;

import com.sri.ai.praise.inference.gabrielstry.representation.api.EditableFactorNetwork;
import com.sri.ai.praise.inference.representation.api.Factor;
import com.sri.ai.praise.inference.representation.api.Variable;
import com.sri.ai.praise.inference.representation.core.AbstractFactorNetwork;

public abstract class AbstractEditableFactorNetwrok extends AbstractFactorNetwork implements EditableFactorNetwork{
	
	public AbstractEditableFactorNetwrok() {
		super();
	}
	
	@Override
	public boolean containsFactor(Factor f) {
		return this.containsA(identityWrapper(f));
	}

	@Override
	public void add(Factor factor, Variable variable) {
		this.add(identityWrapper(factor),variable);
	}
}
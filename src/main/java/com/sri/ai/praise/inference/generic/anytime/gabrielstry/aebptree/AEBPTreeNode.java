package com.sri.ai.praise.inference.generic.anytime.gabrielstry.aebptree;

import com.google.common.base.Predicate;
import com.sri.ai.praise.inference.generic.anytime.polytope.api.Polytope;
import com.sri.ai.praise.inference.generic.representation.api.Factor;
import com.sri.ai.praise.inference.generic.representation.api.Variable;

public interface AEBPTreeNode<RootNode,ParentNode> {
	
	Polytope messageSent(Predicate<Polytope> boxIt);
	
	RootNode getRoot();
	AEBPTreeNode<ParentNode,RootNode> getParent();
//	List<AEBPTreeNode<ParentNode,RootNode>> getChildren();
	
	void addChild(AEBPTreeNode<ParentNode, RootNode> node);

	
	default boolean isRootAFactor() {
		return getRoot() instanceof Factor;
	}
	default boolean isRootAVariable() {
		return getRoot() instanceof Variable;
	}

}
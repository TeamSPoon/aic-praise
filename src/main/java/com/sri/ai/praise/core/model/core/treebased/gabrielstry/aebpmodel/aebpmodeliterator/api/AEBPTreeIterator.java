package com.sri.ai.praise.core.model.core.treebased.gabrielstry.aebpmodel.aebpmodeliterator.api;

import java.util.Iterator;

import com.sri.ai.praise.core.model.core.treebased.gabrielstry.aebptree.AEBPFactorTreeNode;
import com.sri.ai.praise.core.model.core.treebased.gabrielstry.aebptree.AEBPQueryTreeNode;

public interface AEBPTreeIterator extends Iterator<AEBPFactorTreeNode>{
	public AEBPQueryTreeNode getRootOfTree();
}

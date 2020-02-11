package com.sri.ai.test.praise.core.inference.byinputrepresentation.interfacebased.exact.randomtablefactornetworks;

import static com.sri.ai.praise.core.representation.interfacebased.factor.core.table.randomgeneration.tablefactornetwork.RandomTableFactorNetworkGenerator.generateRandomTableFactorNetwork;
import static com.sri.ai.util.Util.arrayList;
import static com.sri.ai.util.Util.getFirst;
import static com.sri.ai.util.base.Pair.pair;

import java.util.ArrayList;
import java.util.Random;

import org.junit.jupiter.api.Test;

import com.sri.ai.praise.core.inference.byinputrepresentation.interfacebased.exactbp.fulltime.core.ExactBPSolver;
import com.sri.ai.praise.core.inference.byinputrepresentation.interfacebased.variableelimination.VariableEliminationSolver;
import com.sri.ai.praise.core.inference.byinputrepresentation.interfacebased.variableelimination.ordering.MinFillEliminationOrdering;
import com.sri.ai.praise.core.representation.interfacebased.factor.api.Factor;
import com.sri.ai.praise.core.representation.interfacebased.factor.api.FactorNetwork;
import com.sri.ai.praise.core.representation.interfacebased.factor.api.Variable;
import com.sri.ai.praise.core.representation.interfacebased.factor.core.table.TableFactorNetwork;
import com.sri.ai.test.praise.core.inference.byinputrepresentation.interfacebased.exact.base.AbstractTestsOnBatchOfFactorNetworks;
import com.sri.ai.test.praise.core.inference.byinputrepresentation.interfacebased.exact.randomtablefactornetworks.configuration.ConfigurationForTestsOnRandomTableFactorNetworks;
import com.sri.ai.test.praise.core.inference.byinputrepresentation.interfacebased.exact.randomtablefactornetworks.configuration.LargerProblems;
import com.sri.ai.test.praise.core.inference.byinputrepresentation.interfacebased.exact.randomtablefactornetworks.configuration.SmallProblems;
import com.sri.ai.util.base.BinaryFunction;
import com.sri.ai.util.base.NullaryFunction;
import com.sri.ai.util.base.Pair;

class TestsOnRandomTableFactorNetworks extends AbstractTestsOnBatchOfFactorNetworks {

	///////////////// USER INTERFACE
	
	@Test
	void test() {
		new TestsOnRandomTableFactorNetworks().run(new SmallProblems());
	}

	public static void main(String[] args) {
		new TestsOnRandomTableFactorNetworks().run(new LargerProblems());
	}

	////////////////// ABSTRACT METHODS IMPLEMENTATION
	
	@Override
	protected ArrayList<Pair<String, BinaryFunction<Variable,FactorNetwork,Factor>>> makeAlgorithms() {
		return arrayList( 
				pair("VE_MI", new VariableEliminationSolver(new MinFillEliminationOrdering())),
				// pair("VE_DC", new VariableEliminationSolver(new DontCareEliminationOrdering())),
				pair("  EBP", new ExactBPSolver())
		);
	}

	@Override
	protected NullaryFunction<Pair<Variable, FactorNetwork>> makeProblemGenerator() {
		return () -> {
			TableFactorNetwork factorNetwork = generateRandomTableFactorNetwork(getConfiguration(), new Random());
			Variable query = getFirst(factorNetwork.getFactors()).getVariables().get(0);
			return pair(query, factorNetwork);
		};
	}

	////////////////// DETAIL
	
	@Override
	protected ConfigurationForTestsOnRandomTableFactorNetworks getConfiguration() {
		return (ConfigurationForTestsOnRandomTableFactorNetworks) super.getConfiguration();
	}
}

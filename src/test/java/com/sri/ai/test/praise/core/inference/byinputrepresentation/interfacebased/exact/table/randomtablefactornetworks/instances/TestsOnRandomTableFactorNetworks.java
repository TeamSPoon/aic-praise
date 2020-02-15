package com.sri.ai.test.praise.core.inference.byinputrepresentation.interfacebased.exact.table.randomtablefactornetworks.instances;

import static com.sri.ai.util.Util.list;
import static com.sri.ai.util.base.Pair.pair;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.sri.ai.praise.core.inference.byinputrepresentation.interfacebased.variableelimination.VariableEliminationSolver;
import com.sri.ai.praise.core.inference.byinputrepresentation.interfacebased.variableelimination.ordering.MinFillEliminationOrdering;
import com.sri.ai.praise.core.representation.interfacebased.factor.api.Factor;
import com.sri.ai.praise.core.representation.interfacebased.factor.api.FactorNetwork;
import com.sri.ai.praise.core.representation.interfacebased.factor.api.Variable;
import com.sri.ai.test.praise.core.inference.byinputrepresentation.interfacebased.exact.table.convertersolverwrapper.NDArraySolver;
import com.sri.ai.test.praise.core.inference.byinputrepresentation.interfacebased.exact.table.randomtablefactornetworks.runner.RandomTableFactorNetworksTestRunner;
import com.sri.ai.util.base.BinaryFunction;
import com.sri.ai.util.base.Pair;

class TestsOnRandomTableFactorNetworks {

	static List<Pair<String,BinaryFunction<Variable,FactorNetwork,Factor>>> algorithms =
			list( 
					pair("VE_MI_AL", new VariableEliminationSolver(new MinFillEliminationOrdering()))
					,
					pair("VE_MI_ND", new NDArraySolver(new VariableEliminationSolver(new MinFillEliminationOrdering())))
					//,
					// pair("VE_DC", new VariableEliminationSolver(new DontCareEliminationOrdering())),
					//				pair("  EBP_AL", new ArrayListSolver(new ExactBPSolver())),
					//				pair("  EBP_ND", new NDArraySolver(new ExactBPSolver()))
					);

	@Test
	void test() {
		new RandomTableFactorNetworksTestRunner<>(new SmallProblems<>(algorithms)).run();
	}

	public static void main(String[] args) {
		new RandomTableFactorNetworksTestRunner<>(new LargestProblems<>(algorithms)).run();
	}

}

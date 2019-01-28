package com.sri.ai.praise.core.representation.translation.rodrigoframework.fromrelationaltogroundhogm;

import static com.sri.ai.expresso.helper.Expressions.makeSymbol;
import static com.sri.ai.expresso.helper.Expressions.parse;
import static com.sri.ai.grinder.library.indexexpression.IndexExpressions.makeIndexExpression;
import static com.sri.ai.util.Util.in;
import static com.sri.ai.util.Util.join;
import static com.sri.ai.util.Util.list;
import static com.sri.ai.util.Util.makeNewIdentifier;
import static com.sri.ai.util.Util.mapIntoList;

import java.util.List;
import java.util.Map;

import com.sri.ai.expresso.api.Expression;
import com.sri.ai.expresso.api.IndexExpressionsSet;
import com.sri.ai.expresso.core.ExtensionalIndexExpressionsSet;
import com.sri.ai.grinder.api.Context;
import com.sri.ai.grinder.helper.AssignmentsIterator;
import com.sri.ai.praise.core.representation.classbased.expressionbased.api.ExpressionBasedProblem;
import com.sri.ai.praise.core.representation.classbased.hogm.components.HOGMExpressionBasedModel;
import com.sri.ai.praise.core.representation.classbased.hogm.components.HOGMExpressionBasedProblem;
import com.sri.ai.praise.core.representation.classbased.hogm.components.HOGMRandomVariableDeclaration;
import com.sri.ai.praise.core.representation.classbased.hogm.components.HOGMSortDeclaration;

public class FromRelationalToGroundHOGMExpressionBasedProblem {
	
	public static ExpressionBasedProblem translate(HOGMExpressionBasedProblem problem) {
		
		String modelString = getModelString(problem.getHOGMExpressionBasedModel());
		
		HOGMExpressionBasedModel model = new HOGMExpressionBasedModel(modelString);
		ExpressionBasedProblem result = new HOGMExpressionBasedProblem(parse("query"), model);
		
		return result;
	}

	public static String getModelString(HOGMExpressionBasedModel model) {
		StringBuilder groundedModelString = new StringBuilder();

		groundedModelString.append(getSorts(model));
		groundedModelString.append(getRandomVariableDeclarations(model));
		
		String modelString = groundedModelString.toString();
		
		return modelString;
	}

	private static String getSorts(HOGMExpressionBasedModel model) {
		StringBuilder result = new StringBuilder();
		for (HOGMSortDeclaration sortDeclaration : model.getHOGModel().getSortDeclarations()) {
			if (!list("Integer", "String", "Real").contains(sortDeclaration.getName().toString())) {
				result.append(sortDeclaration.toHOGMString() + "\n");
			}
		}
		return result.toString();
	}
	
	private static String getRandomVariableDeclarations(HOGMExpressionBasedModel model) {
		StringBuilder result = new StringBuilder();
		for (HOGMRandomVariableDeclaration declaration : model.getHOGModel().getRandomVariableDeclarations()) {
			result.append(grounding(declaration, model.getContext()) + "\n");
		}
		return result.toString();
	}

	private static String grounding(HOGMRandomVariableDeclaration declaration, Context context) {
		StringBuilder result = new StringBuilder();
		IndexExpressionsSet parameters = makeParameters(declaration, context);
		AssignmentsIterator parameterValues = new AssignmentsIterator(parameters, context);
		for (Map<Expression, Expression> assignment : in(parameterValues)) {
			result.append(grounding(declaration, assignment, context) + "\n");
		}
		return result.toString();
	}

	private static IndexExpressionsSet makeParameters(HOGMRandomVariableDeclaration declaration, Context context) {
		List<Expression> indexExpressions = mapIntoList(declaration.getParameterSorts(), e -> makeParameterIndexExpression(e, context));
		IndexExpressionsSet result = new ExtensionalIndexExpressionsSet(indexExpressions);
		return result;
	}
	
	private static Expression makeParameterIndexExpression(Expression sort, Context context) {
		String identifier = makeNewIdentifier("parameter", s -> !context.getSymbols().contains(makeSymbol(s)));
		Expression indexExpression = makeIndexExpression(makeSymbol(identifier), sort);
		return indexExpression;
	}

	private static String grounding(HOGMRandomVariableDeclaration declaration, Map<Expression, Expression> assignment, Context context) {
		List<String> nameComponents = list();
		nameComponents.add(declaration.getName().toString());
		if (!assignment.isEmpty()) {
			nameComponents.add("_");
		}
		mapIntoList(assignment.values(), Expression::toString, nameComponents);
		String name = join("_", nameComponents);
		Expression type = declaration.getRangeSort();
		String result = "random " + name + ": " + type + ";";
		return result;
	}

}
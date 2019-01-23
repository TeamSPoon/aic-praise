package com.sri.ai.praise.core.representation.interfacebased.factor.core.expressionsampling;

import static com.sri.ai.expresso.helper.Expressions.makeSymbol;
import static com.sri.ai.grinder.library.FunctorConstants.LESS_THAN;
import static com.sri.ai.praise.core.representation.interfacebased.factor.core.expressionsampling.FromRealExpressionVariableToRealVariableWithRange.makeRealVariableWithRange;
import static com.sri.ai.util.Util.mapIntegersIntoArrayList;
import static com.sri.ai.util.Util.mapIntegersIntoList;
import static com.sri.ai.util.Util.mapIntoArray;
import static com.sri.ai.util.Util.mapIntoArrayList;
import static com.sri.ai.util.Util.mapIntoList;
import static com.sri.ai.util.Util.myAssert;
import static com.sri.ai.util.Util.objectStringEqualsOneOf;
import static com.sri.ai.util.Util.repeat;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import com.sri.ai.expresso.api.Expression;
import com.sri.ai.expresso.api.Symbol;
import com.sri.ai.expresso.api.Type;
import com.sri.ai.expresso.helper.Expressions;
import com.sri.ai.expresso.type.Categorical;
import com.sri.ai.expresso.type.IntegerInterval;
import com.sri.ai.expresso.type.RealInterval;
import com.sri.ai.grinder.api.Context;
import com.sri.ai.grinder.library.Equality;
import com.sri.ai.grinder.library.boole.And;
import com.sri.ai.grinder.library.boole.Not;
import com.sri.ai.grinder.library.controlflow.IfThenElse;
import com.sri.ai.praise.core.representation.interfacebased.factor.core.expression.api.ExpressionVariable;
import com.sri.ai.praise.core.representation.interfacebased.factor.core.sampling.api.factor.SamplingFactor;
import com.sri.ai.praise.core.representation.translation.rodrigoframework.samplinggraph2d.SamplingFactorDiscretizedProbabilityDistributionFunction;
import com.sri.ai.util.base.Pair;
import com.sri.ai.util.collect.CartesianProductOnIntegersIterator;
import com.sri.ai.util.function.api.values.Value;
import com.sri.ai.util.function.api.variables.Assignment;
import com.sri.ai.util.function.api.variables.SetOfVariables;
import com.sri.ai.util.function.api.variables.Unit;
import com.sri.ai.util.function.api.variables.Variable;
import com.sri.ai.util.function.core.values.SetOfIntegerValues;
import com.sri.ai.util.function.core.values.SetOfRealValues;
import com.sri.ai.util.function.core.variables.DefaultAssignment;
import com.sri.ai.util.function.core.variables.DefaultSetOfVariables;
import com.sri.ai.util.function.core.variables.EnumVariable;
import com.sri.ai.util.function.core.variables.IntegerVariable;

public interface ExpressionSamplingFactor extends Expression, SamplingFactor {
	
	SamplingFactorDiscretizedProbabilityDistributionFunction getSamplingFactorDiscretizedProbabilityDistributionFunction();

	void sample();
	
	double getTotalWeight();

	/**
	 * If given expression is an instance of {@link ExpressionSamplingFactor}, samples it a given number of times.
	 * @param expression
	 * @param numberOfSamples
	 */
	public static void sample(Expression expression, int numberOfSamples) {
		if (expression instanceof ExpressionSamplingFactor) {
			repeat(numberOfSamples, () -> ((ExpressionSamplingFactor) expression).sample());
		}
	}

	public static ExpressionSamplingFactor expressionSamplingFactor(
			SamplingFactor samplingFactor, 
			int queryIndex, 
			Function<Expression, Integer> fromVariableToNumberOfDiscreteValues,
			Context context) {
		
	     return (ExpressionSamplingFactor) java.lang.reflect.Proxy.newProxyInstance(
	             samplingFactor.getClass().getClassLoader(),
	             new Class[] { ExpressionSamplingFactor.class },
	             new ExpressionSamplingFactorProxyInvocationHandler(samplingFactor, queryIndex, fromVariableToNumberOfDiscreteValues, context));		
	}

	public static class ExpressionSamplingFactorProxyInvocationHandler implements InvocationHandler {

		private SamplingFactor samplingFactor;
		private int queryIndex;
		private Function<Expression, Integer> fromVariableToNumberOfDiscreteValues;
		private Context context;
		private SamplingFactorDiscretizedProbabilityDistributionFunction samplingFactorDiscretizedProbabilityDistribution;
		
		public ExpressionSamplingFactorProxyInvocationHandler(SamplingFactor samplingFactor, int queryIndex, Function<Expression, Integer> fromVariableToNumberOfDiscreteValues, Context context) {
			this.samplingFactor = samplingFactor;
			this.queryIndex = queryIndex;
			this.fromVariableToNumberOfDiscreteValues = fromVariableToNumberOfDiscreteValues;
			this.context = context;
		}
		
		public Function<Expression, Integer> getFromVariableToNumberOfDiscreteValues() {
			return fromVariableToNumberOfDiscreteValues;
		}

		public void setFromVariableToNumberOfDiscreteValues(Function<Expression, Integer> fromVariableToNumberOfDiscreteValues) {
			this.fromVariableToNumberOfDiscreteValues = fromVariableToNumberOfDiscreteValues;
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			if (method.getDeclaringClass().isAssignableFrom(Expression.class)) {
				return method.invoke(getFactorExpression(), args);
			}
			else if (method.getDeclaringClass().isAssignableFrom(SamplingFactor.class)) {
				Object result = method.invoke(samplingFactor, args);
				return result;
			}
			else if (method.getName().equals("getSamplingFactorDiscretizedProbabilityDistributionFunction")) {
				return getSamplingFactorDiscretizedProbabilityDistributionFunction();
			}
			else if (method.getName().equals("sample")) {
				sample();
				return null;
			}
			else if (method.getName().equals("getTotalWeight")) {
				return getTotalWeight();
			}
			else {
				throw new Error(getClass() + " received method '" + method + "' of " + method.getDeclaringClass() + " which it is not prepared to execute.");
			}
		}

		public void sample() {
			getSamplingFactorDiscretizedProbabilityDistributionFunction().sample();
		}
		
		public double getTotalWeight() {
			return getSamplingFactorDiscretizedProbabilityDistributionFunction().getTotalWeight();
		}
		
		private Expression getFactorExpression() {
			List<Integer> fromVariableIndexToNumberOfValues = mapIntoList(getFunctionVariables(), v -> v.getSetOfValuesOrNull().size()); 
			Expression result = getFactorExpressionFor(new CartesianProductOnIntegersIterator(fromVariableIndexToNumberOfValues));
			return result;
		}

		private Expression getFactorExpressionFor(Iterator<ArrayList<Integer>> valueIndicesIterator) {
			Expression result;
			if (valueIndicesIterator.hasNext()) {
				result = getFactorExpressionForNonEmptyDomain(valueIndicesIterator);
			}
			else {
				result = getFactorExpressionForEmptyDomain();
			}
			return result;
		}

		private Expression getFactorExpressionForNonEmptyDomain(Iterator<ArrayList<Integer>> valueIndicesIterator) {
			Expression result;
			ArrayList<Integer> valueIndices = valueIndicesIterator.next();
			Expression probabilityExpression = getProbabilityForAssignment(valueIndices);
			if (valueIndicesIterator.hasNext()) {
				result = getFactorExpressionForNotLastAssignment(valueIndicesIterator, valueIndices, probabilityExpression);
			}
			else {
				result = probabilityExpression;
			}
			return result;
		}

		private Expression getProbabilityForAssignment(ArrayList<Integer> valueIndices) {
			Assignment assignment = getAssignmentFromValueIndices(valueIndices);
			double probability = getSamplingFactorDiscretizedProbabilityDistributionFunction().evaluate(assignment).doubleValue();
			return makeSymbol(probability);
		}

		private Expression getFactorExpressionForNotLastAssignment(
				Iterator<ArrayList<Integer>> valueIndicesIterator,
				ArrayList<Integer> valueIndices, 
				Expression probabilityExpression) {
			
			Expression condition = getConditionForAssignmentIndices(valueIndices);
			Expression remaining = getFactorExpressionFor(valueIndicesIterator);
			Expression result = IfThenElse.make(condition, probabilityExpression, remaining);
			return result;
		}

		private Expression getFactorExpressionForEmptyDomain() {
			return Expressions.ONE;
		}
		
		private Assignment getAssignmentFromValueIndices(ArrayList<Integer> valueIndices) {
			SetOfVariables setOfVariables = getSamplingFactorDiscretizedProbabilityDistributionFunction().getSetOfVariablesWithRange();
			ArrayList<Value> values = mapIntegersIntoArrayList(setOfVariables.size(), i -> getValue(setOfVariables, valueIndices, i));
			Assignment assignment = new DefaultAssignment(setOfVariables, values);
			return assignment;
		}

		private Value getValue(SetOfVariables setOfVariables, ArrayList<Integer> valueIndices, int i) {
			Variable variable = setOfVariables.get(i);
			int indexOfValue = valueIndices.get(i);
			Value value = variable.getSetOfValuesOrNull().get(indexOfValue);
			return value;
		}

		private Expression getConditionForAssignmentIndices(ArrayList<Integer> valueIndices) {
			ArrayList<? extends Variable> variables = getFunctionVariables();
			List<Expression> conjuncts = 
					mapIntegersIntoList(
							valueIndices.size(), 
							i -> getConditionForAssignment(variables.get(i), valueIndices.get(i)));
			Expression result = And.make(conjuncts);
			return result;
		}

		private Expression getConditionForAssignment(Variable variable, int valueIndex) {
			if (variable instanceof IntegerVariable || variable instanceof EnumVariable) {
				return getConditionForDiscreteVariableAssignment(variable, valueIndex);
			}
			else {
				return getConditionForRealVariableAssignment(variable, valueIndex);
			}
		}

		private Expression getConditionForDiscreteVariableAssignment(Variable variable, int valueIndex) {
			Expression result;
			Object value = variable.getSetOfValuesOrNull().get(valueIndex).objectValue();
			if (objectStringEqualsOneOf(value, "true", "false")) {
				result = getConditionForBooleanVariableAssignment(variable, value);
			}
			else {
				result = Equality.make(variable.getName(), value);
			}
			return result;
		}

		private Expression getConditionForBooleanVariableAssignment(Variable variable, Object value) {
			Expression result;
			Symbol variableExpression = Expressions.makeSymbol(variable.getName());
			if (value.toString().equals("true")) {
				result = variableExpression;
			}
			else {
				result = Not.make(variableExpression);
			}
			return result;
		}

		private Expression getConditionForRealVariableAssignment(Variable variable, int valueIndex) {
			Pair<BigDecimal, BigDecimal> boundsForIndex = ((SetOfRealValues) variable.getSetOfValuesOrNull()).getBoundsForIndex(valueIndex);
			Expression result = Expressions.apply(LESS_THAN, variable.getName(), boundsForIndex.second.doubleValue());
			return result;
		}

		public SamplingFactorDiscretizedProbabilityDistributionFunction getSamplingFactorDiscretizedProbabilityDistributionFunction() {
			if (samplingFactorDiscretizedProbabilityDistribution == null) {
				samplingFactorDiscretizedProbabilityDistribution = makeSamplingFactorDiscretizedProbabilityDistribution();
			}
			return samplingFactorDiscretizedProbabilityDistribution;
		}
		
		private SamplingFactorDiscretizedProbabilityDistributionFunction makeSamplingFactorDiscretizedProbabilityDistribution() {
			List<? extends ExpressionVariable> expressionVariables = mapIntoList(samplingFactor.getVariables(), v -> (ExpressionVariable) v);
			SetOfVariables setOfVariables = makeSetOfVariablesWithRanges(expressionVariables, fromVariableToNumberOfDiscreteValues, context);
			SamplingFactorDiscretizedProbabilityDistributionFunction result = new SamplingFactorDiscretizedProbabilityDistributionFunction(samplingFactor, setOfVariables, queryIndex);
			return result;
		}

		private ArrayList<? extends Variable> getFunctionVariables() {
			return getSamplingFactorDiscretizedProbabilityDistributionFunction().getSetOfInputVariables().getVariables();
		}

	}
	
	
	/**
	 * Prepare a {@link SetOfVariables} specifying discretization for the {@link ExpressionVariable}s 
	 * in a given a factor and a function indicating the number of discrete variables specified for each expression variable.
	 * 
	 * @param expressionVariables
	 * @param numberOfDiscreteValues
	 * @param context
	 * @return
	 */
	public static SetOfVariables makeSetOfVariablesWithRanges(
			List<? extends ExpressionVariable> expressionVariables, 
			Function<Expression, Integer> numberOfDiscreteValues,
			Context context) {
		
		ArrayList<Variable> variables = mapIntoArrayList(expressionVariables,v -> makeVariableWithRange(v, numberOfDiscreteValues.apply(v), context));
		SetOfVariables result = new DefaultSetOfVariables(variables);
		return result;

	}

	public static Variable makeVariableWithRange(ExpressionVariable expression, Integer numberOfDiscreteValues, Context context) {
		Variable result;
		String name = expression.toString();
		Type type = context.getTypeOfRegisteredSymbol(expression);
		if (type instanceof RealInterval) {
			result = makeRealVariableWithRange(name, (RealInterval) type, numberOfDiscreteValues, context);
		}
		else if (type instanceof IntegerInterval) {
			result = makeIntegerVariableWithRange(name, (IntegerInterval) type, context);
		}
		else if (type instanceof Categorical) {
			result = makeEnumVariableWithRange(name, (Categorical) type, context);
		}
		else {
			throw new Error(ExpressionSamplingFactor.class + " only supports real, integer and enum types, but got variable " + expression + " of type " + type);
		}
		return result;
	}

	public static Variable makeIntegerVariableWithRange(String name, IntegerInterval type, Context context) {
		int first = type.getNonStrictLowerBound().intValue();
		int last = type.getNonStrictUpperBound().intValue();
		SetOfIntegerValues setOfIntegerValues = new SetOfIntegerValues(first, last);
		IntegerVariable integerVariable = new IntegerVariable(name, Unit.NONE, setOfIntegerValues);
		return integerVariable;
	}

	public static Variable makeEnumVariableWithRange(String name, Categorical type, Context context) {
		myAssert(Expressions.isNumber(type.cardinality()), () -> ExpressionSamplingFactor.class + " requires categorical types to have known finite cardinality, but got " + type);
		String[] values = mapIntoArray(String.class, type.cardinality().intValue(), type.iterator(), Expression::toString);
		return new EnumVariable(name, values);
	}
}

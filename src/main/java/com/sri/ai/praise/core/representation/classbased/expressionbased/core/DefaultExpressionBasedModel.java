package com.sri.ai.praise.core.representation.classbased.expressionbased.core;

import static com.sri.ai.expresso.helper.Expressions.ONE;
import static com.sri.ai.expresso.helper.Expressions.ZERO;
import static com.sri.ai.util.Util.mapIntoSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

import com.google.common.base.Predicate;
import com.sri.ai.expresso.api.Expression;
import com.sri.ai.expresso.api.Type;
import com.sri.ai.expresso.helper.Expressions;
import com.sri.ai.grinder.api.Context;
import com.sri.ai.grinder.api.Theory;
import com.sri.ai.grinder.helper.GrinderUtil;
import com.sri.ai.grinder.helper.UniquelyNamedConstantIncludingBooleansAndNumbersPredicate;
import com.sri.ai.grinder.library.controlflow.IfThenElse;
import com.sri.ai.grinder.theory.compound.CompoundTheory;
import com.sri.ai.grinder.theory.differencearithmetic.DifferenceArithmeticTheory;
import com.sri.ai.grinder.theory.equality.EqualityTheory;
import com.sri.ai.grinder.theory.linearrealarithmetic.LinearRealArithmeticTheory;
import com.sri.ai.grinder.theory.propositional.PropositionalTheory;
import com.sri.ai.praise.core.representation.classbased.expressionbased.api.ExpressionBasedModel;
import com.sri.ai.praise.other.integration.proceduralattachment.api.ProceduralAttachments;
import com.sri.ai.praise.other.integration.proceduralattachment.core.DefaultProceduralAttachments;
import com.sri.ai.util.Util;

public class DefaultExpressionBasedModel implements ExpressionBasedModel {

	protected List<Expression> factors = new ArrayList<>();
	protected Map<String, String> mapFromRandomVariableNameToTypeName = new LinkedHashMap<>();
	protected Map<String, String> mapFromNonUniquelyNamedConstantNameToTypeName = new LinkedHashMap<>();
	protected Map<String, String> mapFromUniquelyNamedConstantNameToTypeName = new LinkedHashMap<>();
	protected Map<String, String> mapFromCategoricalTypeNameToSizeString = new LinkedHashMap<>();
	protected Collection<Type> additionalTypes = new LinkedList<>();
	protected boolean isKnownToBeBayesianNetwork = false;
	protected Theory theory = null;
	protected ProceduralAttachments proceduralAttachments = new DefaultProceduralAttachments();
	
	private List<Expression> randomVariables;

	protected static class Parameters {
		public Parameters() {
		}
		public List<Expression> factors = new ArrayList<>();
		public List<Expression> randomVariables;
		public Map<String, String> mapFromRandomVariableNameToTypeName = new LinkedHashMap<>();
		public Map<String, String> mapFromNonUniquelyNamedConstantNameToTypeName = new LinkedHashMap<>();
		public Map<String, String> mapFromUniquelyNamedConstantNameToTypeName = new LinkedHashMap<>();
		public Map<String, String> mapFromCategoricalTypeNameToSizeString = new LinkedHashMap<>();
		public Collection<Type> additionalTypes = new LinkedList<>();
		public boolean isKnownToBeBayesianNetwork = false;
		public Theory optionalTheory = null;
	}

	protected DefaultExpressionBasedModel(Parameters parameters) {
		this(
				parameters.factors,
				parameters.mapFromRandomVariableNameToTypeName,
				parameters.mapFromNonUniquelyNamedConstantNameToTypeName,
				parameters.mapFromUniquelyNamedConstantNameToTypeName,
				parameters.mapFromCategoricalTypeNameToSizeString,
				parameters.additionalTypes,
				parameters.isKnownToBeBayesianNetwork,
				parameters.optionalTheory
				);
	}
	
	public DefaultExpressionBasedModel(
			List<? extends Expression> factors,
			Map<String, String> mapFromRandomVariableNameToTypeName,
			Map<String, String> mapFromNonUniquelyNamedConstantNameToTypeName,
			Map<String, String> mapFromUniquelyNamedConstantNameToTypeName,
			Map<String, String> mapFromCategoricalTypeNameToSizeString,
			Collection<Type> additionalTypes,
			boolean isKnownToBeBayesianNetwork
			) {
		this(
				factors,
				mapFromRandomVariableNameToTypeName,
				mapFromNonUniquelyNamedConstantNameToTypeName,
				mapFromUniquelyNamedConstantNameToTypeName,
				mapFromCategoricalTypeNameToSizeString,
				additionalTypes,
				isKnownToBeBayesianNetwork,
				null
				);
	}
	
	public DefaultExpressionBasedModel(
			List<? extends Expression> factors,
			Map<String, String> mapFromRandomVariableNameToTypeName,
			Map<String, String> mapFromNonUniquelyNamedConstantNameToTypeName,
			Map<String, String> mapFromUniquelyNamedConstantNameToTypeName,
			Map<String, String> mapFromCategoricalTypeNameToSizeString,
			Collection<Type> additionalTypes,
			boolean isKnownToBeBayesianNetwork,
			Theory optionalTheory
			) {
		
		this.factors.addAll(factors);
		this.mapFromRandomVariableNameToTypeName.putAll(mapFromRandomVariableNameToTypeName);
		this.mapFromNonUniquelyNamedConstantNameToTypeName.putAll(mapFromNonUniquelyNamedConstantNameToTypeName);
		this.mapFromUniquelyNamedConstantNameToTypeName.putAll(mapFromUniquelyNamedConstantNameToTypeName);
		this.mapFromCategoricalTypeNameToSizeString.putAll(mapFromCategoricalTypeNameToSizeString);
		this.additionalTypes = additionalTypes;
		this.isKnownToBeBayesianNetwork = isKnownToBeBayesianNetwork;
		this.theory = optionalTheory;
		
		this.randomVariables = Util.mapIntoList(getMapFromRandomVariableNameToTypeName().keySet(), Expressions::parse);
		this.theory = getTheoryToBeUsed(optionalTheory);
	}	

	private Theory getTheoryToBeUsed(Theory optionalTheory) {
		Theory theory;
		if (optionalTheory != null) {
			theory = optionalTheory;
		}
		else {
			theory =
					new CompoundTheory(
							new EqualityTheory(false, true),
							new DifferenceArithmeticTheory(false, true),
							new LinearRealArithmeticTheory(false, true),
							new PropositionalTheory());
			//theory = new CompoundTheory(new PropositionalTheory(), new LinearRealArithmeticTheory(false, false)); // TODO: temporary hack for July 2018 demo because DifferenceArithmeticTheory is interfering
		}
		return theory;
	}

	private Context context = null;

	@Override
	public Context getContext() {
		if (context == null) {
			context = makeContext();
		}
		return context;
	}

	private Context makeContext() {
		Map<String, String> mapFromSymbolNameToTypeName;
		Map<String, String> mapFromCategoricalTypeNameToSizeString;
		Collection<Type> additionalTypes;
		Predicate<Expression> isUniquelyNamedConstantPredicate;
	
		mapFromSymbolNameToTypeName = new LinkedHashMap<>(getMapFromRandomVariableNameToTypeName());
		mapFromSymbolNameToTypeName.putAll(getMapFromNonUniquelyNamedConstantNameToTypeName());
		mapFromSymbolNameToTypeName.putAll(getMapFromUniquelyNamedConstantNameToTypeName());
	
		mapFromCategoricalTypeNameToSizeString = new LinkedHashMap<>(getMapFromCategoricalTypeNameToSizeString());
	
		Set<Expression> uniquelyNamedConstants = mapIntoSet(getMapFromUniquelyNamedConstantNameToTypeName().keySet(), Expressions::parse);
		isUniquelyNamedConstantPredicate = new UniquelyNamedConstantIncludingBooleansAndNumbersPredicate(uniquelyNamedConstants);
	
		additionalTypes = new LinkedList<Type>(getTheory().getNativeTypes()); // add needed types that may not be the type of any variable
		additionalTypes.addAll(getAdditionalTypes());
	
		Context contextWithQuery = GrinderUtil.makeContext(mapFromSymbolNameToTypeName, mapFromCategoricalTypeNameToSizeString, additionalTypes, isUniquelyNamedConstantPredicate, getTheory());
		
		return contextWithQuery;
	}

	@Override
	public List<Expression> getFactors() {
		return Collections.unmodifiableList(factors);
	}

	@Override
	public List<Expression> getRandomVariables() {
		return Collections.unmodifiableList(randomVariables);
	}

	@Override
	public DefaultExpressionBasedModel getConditionedModel(Expression evidence) {
		DefaultExpressionBasedModel result;
		if (evidence != null && !Expressions.isNumber(evidence)) {
			result = clone();
			result.factors = new LinkedList<Expression>(factors);
			result.factors.add(IfThenElse.make(evidence, ONE, ZERO));
			result.isKnownToBeBayesianNetwork = false;
		}
		else {
			result = this;
		}
		return result;
	}
	
	@Override
	public Map<String, String> getMapFromRandomVariableNameToTypeName() {
		return Collections.unmodifiableMap(mapFromRandomVariableNameToTypeName);
	}

	@Override
	public Map<String, String> getMapFromNonUniquelyNamedConstantNameToTypeName() {
		return Collections.unmodifiableMap(mapFromNonUniquelyNamedConstantNameToTypeName);
	}

	@Override
	public Map<String, String> getMapFromUniquelyNamedConstantNameToTypeName() {
		return Collections.unmodifiableMap(mapFromUniquelyNamedConstantNameToTypeName);
	}

	@Override
	public Map<String, String> getMapFromCategoricalTypeNameToSizeString() {
		return Collections.unmodifiableMap(mapFromCategoricalTypeNameToSizeString);
	}

	@Override
	public Collection<Type> getAdditionalTypes() {
		return Collections.unmodifiableCollection(additionalTypes);
	}
	
	@Override
	public boolean isKnownToBeBayesianNetwork() {
		return isKnownToBeBayesianNetwork;
	}

	@Override
	public Theory getTheory() {
		return theory;
	}

	@Override
	public void setTheory(Theory newTheory) {
		theory = newTheory;
		context = null;
	}


	@Override
	public ProceduralAttachments getProceduralAttachments() {
		return proceduralAttachments;
	}

	@Override
	public void setProceduralAttachments(ProceduralAttachments proceduralAttachments) {
		this.proceduralAttachments = proceduralAttachments;
	}
	
	@Override
	public String toString() {
		StringJoiner stringJoiner = new StringJoiner("\n");
		
		stringJoiner.add("factors                                       = " + factors);
		stringJoiner.add("mapFromRandomVariableNameToTypeName           = " + mapFromRandomVariableNameToTypeName);
		stringJoiner.add("mapFromNonUniquelyNamedConstantNameToTypeName = " + mapFromNonUniquelyNamedConstantNameToTypeName);
		stringJoiner.add("mapFromUniquelyNamedConstantNameToTypeName    = " + mapFromUniquelyNamedConstantNameToTypeName);
		stringJoiner.add("mapFromCategoricalTypeNameToSizeString        = " + mapFromCategoricalTypeNameToSizeString);
		stringJoiner.add("additionalTypes                               = " + additionalTypes);
		stringJoiner.add("isKnownToBeBayesianNetwork                    = " + isKnownToBeBayesianNetwork);
		stringJoiner.add("theory                                        = " + theory);
		
		return stringJoiner.toString();
	}
	
	@Override
	public DefaultExpressionBasedModel clone() {
		DefaultExpressionBasedModel result;
		try {
			result = (DefaultExpressionBasedModel) super.clone();
		}
		catch (CloneNotSupportedException exception) {
			throw new RuntimeException(exception);
		}
		return result;
	}
}
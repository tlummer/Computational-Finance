/*
 * (c) Copyright Christian P. Fries, Germany. Contact: email@christian-fries.de.
 *
 * Created on 21.01.2004
 */
package com.timlummer.bonus;

import net.finmath.exception.CalculationException;
import net.finmath.montecarlo.RandomVariable;
import net.finmath.montecarlo.assetderivativevaluation.AssetModelMonteCarloSimulationInterface;
import net.finmath.montecarlo.assetderivativevaluation.products.AbstractAssetMonteCarloProduct;
import net.finmath.stochastic.RandomVariableInterface;

/**
 * Implements the valuation of a European option on a single asset.
 * 
 * Given a model for an asset <i>S</i>, the European option with strike <i>K</i>, maturity <i>T</i>
 * pays
 * <br>
 * 	<i>V(T) = max(S(T) - K , 0)</i> in <i>T</i>.
 * <br>
 * 
 * The <code>getValue</code> method of this class will return the random variable <i>N(t) * V(T) / N(T)</i>,
 * where <i>N</i> is the numeraire provided by the model. If <i>N(t)</i> is deterministic,
 * calling <code>getAverage</code> on this random variable will result in the value. Otherwise a
 * conditional expectation has to be applied.
 * 
 * @author Christian Fries
 * @version 1.3
 */
public class BonusOption extends AbstractAssetMonteCarloProduct {

	private final double maturity;
	private final double bonus;
	private final double barrier;
	private final Integer underlyingIndex;
	private final String nameOfUnderliyng;

	/**
	 * Construct a product representing an European option on an asset S (where S the asset with index 0 from the model - single asset case).
	 * @param maturity The maturity T in the option payoff max(S(T)-K,0)
	 * @param strike The strike K in the option payoff max(S(T)-K,0).
	 * @param underlyingIndex The index of the underlying to be fetched from the model.
	 */
	public BonusOption(double maturity, double bonus, double barrier, int underlyingIndex) {
		super();
		this.barrier			= barrier;
		this.maturity			= maturity;
		this.bonus				= bonus;
		this.underlyingIndex	= underlyingIndex;
		this.nameOfUnderliyng	= null;		// Use underlyingIndex
	}

	/**
	 * Construct a product representing an European option on an asset S (where S the asset with index 0 from the model - single asset case).
	 * @param maturity The maturity T in the option payoff max(S(T)-K,0)
	 * @param strike The strike K in the option payoff max(S(T)-K,0).
	 */
	public BonusOption(double maturity, double barrier, double bonus) {
		this(maturity, bonus, barrier, 0);
	}

	/**
	 * This method returns the value random variable of the product within the specified model, evaluated at a given evalutationTime.
	 * Note: For a lattice this is often the value conditional to evalutationTime, for a Monte-Carlo simulation this is the (sum of) value discounted to evaluation time.
	 * Cashflows prior evaluationTime are not considered.
	 * 
	 * @param evaluationTime The time on which this products value should be observed.
	 * @param model The model used to price the product.
	 * @return The random variable representing the value of the product discounted to evaluation time
	 * @throws net.finmath.exception.CalculationException Thrown if the valuation fails, specific cause may be available via the <code>cause()</code> method.
	 */
	@Override
	public RandomVariableInterface getValue(double evaluationTime, AssetModelMonteCarloSimulationInterface model) throws CalculationException {
		// Get underlying and numeraire
		
		 // Get value of underlying and numeraire assets		
		RandomVariableInterface underlyingAtTimeIndex = model.getAssetValue(maturity,0);
	    RandomVariableInterface underlyingAtInitial  = model.getAssetValue(0.0,0);
	    

	    
		/*
		 * At maturity, calculate the value of the replication portfolio
		 */
		double[] portfolioValue				= new double[model.getNumberOfPaths()];

		// The payoff: values = max(underlying - strike, 0) = V(T) = max(S(T)-K,0)
		for(int path=0; path<model.getNumberOfPaths(); path++)
		{
			double underlyingValue	= underlyingAtTimeIndex.get(path);
			
			double underlyingValueatStart = underlyingAtInitial.get(path);
			
			if(underlyingValue > barrier) {
				portfolioValue[path] = underlyingValueatStart * (1+ bonus);
			}else {
			
				portfolioValue[path] = underlyingValue ;
								
			}
		}
		
		RandomVariableInterface values = new RandomVariable(maturity,portfolioValue);

		// Discounting...
		RandomVariableInterface numeraireAtMaturity		= model.getNumeraire(maturity);
		RandomVariableInterface monteCarloWeights		= model.getMonteCarloWeights(maturity);
		values = values.div(numeraireAtMaturity).mult(monteCarloWeights);

		// ...to evaluation time.
		RandomVariableInterface	numeraireAtEvalTime					= model.getNumeraire(evaluationTime);
		RandomVariableInterface	monteCarloProbabilitiesAtEvalTime	= model.getMonteCarloWeights(evaluationTime);
		values = values.mult(numeraireAtEvalTime).div(monteCarloProbabilitiesAtEvalTime);

		return values;
	}
}

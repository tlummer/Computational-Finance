/*
 * (c) Copyright Christian P. Fries, Germany. Contact: email@christian-fries.de.
 *
 * Created on 23.01.2004
 */
package com.timlummer.bonus;

import java.util.ArrayList;
import java.util.Arrays;

import net.finmath.exception.CalculationException;
import net.finmath.montecarlo.RandomVariable;
import net.finmath.montecarlo.RandomVariableFactory;
import net.finmath.montecarlo.assetderivativevaluation.AssetModelMonteCarloSimulationInterface;
import net.finmath.montecarlo.assetderivativevaluation.products.AbstractAssetMonteCarloProduct;
import net.finmath.montecarlo.conditionalexpectation.MonteCarloConditionalExpectationRegression;
import net.finmath.optimizer.GoldenSectionSearch;
import net.finmath.stochastic.ConditionalExpectationEstimatorInterface;
import net.finmath.stochastic.RandomVariableInterface;

/**
 * This class implements the valuation of a Bermudan option paying
 * <br>
 * <i>	N(i) * (S(T(i)) - K(i))	</i> 	at <i>T(i)</i>,
 * <br>
 * when exercised in T(i), where N(i) is the notional, S is the underlying, K(i) is the strike
 * and T(i) the exercise date.
 * 
 * The code "demos" the two prominent methods for the valuation of Bermudan (American) products:
 * <ul>
 * 	<li>
 * 		The valuation may be performed using an estimation of the conditional expectation to determine the
 * 		exercise criteria. Apart from a possible foresight bias induced by the Monte-Carlo errors, this give a lower bound
 *		for the Bermudan value.
 * 	<li>
 * 		The valuation may be performed using the dual method based on a minimization problem, which gives an upper bound.
 * </ul>
 * 
 * 
 * @author Christian Fries
 * @version 1.4
 */
public class MemoryExpress extends AbstractAssetMonteCarloProduct {


	private final double[]	exerciseDates;
	private final double coupon;
	private final double strike;
	private final double initialValue;
	private final int underlyingIndex;



	private RandomVariableInterface lastValuationExerciseTime;

	/**
	 * Create a Bermudan option paying
	 * N(i) * (S(T(i)) - K(i)) at T(i),
	 * when exercised in T(i), where N(i) is the notional, S is the underlying, K(i) is the strike
	 * and T(i) the exercise date.
	 * 
	 * @param exerciseDates The exercise dates (T(i)), given as doubles.
	 * @param notionals The notionals (N(i)) for each exercise date.
	 * @param strikes The strikes (K(i)) for each exercise date.
	 * @param exerciseMethod The exercise method to be used for the estimation of the exercise boundary.
	 */
	public MemoryExpress(
			double[] exerciseDates,
			double initialValue,
			double coupon,
			double strike,
			int underlyingIndex) {
		super();
		this.exerciseDates = exerciseDates;
		this.strike = strike;
		this.coupon = coupon;
		this.underlyingIndex	= underlyingIndex;
		this.initialValue = initialValue;
	}
	
	/**
	 * This method returns the value random variable of the product within the specified model,
	 * evaluated at a given evalutationTime.
	 * Cash-flows prior evaluationTime are not considered.
	 * 
	 * @param evaluationTime The time on which this products value should be observed.
	 * @param model The model used to price the product.
	 * @return The random variable representing the value of the product discounted to evaluation time.
	 * @throws net.finmath.exception.CalculationException Thrown if the valuation fails, specific cause may be available via the <code>cause()</code> method.
	 * 
	 */
	@Override
	public RandomVariableInterface getValue(double evaluationTime, AssetModelMonteCarloSimulationInterface model) throws CalculationException {
		// Get underlying and numeraire
		
		RandomVariableInterface values = model.getAssetValue(0.0, underlyingIndex).mult(0.0);
		RandomVariableInterface memory = model.getAssetValue(0.0, underlyingIndex).mult(0.0);
		RandomVariableInterface exercised = model.getAssetValue(0.0, underlyingIndex).mult(0.0);
		
		for(double evaltime : exerciseDates) {
		
			
		RandomVariableInterface valueatTimet = model.getAssetValue(0.0, underlyingIndex).mult(0.0);
		// Get S(t_i)

		RandomVariableInterface underlyingAtMaturity = model.getAssetValue(evaltime,underlyingIndex);

		
		RandomVariableInterface eligableforMemory = underlyingAtMaturity.apply(x ->  x > strike ? 1  : 0);
		
		//get payoffs
		 RandomVariableInterface payoffs = underlyingAtMaturity.apply(x ->  x > strike & x < initialValue ? coupon  : 0).add(eligableforMemory.mult(memory));
		 payoffs = payoffs.add(underlyingAtMaturity.apply(x ->  x > initialValue ? coupon + 1 : 0).add(eligableforMemory.mult(memory)));
		 
		 exercised = exercised.add(underlyingAtMaturity.apply(x ->  x > initialValue ? 1 : 0));
		 
		 //Add to values
		 valueatTimet = payoffs.mult(initialValue).mult(exercised.apply(x ->  x > 0 ? 0 : 1));
		 
		
		//reset memory
		memory = memory.mult(eligableforMemory.apply(x ->  x > 0 ? 0 : 1));
		
		
		//add to memory	if not payed
		memory = memory.add(underlyingAtMaturity.apply(x ->  x < strike ? coupon : 0.0));
		
		
		
		
		//System.out.println("AVG payoff at t "+ evaltime + " is " + valueatTimet.getAverage());
		System.out.println("AVG memory at t "+ evaltime + " is " +  model.getAssetValue(evaltime,underlyingIndex).getAverage() );
		System.out.println("AVG understrike at t "+ evaltime +  " is " + exercised.apply(x -> (x > strike && x < initialValue )? 1 : 0).getAverage() );
		//System.out.println("AVG eligable at t "+ evaltime +  " is " + eligableforMemory.getAverage() );
		
		// Discounting...
		RandomVariableInterface numeraireAtMaturity		= model.getNumeraire(evaltime);
		RandomVariableInterface monteCarloWeights		= model.getMonteCarloWeights(evaltime);
		valueatTimet = valueatTimet.div(numeraireAtMaturity).mult(monteCarloWeights);

		// ...to evaluation time.
		RandomVariableInterface	numeraireAtEvalTime					= model.getNumeraire(evaluationTime);
		RandomVariableInterface	monteCarloProbabilitiesAtEvalTime	= model.getMonteCarloWeights(evaluationTime);
		
		valueatTimet = valueatTimet.mult(numeraireAtEvalTime).div(monteCarloProbabilitiesAtEvalTime);
		
		values = values.add(valueatTimet);
		
		}
		
		return values;
}	
	}


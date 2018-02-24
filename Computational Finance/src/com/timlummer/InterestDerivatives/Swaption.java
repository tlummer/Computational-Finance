/*
 * (c) Copyright Christian P. Fries, Germany. Contact: email@christian-fries.de.
 *
 * Created on 15.02.2004
 */
package com.timlummer.InterestDerivatives;

import java.util.Arrays;

import net.finmath.exception.CalculationException;
import net.finmath.functions.AnalyticFormulas;
import net.finmath.marketdata.model.AnalyticModelInterface;
import net.finmath.marketdata.model.curves.DiscountCurveFromForwardCurve;
import net.finmath.marketdata.model.curves.DiscountCurveInterface;
import net.finmath.marketdata.model.curves.ForwardCurveInterface;
import net.finmath.marketdata.products.Swap;
import net.finmath.marketdata.products.SwapAnnuity;
import net.finmath.montecarlo.interestrate.LIBORModelMonteCarloSimulationInterface;
import net.finmath.montecarlo.interestrate.products.AbstractLIBORMonteCarloProduct;
import net.finmath.stochastic.RandomVariableInterface;
import net.finmath.time.TimeDiscretization;
import net.finmath.time.TimeDiscretizationInterface;

/**
 * Implements the valuation of a swaption under a LIBORModelMonteCarloSimulationInterface
 * 
 * Important: If the LIBOR Market Model is a multi-curve model in the sense that the
 * numeraire is not calculated from the forward curve, then this valuation does
 * assume that the basis deterministic. For the valuation of a fully generalize swaption,
 * you have to use the <code>Option</code> component on a <code>Swap</code>.
 * 
 * @author Christian Fries
 * @version 1.3
 */
public class Swaption extends AbstractLIBORMonteCarloProduct {
	private double     PeriodStart;	// Exercise date
	private double     PeriodEnd;	// Vector of payment dates (same length as fixing dates)
	private double     swaprate;		// Vector of strikes


	/**
	 * Create a swaption.
	 * 
	 * @param exerciseDate Vector of exercise dates.
	 * @param fixingDates Vector of fixing dates.
	 * @param paymentDates Vector of payment dates (must have same length as fixing dates).
	 * @param swaprates Vector of strikes (must have same length as fixing dates).
	 */
	public Swaption(
			double PeriodStart,
			double PeriodEnd,
			double swaprate) {
	super();
	
	this.PeriodEnd=PeriodEnd;
	this.PeriodStart=PeriodStart;
	this.swaprate=swaprate;
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
	public RandomVariableInterface getValue(double evaluationTime, LIBORModelMonteCarloSimulationInterface model) throws CalculationException {
		/*
		 * Calculate value of the swap at exercise date on each path (beware of perfect foresight - all rates are simulationTime=exerciseDate)
		 */
		
		double periodLength	= PeriodEnd - PeriodStart;

		// Get random variables - note that this is the rate at simulation time = exerciseDate
		RandomVariableInterface libor	= model.getLIBOR(PeriodStart, PeriodStart, PeriodEnd);

		// Calculate payoff
		RandomVariableInterface values = libor.sub(swaprate).mult(periodLength).floor(0.0);

		RandomVariableInterface	numeraire				= model.getNumeraire(PeriodEnd);
		RandomVariableInterface	monteCarloProbabilities	= model.getMonteCarloWeights(PeriodEnd);
		values = values.div(numeraire).mult(monteCarloProbabilities);

		RandomVariableInterface	numeraireAtZero					= model.getNumeraire(evaluationTime);
		RandomVariableInterface	monteCarloProbabilitiesAtZero	= model.getMonteCarloWeights(evaluationTime);
		values = values.mult(numeraireAtZero).div(monteCarloProbabilitiesAtZero);

		return values;
	}
}

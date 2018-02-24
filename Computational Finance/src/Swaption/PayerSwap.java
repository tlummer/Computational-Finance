/*
 * (c) Copyright Christian P. Fries, Germany. Contact: email@christian-fries.de.
 *
 * Created on 07.11.2015
 */

package Swaption;

import net.finmath.exception.CalculationException;
import net.finmath.montecarlo.interestrate.LIBORModelMonteCarloSimulationInterface;
import net.finmath.montecarlo.interestrate.products.AbstractLIBORMonteCarloProduct;
import net.finmath.stochastic.RandomVariableInterface;

/**
 * @author Christian Fries
 *
 */
public class PayerSwap extends AbstractLIBORMonteCarloProduct {

	private final double[]	fixingDates;	// Vector of fixing dates
	private final double[]	SwapRates;	// Vector of SwapRates
	private final double[]	paymentDates;	// Vector of payment dates (same length as fixing dates)
	private final double 	notional;

	public PayerSwap(double[]	SwapRates, double[] fixingDates, double[] paymentDates, double notional) {
		
		super();
		this.SwapRates = SwapRates;
		this.fixingDates = fixingDates;
		this.paymentDates = paymentDates;
		this.notional=notional;
	
	}

	@Override
	public RandomVariableInterface getValue(double evaluationTime, LIBORModelMonteCarloSimulationInterface model) throws CalculationException {

		// Accumulating values in the random variable
		RandomVariableInterface value = model.getRandomVariableForConstant(0.0);

		for(int periodIndex=0; periodIndex<fixingDates.length; periodIndex++) {
			double fixingDate = fixingDates[periodIndex];
			double paymentDate = paymentDates[periodIndex];
			double periodLength = paymentDate-fixingDate;

			// Get floating rate for coupon
			RandomVariableInterface libor = model.getLIBOR(fixingDate, fixingDate, paymentDate);
			RandomVariableInterface SwapRate = model.getRandomVariableForConstant(SwapRates[periodIndex]);

			RandomVariableInterface periodPayoff = libor.sub(SwapRate).mult(periodLength).mult(notional); //.mult(-1.0,) for ReceiverSwap
			
			RandomVariableInterface numeraire = model.getNumeraire(paymentDate);
			RandomVariableInterface monteCarloProbabilities	= model.getMonteCarloWeights(paymentDate);

			value = value.add(periodPayoff.div(numeraire).mult(monteCarloProbabilities)); 
		}

		RandomVariableInterface	numeraireAtEvalTime					= model.getNumeraire(evaluationTime);
		RandomVariableInterface	monteCarloProbabilitiesAtEvalTime	= model.getMonteCarloWeights(evaluationTime);
		value = value.mult(numeraireAtEvalTime).div(monteCarloProbabilitiesAtEvalTime);

		return value;
	}
}

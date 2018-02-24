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
public class Floater extends AbstractLIBORMonteCarloProduct {

	private final double[]	fixingDates;	// Vector of fixing dates
	private final double[]	paymentDates;	// Vector of payment dates (same length as fixing dates)
	private final double 	notional;

	public Floater(double[] fixingDates, double[] paymentDates, double notional) {
		
		super();
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
			RandomVariableInterface coupon = model.getLIBOR(fixingDate, fixingDate, paymentDate);

			coupon = coupon.mult(periodLength).mult(notional);

			RandomVariableInterface numeraire = model.getNumeraire(paymentDate);
			RandomVariableInterface monteCarloProbabilities	= model.getMonteCarloWeights(paymentDate);

			value = value.add(coupon.div(numeraire).mult(monteCarloProbabilities));
		}

		RandomVariableInterface	numeraireAtEvalTime					= model.getNumeraire(evaluationTime);
		RandomVariableInterface	monteCarloProbabilitiesAtEvalTime	= model.getMonteCarloWeights(evaluationTime);
		value = value.mult(numeraireAtEvalTime).div(monteCarloProbabilitiesAtEvalTime);

		return value;
	}
}

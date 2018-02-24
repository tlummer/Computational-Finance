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
public class FloaterBond extends AbstractLIBORMonteCarloProduct {

	private final double[]	fixingDates;	// Vector of fixing dates
	private final double[]	paymentDates;	// Vector of payment dates (same length as fixing dates)
	private final double	maturity;
	private final double 	notional;

	public FloaterBond(double[] fixingDates, double[] paymentDates,double maturity, double notional) {
		
		super();
		this.fixingDates = fixingDates;
		this.paymentDates = paymentDates;
		this.maturity = maturity;
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

		// Add unit notional payment at maturity
		RandomVariableInterface notionalPayoff = model.getRandomVariableForConstant(notional);
		RandomVariableInterface numeraire = model.getNumeraire(maturity);
		RandomVariableInterface monteCarloProbabilities	= model.getMonteCarloWeights(maturity);
		value = value.add(notionalPayoff.div(numeraire).mult(monteCarloProbabilities));

		RandomVariableInterface	numeraireAtEvalTime					= model.getNumeraire(evaluationTime);
		RandomVariableInterface	monteCarloProbabilitiesAtEvalTime	= model.getMonteCarloWeights(evaluationTime);
		value = value.mult(numeraireAtEvalTime).div(monteCarloProbabilitiesAtEvalTime);

		return value;
	}
}

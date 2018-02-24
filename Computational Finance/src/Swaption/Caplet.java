/*
 * (c) Copyright Christian P. Fries, Germany. Contact: email@christian-fries.de.
 *
 * Created on 09.02.2004
 */
package Swaption;

import net.finmath.exception.CalculationException;
import net.finmath.functions.AnalyticFormulas;
import net.finmath.montecarlo.interestrate.LIBORModelMonteCarloSimulationInterface;
import net.finmath.montecarlo.interestrate.products.AbstractLIBORMonteCarloProduct;
import net.finmath.stochastic.RandomVariableInterface;

/**
 * Implements the pricing of a Caplet using a given <code>AbstractLIBORMarketModel</code>.
 * 
 * @author Christian Fries
 * @version 1.0
 */
public class Caplet extends AbstractLIBORMonteCarloProduct {

	private final double	periodStart;
	private final double	periodEnd;
	private final double	strike;
	private final boolean	isFloorlet;

	/**
	 * Create a caplet or a floorlet.
	 * 
	 * A caplet pays \( max(L-K,0) * daycountFraction \) at maturity+periodLength
	 * where L is fixed at maturity.
	 * 
	 * A floorlet pays \( -min(L-K,0) * daycountFraction \) at maturity+periodLength
	 * where L is fixed at maturity.
	 * 
	 * @param periodStart The fixing date given as double. The payment is at the period end.
	 * @param periodEnd The length of the forward rate period.
	 * @param strike The strike given as double.
	 * @param daycountFraction The daycount fraction used in the payout function.
	 * @param isFloorlet If true, this object will represent a floorlet, otherwise a caplet.
	 * @param valueUnit The unit of the value returned by the <code>getValue</code> method.
	 */
	public Caplet(double periodStart, double periodEnd, double strike, boolean isFloorlet) {
		super();
		this.periodStart = periodStart;
		this.periodEnd = periodEnd;
		this.strike = strike;
		this.isFloorlet = isFloorlet;
	}
	
	/**
	 * Create a caplet or a floorlet.
	 * 
	 * A caplet pays \( max(L-K,0) * daycountFraction \) at maturity+periodLength
	 * where L is fixed at maturity.
	 * 
	 * A floorlet pays \( -min(L-K,0) * daycountFraction \) at maturity+periodLength
	 * where L is fixed at maturity.
	 * 
	 * This simplified constructor uses daycountFraction = periodLength.
	 * 
	 * @param maturity The fixing date given as double. The payment is at the period end.
	 * @param periodLength The length of the forward rate period in ACT/365 convention.
	 * @param strike The strike given as double.
	 * @param isFloorlet If true, this object will represent a floorlet, otherwise a caplet.
	 */
	
	@Override
	public RandomVariableInterface getValue(double evaluationTime, LIBORModelMonteCarloSimulationInterface model) throws CalculationException {        
		// This is on the LIBOR discretization
		double	periodLength	= periodEnd-periodStart;
				
		// Get random variables
		RandomVariableInterface	libor					= model.getLIBOR(periodStart, periodStart, periodEnd);
		RandomVariableInterface	numeraire				= model.getNumeraire(periodEnd);
		RandomVariableInterface	monteCarloProbabilities	= model.getMonteCarloWeights(periodEnd);
	
		/*
		 * Calculate the payoff, which is
		 *    max(L-K,0) * periodLength         for caplet or
		 *   -min(L-K,0) * periodLength         for floorlet.
		 */
		RandomVariableInterface values = libor;		
		if(!isFloorlet)	values = values.sub(strike).floor(0.0).mult(periodLength);
		else			values = values.sub(strike).cap(0.0).mult(-1.0 * periodLength);

		values = values.div(numeraire).mult(monteCarloProbabilities);

		RandomVariableInterface	numeraireAtValuationTime				= model.getNumeraire(evaluationTime);		
		RandomVariableInterface	monteCarloProbabilitiesAtValuationTime	= model.getMonteCarloWeights(evaluationTime);		
		values = values.mult(numeraireAtValuationTime).div(monteCarloProbabilitiesAtValuationTime);


		return values;
	}
}

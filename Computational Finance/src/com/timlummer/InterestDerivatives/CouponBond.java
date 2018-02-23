/*
 * (c) Copyright Christian P. Fries, Germany. Contact: email@christian-fries.de.
 *
 * Created on 10.02.2004
 */
package com.timlummer.InterestDerivatives;

import net.finmath.exception.CalculationException;
import net.finmath.montecarlo.interestrate.LIBORModelMonteCarloSimulationInterface;
import net.finmath.montecarlo.interestrate.products.AbstractLIBORMonteCarloProduct;
import net.finmath.stochastic.RandomVariableInterface;

/**
 * This class implements the valuation of a zero coupon bond.
 * 
 * @author Christian Fries
 * @version 1.1
 */
public class CouponBond extends AbstractLIBORMonteCarloProduct {
	private double coupon;
	private double [] CouponDates;
	private double maturity;
	

    /**
	 * @param maturity The maturity given as double.
	 */
	public CouponBond(double coupon, double[] couponDates, double maturity ) {
		super();
		this.coupon = coupon;
		this.CouponDates = couponDates;
		this.maturity = maturity;
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
		
    	
    	// Initialize 
    	RandomVariableInterface values = model.getRandomVariableForConstant(0.0);
    	
    	for(int i = 0; i<CouponDates.length;i++) {
    		
        	// Get random variables
            RandomVariableInterface	numeraire				= model.getNumeraire(CouponDates[i]);
            RandomVariableInterface	monteCarloProbabilities	= model.getMonteCarloWeights(CouponDates[i]);

            // Calculate numeraire relative value
            
            RandomVariableInterface Coupon = model.getRandomVariableForConstant(coupon).div(numeraire).mult(monteCarloProbabilities);
            values = values.add(Coupon);         
    		
    	}
    	
    	
    	// Get random variables
        RandomVariableInterface	numeraire				= model.getNumeraire(maturity);
        RandomVariableInterface	monteCarloProbabilities	= model.getMonteCarloWeights(maturity);

        // Calculate numeraire relative value
        
        RandomVariableInterface MaturityPayoff = model.getRandomVariableForConstant(1.0).div(numeraire).mult(monteCarloProbabilities); 
        values = values.add(MaturityPayoff);
        
        
        
        // Convert back to values
        RandomVariableInterface	numeraireAtEvaluationTime				= model.getNumeraire(evaluationTime);
        RandomVariableInterface	monteCarloProbabilitiesAtEvaluationTime	= model.getMonteCarloWeights(evaluationTime);
        values = values.mult(numeraireAtEvaluationTime).div(monteCarloProbabilitiesAtEvaluationTime);

		// Return values
		return values;	
	}

	/**
	 * @return Returns the maturity.
	 */
	public double getMaturity() {
		return maturity;
	}

	/**
	 * @param maturity The maturity to set.
	 */
	public void setMaturity(double maturity) {
		this.maturity = maturity;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return super.toString() + "\n" + "maturity: " + maturity;
	}
}

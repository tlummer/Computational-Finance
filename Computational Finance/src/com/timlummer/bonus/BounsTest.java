/*
 * (c) Copyright Christian P. Fries, Germany. All rights reserved. Contact: email@christian-fries.de.
 *
 * Created on 21.01.2004
 */
package com.timlummer.bonus;

import net.finmath.exception.CalculationException;
import net.finmath.functions.AnalyticFormulas;
import net.finmath.montecarlo.BrownianMotion;
import net.finmath.montecarlo.assetderivativevaluation.AssetModelMonteCarloSimulationInterface;
import net.finmath.montecarlo.assetderivativevaluation.BlackScholesModel;
import net.finmath.montecarlo.assetderivativevaluation.MonteCarloAssetModel;
import net.finmath.montecarlo.assetderivativevaluation.products.AbstractAssetMonteCarloProduct;
import net.finmath.montecarlo.assetderivativevaluation.products.EuropeanOption;
import net.finmath.montecarlo.assetderivativevaluation.products.FiniteDifferenceDeltaHedgedPortfolio;
import net.finmath.montecarlo.model.AbstractModel;
import net.finmath.montecarlo.process.AbstractProcess;
import net.finmath.montecarlo.process.ProcessEulerScheme;
import net.finmath.stochastic.RandomVariableInterface;
import net.finmath.time.TimeDiscretization;
import net.finmath.time.TimeDiscretizationInterface;

/**
 * Implements the valuation of a European option on a single asset.
 * 
 * Tim Lummer based on Fries
 * 
 * Given a model for an asset <i>S</i>, the European option with strike <i>K</i>, maturity <i>T</i>
 * pays
 * <br>
 * 	<i>V(T) = max(S(T) - K , 0)</i> in <i>T</i>.
 * <br>
 * 
 * The <code>getValue</code> method of this class will return the random variable <i>N(t) * V(T) / N(T)</i>,
 * where <i>N</i> is the numerarie provided by the model. If <i>N(t)</i> is deterministic,
 * calling <code>getAverage</code> on this random variable will result in the value. Otherwise a
 * conditional expectation has to be applied.
 * 
 * @author Tim Lummer based on Fries
 * @version 1.3
 */
public class BounsTest  {

	public static void main(String[] args) throws Exception {
		
		// Model properties
		double	initialValue   = 100;
		double	riskFreeRate   = 0.01;
		double	volatility     = 0.25;

		// Process discretization properties
		int		numberOfPaths		= 100;
		int		numberOfTimeSteps	= 10;
		double	deltaT				= 0.1;
		
		int		seed				= 31415;

		// Product properties
		double	optionMaturity = 1.0;
		double	optionBarrier = 90.0;
		double  optionBonus = 0.05;

				
		
		// Create a model
		AbstractModel model = new BlackScholesModel(initialValue, riskFreeRate, volatility);

		// Create a time discretization
		TimeDiscretizationInterface timeDiscretization = new TimeDiscretization(0.0 /* initial */, numberOfTimeSteps, deltaT);

		// Create a corresponding MC process 	// net.finmath.montecarlo.process
		AbstractProcess process = new ProcessEulerScheme(new BrownianMotion(timeDiscretization, 1 /* numberOfFactors */, numberOfPaths, seed));

		// Using the process (Euler scheme), create an MC simulation of a Black-Scholes model
		AssetModelMonteCarloSimulationInterface monteCarloBlackScholesModel = new MonteCarloAssetModel(model, process);

		/*
		 * Value a call option (using the product implementation)
		 */

		BonusOption bonusOption = new BonusOption(optionMaturity,optionBarrier, optionBonus);
		//EuropeanOption bonusOption = new EuropeanOption(optionMaturity,optionBarrier);
		
		FiniteDifferenceDeltaHedgedPortfolio bonusHedge = new FiniteDifferenceDeltaHedgedPortfolio(bonusOption, monteCarloBlackScholesModel);
		
		RandomVariableInterface valueBonus = bonusOption.getValue(0.0, monteCarloBlackScholesModel);
		RandomVariableInterface valueHedge = bonusHedge.getValue(optionMaturity ,monteCarloBlackScholesModel);
		
		//double valueNormal = europeanOptionNormal.getValue(monteCarloBlackScholesModel);
		
		//double valueAnalytic = AnalyticFormulas.blackScholesOptionValue(initialValue, riskFreeRate, volatility, optionMaturity, optionStrike);
		

		System.out.println("Bonus Option value.......: " + valueBonus.getAverage());
		System.out.println("Bonus Option value hedge.......: " + valueHedge.getAverage());
	}
}

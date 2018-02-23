package com.timlummer.memoryexpress;

import com.timlummer.bonus.MemoryExpress;

import net.finmath.exception.CalculationException;
import net.finmath.montecarlo.BrownianMotion;
import net.finmath.montecarlo.assetderivativevaluation.AssetModelMonteCarloSimulationInterface;
import net.finmath.montecarlo.assetderivativevaluation.BlackScholesModel;
import net.finmath.montecarlo.assetderivativevaluation.MonteCarloAssetModel;
import net.finmath.montecarlo.assetderivativevaluation.products.AsianOption;
import net.finmath.montecarlo.assetderivativevaluation.products.BermudanOption;
import net.finmath.montecarlo.assetderivativevaluation.products.EuropeanOption;
import net.finmath.montecarlo.model.AbstractModel;
import net.finmath.montecarlo.process.AbstractProcess;
import net.finmath.montecarlo.process.ProcessEulerScheme;
import net.finmath.time.TimeDiscretization;
import net.finmath.time.TimeDiscretizationInterface;



public class MemoryExpresstTest {
	
		
	public static void main(String[] args) {
		// Model properties
		double	initialValue   = 1;
		double	riskFreeRate   = 0.01;
		double	volatility     = 0.25;

		// Process discretization properties
		int		numberOfPaths		= 10000;
		int		numberOfTimeSteps	= 100;
		double	deltaT				= 0.1;
		
		int		seed				= 31415;

		
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
	//	EurpeanOptionTimLummer europeanOption = new EurpeanOptionTimLummer(optionMaturity, optionStrike,Barrier);
		//EuropeanOption europeanOptionNormal = new EuropeanOption(optionMaturity, optionStrike);
			/*

	/*
	 * Express Option
	 */
	double[] exerciseDates	= { 1.0,  2.0,  3.0, 4.0};
	double strikepct		= 0.9;
	double coupon       = 0.055;
	
	double valueMemoryExpress = 0.0;
	
	// Lower bound method
	MemoryExpress memoryExpress = new MemoryExpress(exerciseDates, initialValue, strikepct*initialValue,coupon,0);
	try {
		 valueMemoryExpress = memoryExpress.getValue(monteCarloBlackScholesModel);
	} catch (CalculationException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}

	
	
	System.out.println("Value of Express  is \t"	+ "(" + valueMemoryExpress + ")");

	}
	
	
	
}

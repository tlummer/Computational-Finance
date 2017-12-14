
package com.timlummer.Ex1;


import net.finmath.functions.AnalyticFormulas;
import net.finmath.functions.NormalDistribution;
import net.finmath.montecarlo.BrownianMotion;
import net.finmath.montecarlo.BrownianMotionInterface;
import net.finmath.montecarlo.CorrelatedBrownianMotion;
import net.finmath.montecarlo.RandomVariable;
import net.finmath.stochastic.RandomVariableInterface;
import net.finmath.time.TimeDiscretization;

import java.text.DecimalFormat;

import org.apache.commons.math3.random.MersenneTwister;



/**
 * This is an experiment to test the quality of the random number generator
 * and the discretization scheme. (taken from an Example of C. Fries)
 */



 public class ProductRuleTest
 
{
		private int		numberOfTimeSteps;
		private double	deltaT;
		private int		numberOfPaths;
		private double	initialValue;
		private TimeDiscretization times;
		
		
		private double	volatility[]=new double[2];     //we want a two dimensional Geometric Brownian Motion. We must use a matrix of random variables
		private double[] drift= new double[2];
		
		private double correlation;
		
		private RandomVariableInterface[][]	geometricBivariateProcess = null;
		
		private RandomVariableInterface[] itoProductProcess=null;
		
	
	
	
	public ProductRuleTest(int numberOfPaths, 
			int numberOfTimeSteps, 
			double deltaT, 
			double initialValue, 
			double[] volatility, 
			double[] drift,
				double correlation) {
		this.numberOfPaths=numberOfPaths;
		this.deltaT=deltaT;
			this.numberOfTimeSteps = numberOfTimeSteps;
			this.initialValue = initialValue;
			this.volatility = volatility;
			this.drift = drift;
			this.correlation = correlation;
			
			times=new TimeDiscretization(0.0, numberOfTimeSteps , deltaT);

		}




	public RandomVariableInterface[] getProcessValue(int timeIndex)
	{
		if(geometricBivariateProcess == null)    
		{
			generateProcess();
		}
		
				
		// Return value of process
		return geometricBivariateProcess[timeIndex];
	}
	
	private void generateProcess() {
		geometricBivariateProcess = new RandomVariableInterface[2][getNumberOfTimeSteps()+1];  //allocate memory. important° if you wan tto use lazy initialisaion do it here, not in the constructor.
        itoProductProcess=new RandomVariable[getNumberOfTimeSteps()+1];
		double[][] correlationMatrix={{1.0 , 0},  {correlation, Math.sqrt(1.0-correlation*correlation)}   };
		
		
		BrownianMotionInterface correlatedBrownianMotion= new CorrelatedBrownianMotion( new BrownianMotion(   //the correlated brownian motion inputs a whole brownian motion and a correlation matrix         
				times,    //brownian motion object created inside the constructor. Can be done.
				2,					
				getNumberOfPaths(),
				1234				
				),    correlationMatrix     );
		
		geometricBivariateProcess[0][0]=new RandomVariable(0.0, initialValue);  //initialise first random varible as a constant
		geometricBivariateProcess[1][0]=new RandomVariable(0.0, initialValue);  //initialise first random varible as a constant
		itoProductProcess[0]=  new RandomVariable(0.0,  initialValue*initialValue); 

		
		
		for(int timeIndex = 1; timeIndex < getNumberOfTimeSteps()+1; timeIndex++)
		{
			double[][] newGBMRealization = new double[2][numberOfPaths];   //reset the 
			 double[] newItoProcessRealization=new double[numberOfPaths];
				
				// The numerical scheme

				// Generate values 
				for (int componentIndex = 1; componentIndex < numberOfPaths; componentIndex++ )
				{
					 double previousValueFirstAsset = geometricBivariateProcess[0][timeIndex-1].get(componentIndex);  
					 double previousValueSecondAsset = geometricBivariateProcess[1][timeIndex-1].get(componentIndex);    
					 double previousItoProductProcessValue=itoProductProcess[timeIndex-1].get(componentIndex);	
					//must be reset at all times! 
					// Diffusions
					double diffusionFirstAsset = volatility[0] * correlatedBrownianMotion.getBrownianIncrement(timeIndex-1, 0).get(componentIndex);
					double diffusionSecondAsset = volatility[1] * correlatedBrownianMotion.getBrownianIncrement(timeIndex-1, 1).get(componentIndex);
					
					
					// store new realisations
					
					double incrementFirstAsset=previousValueFirstAsset * drift[0] * deltaT + previousValueFirstAsset * diffusionFirstAsset;
					double incrementSecondAsset=previousValueSecondAsset * drift[1] * deltaT + previousValueSecondAsset * diffusionSecondAsset;

					newGBMRealization[0][componentIndex] = previousValueFirstAsset + incrementFirstAsset;
					newGBMRealization[1][componentIndex] = previousValueSecondAsset + incrementSecondAsset; //Euler scheme
                    
					newItoProcessRealization[componentIndex]=
							  + previousItoProductProcessValue+
							previousValueSecondAsset*incrementFirstAsset
							+previousValueFirstAsset*incrementSecondAsset  +
						  previousValueFirstAsset*previousValueFirstAsset*volatility[0]*
							volatility[1]*correlation*deltaT;
	
			
				}
			
			// Wrap values in RandomVariables
			geometricBivariateProcess[0][timeIndex] = new RandomVariable(times.getTime(timeIndex), newGBMRealization[0]);
			geometricBivariateProcess[1][timeIndex] = new RandomVariable(times.getTime(timeIndex), newGBMRealization[1]);
			itoProductProcess[timeIndex]=new RandomVariable(times.getTime(timeIndex), newItoProcessRealization);
		}
	}


	
	 //some GETTERS
	
	
	
	
	//the time step
	public double getDeltaT() {
		return deltaT;
	}
	
	//the whole time discretisation 
		public TimeDiscretization getTimeDiscretiztion() {
			return times;
		}

// initial value
	public double getInitialValue() {
		return initialValue;
	}



//number of paths
	public int getNumberOfPaths() {
		return numberOfPaths;
	}

//number of time steps
	public int getNumberOfTimeSteps() {
		return numberOfTimeSteps;
	}

	//volatility
	public double[] getVolatility() {
		return volatility;
	}

	//drift
	public double[] getDrift(){
		return drift;
	}
	
	// price and log-price in the time horizon
	
	public RandomVariableInterface getFirstProcessAtSomeInstant(int timeIndex){
		
		if (geometricBivariateProcess== null){
			generateProcess();
		}
		return geometricBivariateProcess[0][timeIndex];
	}
	
	public RandomVariableInterface getSecondProcessAtSomeInstant(int timeIndex){
		if (geometricBivariateProcess== null){
			generateProcess();
		}
		return geometricBivariateProcess[1][timeIndex];
	}
	

public RandomVariableInterface getItoProductProcessAtSomeInstant(int timeIndex){
	if (geometricBivariateProcess== null){
		generateProcess();
	}
	return itoProductProcess[timeIndex];
}
	
	public RandomVariableInterface getFirstProcessFinalValue(){
		return geometricBivariateProcess[0][times.getNumberOfTimes()-1];
	}
	
	public RandomVariableInterface getSecondProcessFinalValue(){
		return geometricBivariateProcess[1][times.getNumberOfTimes()-1];
	}
	
//	public RandomVariableInterface getFirstProcessIncrement(int timeIndex){
//		return this.getFirstProcessAtSomeInstant(timeIndex).sub(this.getFirstProcessAtSomeInstant(timeIndex-1));
//	}
//	
//	public RandomVariableInterface getSecondProcessIncrement(int timeIndex){
//		return this.getSecondProcessAtSomeInstant(timeIndex).sub(this.getSecondProcessAtSomeInstant(timeIndex-1));
//	}
//	
//	public RandomVariableInterface getProductIncrement(int timeIndex){
//		return this.getSecondProcessAtSomeInstant(timeIndex).mult(this.getFirstProcessAtSomeInstant(timeIndex)).sub(this.getSecondProcessAtSomeInstant(timeIndex-1).mult(this.getFirstProcessAtSomeInstant(timeIndex-1)));
//			
//	}
	
	

	public static void main(String[] args)
	{
		double initialValue = 1.0;
		double[] volatility		= {0.5, 0.2	};	// Note: Try different sigmas: 0.2, 0.5, 0.7, 0.9		
		double[] drift={0.03, 0.05};
	
		
		double correlation=0.9;
	
		
		int numberOfPaths	=10;
		
		double deltaT=0.01;
		int numberOfTimeSteps=100;
		
	//	double finalTime=deltaT*numberOfTimeSteps;
		
		
   ProductRuleTest eulerSchemeCorrelatedProcess=
		   new  ProductRuleTest(numberOfPaths, numberOfTimeSteps,  deltaT,  initialValue, volatility, drift,
			 correlation);
   

    
    System.out.println("A verification of Ito´s product rule:");
    
    System.out.println("\n");
    
    System.out.println("time  " + "\t" + "Product of the Processes" + "\t" + "Stochastic Integral Reperesentation of the Process" + "\t" );

     final DecimalFormat formatterReal2	= new DecimalFormat("0.00");
	 final DecimalFormat formatterSci4	= new DecimalFormat(" 0.0000;-0.0000");
	 
	 int testPathIndex=2; // we test the product rule at some path using the increments. ALTERNATIVE wirte the FULL EULER scheme for the right hand side
	
	// System.out.println(eulerSchemeCorrelatedProcess.numberOfPaths);
	 
	 
	 for(int timeIndex=0; timeIndex<eulerSchemeCorrelatedProcess.getNumberOfTimeSteps() ; timeIndex++){
		System.out.println(  //formatting in a table the data gathered for the given time instant (no need of the full power of RVs arrays here)
//				compute the product of the pocesses right here
				formatterReal2.format(eulerSchemeCorrelatedProcess.getTimeDiscretiztion().getTime(timeIndex)) + "\t"  +
				formatterSci4.format(eulerSchemeCorrelatedProcess.getFirstProcessAtSomeInstant(timeIndex).get(testPathIndex)
						*eulerSchemeCorrelatedProcess.getSecondProcessAtSomeInstant(timeIndex).get(testPathIndex) ) + "\t" + "                          " + 
				formatterSci4.format(eulerSchemeCorrelatedProcess.getItoProductProcessAtSomeInstant(timeIndex).get(testPathIndex))
						+ "\t" +   "        " +
 				
				
				""
		);
	 }
	}
}
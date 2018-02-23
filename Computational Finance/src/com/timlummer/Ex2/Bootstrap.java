package com.timlummer.Ex2;

import java.util.Arrays;
import java.util.stream.*;

import net.finmath.optimizer.LevenbergMarquardtTest;

public class Bootstrap {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		double[] liborDates = {0.25, 0.5, 1};
		double[] liborRates = {0.2/100, 0.4/100, 0.65/100};
		
		double[] swapDates = {2,3};
		double[] swapRates = {0.85/100, 0.95/100};
		
		double deltaSwaps = 1.0; 
		
		
		double[] zeroCouponCurveFromLibor = new double [liborRates.length];
		double[] zeroCouponCurveFromSwap = new double [swapRates.length];
		
		Arrays.setAll(zeroCouponCurveFromLibor,  i -> 1 /(1 +  liborDates[i] * liborRates[i]));
		
		zeroCouponCurveFromSwap[0] = bootstrapZeroCouponfromLIBOR(new double [] {zeroCouponCurveFromLibor[2]},deltaSwaps,swapRates[0]);
		
		zeroCouponCurveFromSwap[1] = bootstrapZeroCouponfromLIBOR(new double [] {zeroCouponCurveFromLibor[2],zeroCouponCurveFromSwap[0]},deltaSwaps,swapRates[1]);
		
		System.out.println(zeroCouponCurveFromSwap[0]+"\t" + zeroCouponCurveFromSwap[1]);
		
		
		double[] zeroCouponCurve = new double[6];
		
		double[] zeroCouponCurveTimes = {0.5, 1, 1.5, 2, 2.5, 3}; 
		
		
		zeroCouponCurve[0] = zeroCouponCurveFromLibor[1];
		zeroCouponCurve[1] = zeroCouponCurveFromLibor[2];
		zeroCouponCurve[2] = interpolateZeroCouponCurve(zeroCouponCurveFromLibor[2],zeroCouponCurveFromSwap[0]);
		zeroCouponCurve[3] = zeroCouponCurveFromSwap[0];
		zeroCouponCurve[4] = interpolateZeroCouponCurve(zeroCouponCurveFromSwap[0],zeroCouponCurveFromSwap[1]);
		zeroCouponCurve[5] = zeroCouponCurveFromSwap[1];
	
		double[] yieldCurve = new double[zeroCouponCurve.length];
				
		Arrays.setAll(yieldCurve,  i ->   ((1 /  zeroCouponCurve[i])-1)/zeroCouponCurveTimes[i]);
			
		
		for (int i=0; i<zeroCouponCurveTimes.length;i++) {
			
			System.out.println(zeroCouponCurveTimes[i]+"\t"+zeroCouponCurve[i]+"\t"+yieldCurve[i]);
		}
		
		
		
	}
	
	
	public static double bootstrapZeroCouponfromLIBOR(double[] zeroCoupon, double deltaT, double SwapRate) {		
		
		double couponSum = 0.0;
		
		for (int i=0;i<zeroCoupon.length;i++) {
			
			couponSum += zeroCoupon[i];
			
		}
				
		
		return (1-SwapRate *deltaT * couponSum) /(1+deltaT*SwapRate);
		
	}
	
	
	public static double interpolateZeroCouponCurve(double zeroCouponT0, double zeroCouponT1) {
		
		return Math.exp( 0.5*(Math.log(zeroCouponT0) + Math.log(zeroCouponT1)));
		
	}

}

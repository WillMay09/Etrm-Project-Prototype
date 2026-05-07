package com.fdm.group.Etrm_Project_Prototype;

public class BlackScholesPricer {
	
	//standard normal distribution 
	private double normalCDF(double x) {
		
		return 0.5*(1+erf(x/Math.sqrt(2.0)));
	}
	
	private double erf(double x) {
		
		//Error function approximation
		//use library or abramopwitz and stegun approximation
		return 1.0;
		
	}
	
	public double priceCall(double spot, double strike, double timeToExpiry, double riskFreeRate, double volatility) {
		
		double d1 = (Math.log(spot/strike) + (riskFreeRate + 0.5 * volatility * volatility) * timeToExpiry) / (volatility * Math.sqrt(timeToExpiry));
		
		
		double d2 = d1 - volatility * Math.sqrt(timeToExpiry);
		
		double callPrice = spot * normalCDF(d1) - strike * Math.exp(-riskFreeRate * timeToExpiry)* normalCDF(d2);
		
		return callPrice;
	}
	
	
	
	public double pricePut(double spot, double strike, double timeToExpiry, double riskFreeRate, double volatility) {
		
		//Put-Call parity:
		//Put = Call - Spot + strike x e^(-rT)
		
		
		double callPrice = priceCall(spot,strike, timeToExpiry, riskFreeRate, volatility);
		
		
		double putPrice = (callPrice-spot) + strike * Math.exp(-riskFreeRate * timeToExpiry);
		
		return putPrice;
	}
	
	

	
	
	

}

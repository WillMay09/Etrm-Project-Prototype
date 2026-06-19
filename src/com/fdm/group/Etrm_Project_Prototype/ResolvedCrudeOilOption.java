package com.fdm.group.Etrm_Project_Prototype;

public final class ResolvedCrudeOilOption {
	
	public enum PutCall {
		PUT, CALL
	}
	private final double strike;
	
	private final PutCall putCall;
	
	
	// From Quantity × contractSize — baked in at resolution time
	
	private final double scaledQuantity;
	
	// Resolved from live market data at resolution time
	private final double timeToExpiry;
	private final double impliedVol;
	private final double riskFreeRate;
	private final double spot;
	private final double discountFactor;
	
	
	
	
	public CurrencyAmount presentValue(BlackScholesPricer pricer) {
		
		double pricePerUnit = putCall == PutCall.CALL ? pricer.priceCall(spot, strike, timeToExpiry, riskFreeRate, discountFactor):
			pricer.pricePut(spot, strike, timeToExpiry, riskFreeRate, discountFactor);
		
		
	}
	
	public static Builder builder() {
		
		return new Builder();
	}
	
	
	
	
	public static class Builder {
		
		private double strike;
		
		private PutCall putCall;
		
		
		// From Quantity × contractSize — baked in at resolution time
		
		private double scaledQuantity;
		
		// Resolved from live market data at resolution time
		private double timeToExpiry;
		private double impliedVol;
		private double riskFreeRate;
		private double spot;
		private double discountFactor;
		
		
		
		public Builder() {
			
			
		}
		
	}

}

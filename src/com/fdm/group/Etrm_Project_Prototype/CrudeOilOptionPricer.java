package com.fdm.group.Etrm_Project_Prototype;

import java.time.LocalDate;

import com.fdm.group.Etrm_Project_Prototype.CrudeOilOption.PutCall;

public final class CrudeOilOptionPricer {
	
	public static final CrudeOilOptionPricer DEFAULT = new CrudeOilOptionPricer(new BlackScholesPricer());
	
	private final BlackScholesPricer blackScholes;

	
	
	public CrudeOilOptionPricer(BlackScholesPricer blackScholes) {
		
		this.blackScholes = blackScholes;
	}
	
	/**
     * Present value — the value on the valuation date.
     * Mirrors OpenGamma's DiscountingFraProductPricer.presentValue(ResolvedFra, RatesProvider)
     */
	
	public CurrencyAmount presentValue(ResolvedCrudeOilOption option) {
		
		double pricePerUnit = option.getPutCall() == PutCall.CALL ? 
				blackScholes.priceCall(option.getSpot(), option.getStrike(), option.getTimeToExpiry(), option.getRiskFreeRate(), option.getImpliedVol()) :
					blackScholes.pricePut(option.getSpot(), option.getStrike(), option.getTimeToExpiry(), option.getRiskFreeRate(), option.getImpliedVol());
		
		
		return CurrencyAmount.of("USD", pricePerUnit * option.getScaledQuantity());
	}
	/**
     * Delta exposure, expressed as a currency-scaled amount (barrels of spot exposure × price/barrel implied).
     */
	public CurrencyAmount delta(ResolvedCrudeOilOption option) {
		
		double d1 = blackScholes.calculateD1(option.getSpot(), option.getStrike(), option.getTimeToExpiry(), option.getRiskFreeRate(), option.getImpliedVol());
		
		double deltaPerUnit = option.getPutCall() == PutCall.CALL 
				? blackScholes.normalCDF(d1)
				: blackScholes.normalCDF(d1) - 1.0;
		
		return CurrencyAmount.of("USD", deltaPerUnit * option.getScaledQuantity());
		
		
	}

}

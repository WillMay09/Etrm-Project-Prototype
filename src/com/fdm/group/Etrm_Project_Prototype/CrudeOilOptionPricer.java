package com.fdm.group.Etrm_Project_Prototype;

import java.time.LocalDate;

import com.fdm.group.Etrm_Project_Prototype.CrudeOilOption.PutCall;

public final class CrudeOilOptionPricer {

	public static final CrudeOilOptionPricer DEFAULT = new CrudeOilOptionPricer(new BlackScholesPricer(),
			new GreeksCalculator());

	private final BlackScholesPricer blackScholes;
	private final GreeksCalculator greeksPricer;

	// dependency injection
	public CrudeOilOptionPricer(BlackScholesPricer blackScholes, GreeksCalculator greeksPricer) {

		this.blackScholes = blackScholes;
		this.greeksPricer = greeksPricer;
	}

	/**
	 * Present value — the value on the valuation date. Mirrors OpenGamma's
	 * DiscountingFraProductPricer.presentValue(ResolvedFra, RatesProvider)
	 */

	public CurrencyAmount presentValue(ResolvedCrudeOilOption option) {
		
		double spot = option.getSpot();
		double K = option.getStrike();
		double T = option.getTimeToExpiry();
		double r = option.getRiskFreeRate();
		double vol = option.getRiskFreeRate();
		double qty = option.getScaledQuantity();
		
		//Step 1: pull inputs
		double pricePerUnit = option.getPutCall() == PutCall.CALL
				? blackScholes.priceCall(spot,K,T,r,vol)
				: blackScholes.pricePut(spot,K,T,r,vol);

		return CurrencyAmount.of(option.getCurrency(), pricePerUnit*qty);
	}
	
	public CurrencyAmount forecastValue(ResolvedCrudeOilOption option) {
		
		
		double pv = presentValue(option).getAmount();
		return CurrencyAmount.of(option.getCurrency(), pv/option.getDiscountFactor());
	}

	/**
	 * Delta exposure, expressed as a currency-scaled amount (barrels of spot
	 * exposure × price/barrel implied).
	 */
	public CurrencyAmount delta(ResolvedCrudeOilOption option) {

		double d1 = blackScholes.calculateD1(option.getSpot(), option.getStrike(), option.getTimeToExpiry(),
				option.getRiskFreeRate(), option.getImpliedVol());

		double deltaPerUnit = option.getPutCall() == PutCall.CALL ? blackScholes.normalCDF(d1)
				: blackScholes.normalCDF(d1) - 1.0;

		return CurrencyAmount.of("USD", deltaPerUnit * option.getScaledQuantity());

	}

	public CurrencyAmount gamma(ResolvedCrudeOilOption option) {

		double g = greeksPricer.gamma(option.getSpot(), option.getStrike(), option.getTimeToExpiry(),
				option.getRiskFreeRate(), option.getImpliedVol());

		return CurrencyAmount.of(option.getCurrency(), g * option.getScaledQuantity());

	}
	
	public CurrencyAmount vega(ResolvedCrudeOilOption option) {
		
		double v = greeksPricer.vega(option.getSpot(), option.getStrike(), option.getTimeToExpiry(),
				option.getRiskFreeRate(), option.getImpliedVol());
		
		return CurrencyAmount.of(option.getCurrency(), v*option.getScaledQuantity());
	}

	public CurrencyAmount theta(ResolvedCrudeOilOption option) {

		double t = option.getPutCall() == PutCall.CALL
				? greeksPricer.thetaCall(option.getSpot(), option.getStrike(), option.getTimeToExpiry(),
						option.getRiskFreeRate(), option.getImpliedVol())
				: greeksPricer.thetaPut(option.getSpot(), option.getStrike(), option.getTimeToExpiry(),
						option.getRiskFreeRate(), option.getImpliedVol());

		return CurrencyAmount.of(option.getCurrency(), t * option.getScaledQuantity());
	}

	public CurrencyAmount rho(ResolvedCrudeOilOption option) {

		double r = option.getPutCall() == PutCall.CALL
				? greeksPricer.rhoCall(option.getSpot(), option.getStrike(), option.getTimeToExpiry(),
						option.getRiskFreeRate(), option.getImpliedVol())
				: greeksPricer.rhoPut(option.getSpot(), option.getStrike(), option.getTimeToExpiry(),
						option.getRiskFreeRate(), option.getImpliedVol());

		return CurrencyAmount.of(option.getCurrency(), r * option.getScaledQuantity());
	}
	
	public CurrencyAmount currentCash(ResolvedCrudeOilOption option, LocalDate valuationDate, double settlementSpot) {
		
		//past expiry
		if(option.getExpiryDate().isBefore(valuationDate)) {
			
			return CurrencyAmount.zero(option.getCurrency());
		}
		
		
		//expiry: payoff is now deterministic, no discounting
		
		if(!valuationDate.isBefore(option.getExpiryDate())) {
			
			double payoff = option.getPutCall() == PutCall.CALL ? Math.max(settlementSpot-option.getStrike(), 0.0) :
																Math.max(option.getStrike()-settlementSpot, 0.0);
			
			return CurrencyAmount.of(option.getCurrency(), payoff * option.getScaledQuantity());
		}
		
		//any other day:
		
		return CurrencyAmount.zero(option.getCurrency());
		
	}
	
	
	public CashFlow cashFlows(ResolvedCrudeOilOption option) {
		
		//product has one cashflow: expected payoff at expiry
		
		double fv = forecastValue(option).getAmount();
		CurrencyAmount forecastAmount = CurrencyAmount.of(option.getCurrency(), fv);
		
		return CashFlow.ofForecastValue(option.getExpiryDate(),forecastAmount , option.getDiscountFactor());
				
		
	}

}

package com.fdm.group.Etrm_Project_Prototype;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public final class CrudeOilOptionTradePricer {
	
	private final CrudeOilOptionPricer productPricer;
	
	public CrudeOilOptionTradePricer(CrudeOilOptionPricer productPricer) {
		
		this.productPricer = productPricer;
		
	}
	
	public CurrencyAmount presentValue(ResolvedCrudeOilOptionTrade trade) {
		
		return productPricer.presentValue(trade.getProduct());
	}
	
	
	public CurrencyAmount delta(ResolvedCrudeOilOptionTrade trade) {
		
		return productPricer.delta(trade.getProduct());
	}
	
	
	public CurrencyAmount gamma(ResolvedCrudeOilOptionTrade trade) {
		
		return productPricer.gamma(trade.getProduct());
	}
	
	public CurrencyAmount vega(ResolvedCrudeOilOptionTrade trade) {
		
		return productPricer.vega(trade.getProduct());
	}
	
	public CurrencyAmount theta(ResolvedCrudeOilOptionTrade trade) {
		
		return productPricer.theta(trade.getProduct());
	}
	
	public CurrencyAmount rho(ResolvedCrudeOilOptionTrade trade) {
		
		return productPricer.rho(trade.getProduct());
	}
	
	
	
	 /**
     * Complete cashflows: premium (from TradedPrice) + expected payoff (from product pricer)
     * This is the ONLY method that differs from pure delegation — it adds the trade-level
     * premium cashflow that the product pricer cannot see.
     * Always returns CashFlows plural
     */
	
	
	public CashFlows cashFlows(ResolvedCrudeOilOptionTrade trade, LocalDate valuationDate) {
		
		
		//Product cashflow: expected payoff at expiry
		
		CashFlows productFlows = productPricer.cashFlows(trade.getProduct());
		
		//Trade cashflow: premium agreed at trade time - from TradedPrice, not presentValue
		LocalDate settlementDate = trade.getInfo().getSettlementDate().orElseThrow();
		
		if(valuationDate.isBefore(settlementDate)) {
			
			//premium not yet settled
			double premiumAmount = trade.getTotalPremium(); //
			
			long daysToSettlement = ChronoUnit.DAYS.between(valuationDate, settlementDate);
			
			double df = DiscountFactor.of(trade.getProduct().getRiskFreeRate(), daysToSettlement/365.0);
			
			CashFlow premiumFlow = CashFlow.ofForecastValue(settlementDate, CurrencyAmount.of(trade.getProduct().getCurrency(), premiumAmount), df);
			
			return productFlows.combinedWith(CashFlows.of(premiumFlow));
			
			
		}
		
		return productFlows;
	}
	

}

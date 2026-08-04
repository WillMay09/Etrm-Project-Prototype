package com.fdm.group.Etrm_Project_Prototype;

import java.time.LocalDate;

public final class CrudeOilFutureTradePricer {
	
	private final CrudeOilFutureProductPricer pricer;
	
	
	public CrudeOilFutureTradePricer(CrudeOilFutureProductPricer pricer) {
		
		this.pricer = pricer;
		
	}
	
	public CurrencyAmount presentValue(ResolvedCrudeOilFutureTrade trade) {
		
		
		return pricer.presentValue(trade.getProduct());
	}
	
public CurrencyAmount forcastValue(ResolvedCrudeOilFutureTrade trade) {
		
		return pricer.forecastValue(trade.getProduct());
		
	}
	
	public CurrencyAmount delta(ResolvedCrudeOilFutureTrade trade) {
		
		
		return pricer.delta(trade.getProduct());
	}
	
	public CurrencyAmount currentCash(ResolvedCrudeOilFutureTrade trade, MarketDataProvider marketData) {
		
		
		double previousSettlementPrice = marketData.getForwardPrice(trade.getProduct().getUnderlying(), LocalDate.now());
		
		return pricer.currentCash(trade.getProduct(), previousSettlementPrice);
		
	}
	

}

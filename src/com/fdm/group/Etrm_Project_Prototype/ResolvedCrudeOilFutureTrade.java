package com.fdm.group.Etrm_Project_Prototype;

import java.time.LocalDate;

public final class ResolvedCrudeOilFutureTrade {
	
	
	
	
	private final TradeInfo tradeInfo;
	
	private final ResolvedCrudeOilFuture resolvedCrudeOilFuture;
	
	
	
	private ResolvedCrudeOilFutureTrade(TradeInfo tradeInfo, ResolvedCrudeOilFuture resolvedCrudeOilFuture) {
		
		this.tradeInfo = tradeInfo;
		this.resolvedCrudeOilFuture = resolvedCrudeOilFuture;
	}
	
	public TradeInfo              getInfo()    { return tradeInfo; }
    public ResolvedCrudeOilFuture getProduct() { return resolvedCrudeOilFuture; }

	
	
	public ResolvedCrudeOilFutureTrade of(MarketDataProvider marketData, CrudeOilFutureTrade trade, LocalDate valuationDate) {
		
		
		ResolvedCrudeOilFuture resolvedTrade = ResolvedCrudeOilFuture.of(marketData, trade, valuationDate);
		
		TradeInfo tradeInfo = trade.getInfo();
		
		return new ResolvedCrudeOilFutureTrade(tradeInfo, resolvedTrade);
		
	}
}

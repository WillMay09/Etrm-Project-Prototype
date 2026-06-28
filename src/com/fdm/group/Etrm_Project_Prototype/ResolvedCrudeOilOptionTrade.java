package com.fdm.group.Etrm_Project_Prototype;

import java.time.LocalDate;

public final class ResolvedCrudeOilOptionTrade {
	
	
	
	private final TradeInfo tradeInfo;
	private final ResolvedCrudeOilOption resolvedProduct;
	
	
	
	
	
	public ResolvedCrudeOilOptionTrade(TradeInfo tradeInfo, ResolvedCrudeOilOption resolvedProduct){
		
		this.tradeInfo = tradeInfo;
		this.resolvedProduct = resolvedProduct;
		
	}
	
	public TradeInfo              getInfo()    { return tradeInfo; }
    public ResolvedCrudeOilOption getProduct() { return resolvedProduct; }
    
    public static ResolvedCrudeOilOptionTrade of(CrudeOilOptionTrade trade, MarketDataProvider marketData, LocalDate valuationDate) {
    	
    	ResolvedCrudeOilOption resolvedProduct = ResolvedCrudeOilOption.of(trade, marketData, valuationDate);
    	
    	return new ResolvedCrudeOilOptionTrade(trade.getInfo(), resolvedProduct);
    }

}

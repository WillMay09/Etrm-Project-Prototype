package com.fdm.group.Etrm_Project_Prototype;

import java.time.LocalDate;

/**
 * Resolved TRADE - same TradeInfo (unchanged, carried straight through),
 * plus the resolved product. Mirrors OpenGamma's ResolvedFraTrade exactly:
 * this class does ZERO resolution itself, it is purely a carrier.
 */

public final class ResolvedCrudeOilOptionTrade {
	
	private final TradeInfo tradeInfo;
	private final ResolvedCrudeOilOption resolvedProduct;
	
	
	public ResolvedCrudeOilOptionTrade(TradeInfo tradeInfo, ResolvedCrudeOilOption resolvedProduct){
		
		this.tradeInfo = tradeInfo;
		this.resolvedProduct = resolvedProduct;
		
	}
	
	public TradeInfo              getInfo()    { return tradeInfo; }
    public ResolvedCrudeOilOption getProduct() { return resolvedProduct; }
    
    
    //come back to this
    public double getTotalPremium() {return resolvedProduct.getScaledQuantity() * resolvedProduct.getSpot();};
    
    
    
    public static ResolvedCrudeOilOptionTrade of(CrudeOilOptionTrade trade, MarketDataProvider marketData, LocalDate valuationDate) {
    	
    	ResolvedCrudeOilOption resolvedProduct = ResolvedCrudeOilOption.of(trade, marketData, valuationDate);
    	
    	return new ResolvedCrudeOilOptionTrade(trade.getInfo(), resolvedProduct);
    }

}

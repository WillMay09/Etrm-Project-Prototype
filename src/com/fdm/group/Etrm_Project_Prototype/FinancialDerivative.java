package com.fdm.group.Etrm_Project_Prototype;

import java.time.LocalDate;

public abstract class FinancialDerivative extends EnergyTrade{
	
	protected String underlyingAsset;
	
	protected LocalDate expiryDate;
	
	protected double strikePrice;
	

	public FinancialDerivative(String dealId, LocalDate tradeDate, String counterParty, double quantity, String unit) {
		super(dealId, tradeDate, counterParty, quantity, unit);
		// TODO Auto-generated constructor stub
	}
	
	
	public abstract double calculateIntrinsicValue();
	
	public abstract double calculateTimeValue();
	
	public abstract boolean isInTheMoney();
	
	
	@Override
	public String getCommodityType() {
		
		
		return "DERIVATIVE";
	}

	
}

package com.fdm.group.Etrm_Project_Prototype;

import java.time.LocalDate;

public abstract class DerivativeDeal extends EnergyTrade {

	protected String underlyingCommodity;
	
	protected LocalDate expiryDate;
	
	public DerivativeDeal(String dealId, LocalDate tradeDate, String counterParty, double quantity, String unit,
			double price, String underlyingCommodity, LocalDate expiryDate) {
		super(dealId, tradeDate, counterParty, quantity, unit, price);
		// TODO Auto-generated constructor stub
		
		this.underlyingCommodity = underlyingCommodity;
		this.expiryDate = expiryDate;
	}
	
	public void setExpiryDate(LocalDate expiryDate) {
		
		this.expiryDate = expiryDate;
		
	}
	
public void setExpiryDate() {
		
		this.expiryDate = expiryDate;
		
	}
	
	

	
}

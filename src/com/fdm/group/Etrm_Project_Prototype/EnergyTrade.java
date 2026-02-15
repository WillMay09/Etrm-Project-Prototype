package com.fdm.group.Etrm_Project_Prototype;

import java.time.LocalDate;

public abstract class EnergyTrade implements Tradeable{
	
	
	protected String unit;
	
	protected double quantity;
	
	protected String counterParty;
	 
	protected String dealId;
	
	protected double price;
	
	protected LocalDate tradeDate;
	
	protected double contractPrice;
	
	
	//constructor
	public EnergyTrade(String dealId, LocalDate tradeDate, String counterParty, double quantity, String unit, double price) {
		
		if(dealId == null || dealId.trim().isEmpty()) {
			
			//throw new illegalArgumentException("Deal ID required")
		}
		
		this.unit = unit;
		this.quantity = quantity;
		this.counterParty = counterParty;
		this.dealId = dealId;
		this.tradeDate = tradeDate;
		this.contractPrice = contractPrice;
		this.price = price;
		
	}
	
	//abstract methods
	
	public abstract String getCommodityType();
	
	
	
	
	//concrete methods:
	
	public boolean isExpired() {
		
		return tradeDate.isBefore(LocalDate.now().minusYears(1));
	}
	
	
	public void validate() {
		
		if(dealId == null || dealId.isEmpty()) {
			
			throw new IllegalStateException("Deal ID cannot be empty");
			
			
		}else if(counterParty == null || counterParty.isEmpty()) {
			
			throw new IllegalStateException("Counterparty cannot be null");
			
			
		}
		
	}
	
	//methods from Interface
	
	@Override
	public LocalDate getTradeDate() {
		
		return tradeDate;
	}
	
	@Override
	public String getCounterParty() {
		
		return counterParty;
		
	}
	
	@Override
	public double getNotionalValue() {
		
		return quantity;
	}
	
	@Override
	public String getDealId() {
		
		return dealId;
	}
	
	//concrete getters/setters
	public String getUnit() {
		
		
		return unit;
	}
	
	public double getQuantity() {
		
		
		return quantity * contractPrice;
	}
	
	
	public void setUnit(String unit) {
		
		this.unit = unit;
	}
	
	public void setQuantity(double quantity) {
		
		this.quantity = quantity;
	}
	
	
	
	public void setDealId(String dealId) {
		
		this.dealId = dealId;
	}
	
	

	
	

	
	
	
	

}

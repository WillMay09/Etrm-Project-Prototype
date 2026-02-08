package com.fdm.group.Etrm_Project_Prototype;

import java.time.LocalDate;

public abstract class MetalTrade extends EnergyTrade{
	
	
	

	protected String metal;
	
	protected String exchangeLocation;
	
	protected double purity;
	
	public MetalTrade(String dealId, LocalDate tradeDate, String counterParty, double quantity, String unit, String metal,
			String exchangeLocation, double purity) {
		super(dealId, tradeDate, counterParty, quantity, unit);
		
		// TODO Auto-generated constructor stub
	}
	
	public abstract String getMetalGrade();
	
	public abstract double getStorageCost();
	
	
	
	public String getMetal() {
		
		
		return metal;
	}
	

	public void settMetal(String metal) {
		
		
		this.metal = metal;
	}
	
	
	public String getExchangeLocation() {
		
		
		return exchangeLocation;
	}
	public void setExchangeLocation(String exchangeLocation) {
		
		
		this.exchangeLocation = exchangeLocation;
	}

	

}

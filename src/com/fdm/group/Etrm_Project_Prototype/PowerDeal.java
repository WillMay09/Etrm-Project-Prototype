package com.fdm.group.Etrm_Project_Prototype;

import java.time.LocalDate;

public class PowerDeal extends PhysicalCommodity implements Priceable, Settleable {
	
	private int deliveryHour;
	
	private double strikePrice;
	
	private String voltageLevel;
	
	private String deliveryPoint;
	
	
	
	
	public PowerDeal(String dealId, LocalDate tradeDate, String counterParty, 
            double quantity, double contractPrice, String unit,
            String deliveryLocation, LocalDate deliveryStartDate, 
            LocalDate deliveryEndDate, String transportMethod,
            int deliveryHour) {

		// Call parent constructor with ALL required parameters
		super(dealId, tradeDate, counterParty, quantity, contractPrice, unit,
				deliveryLocation, deliveryStartDate, deliveryEndDate);

		// Set PowerDeal-specific fields
		this.deliveryHour = deliveryHour;
		this.transportMethod = transportMethod;  // If this is a PowerDeal field
	
	
	}
	//settleable interface
	@Override
	public LocalDate getSettlementDate() {
		// localDate object method used here
		return tradeDate.plusDays(2);
	}

	@Override
	public double calculateSettlementAmount() {
		// TODO Auto-generated method stub
		return quantity * strikePrice;
	}
	
	
	
	//Priceable interface
	//quantity is protected
	
	
	//updates constantly
	@Override
	public double calculateMTM() {
		// TODO Auto-generated method stub
		return quantity * getMarketPrice();
	}

	@Override
	public double getMarketPrice() {
		// TODO Auto-generated method stub
		return 45.30;
	}
	
	
	
	//EnergyDeal Abastract class

	@Override
	public String getCommodityType() {
		// TODO Auto-generated method stub
		return "POWER";
	}
	
	@Override
	public double calculateNotionalValue() {
		// TODO Auto-generated method stub
		return contractPrice * quantity;
	}
	@Override
	public double getDeliveryCost() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public boolean requiresStorage() {
		// TODO Auto-generated method stub
		return false;
	}

}

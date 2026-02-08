package com.fdm.group.Etrm_Project_Prototype;

import java.time.LocalDate;

public class LNGDeal extends EnergyTrade implements Priceable, Settleable {

	private LocalDate deliveryDate;
	
	private double resetPrice;
	
	public LNGDeal(String dealId, LocalDate tradeDate, String counterParty, double quantity, String unit) {
		
		super(dealId, tradeDate, counterParty, quantity, unit);
		
		
	}
	
	
	
	
	
	
	//Abstract methods from parent class

	@Override
	public String getCommodityType() {
		// TODO Auto-generated method stub
		return "LIQUAD NATURAL GAS";
	}

	
	
	@Override
	public double calculateNotionalValue() {
		// TODO Auto-generated method stub
		return 0;
	}
}

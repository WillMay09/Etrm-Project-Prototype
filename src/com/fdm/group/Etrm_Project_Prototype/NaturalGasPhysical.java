package com.fdm.group.Etrm_Project_Prototype;

import java.time.LocalDate;

public class NaturalGasPhysical extends PhysicalDeal {
	
	

	private String deliveryPoint;
	
	
	public NaturalGasPhysical(String dealId, LocalDate tradeDate, String counterParty, double quantity, String unit,
			double price) {
		super(dealId, tradeDate, counterParty, quantity, unit, price);
		// TODO Auto-generated constructor stub
		
		this.deliveryPoint = deliveryPoint;
	}



	

	@Override
	public String getInstrumentType() {
		// TODO Auto-generated method stub
		return "NATURAL_GAS";
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

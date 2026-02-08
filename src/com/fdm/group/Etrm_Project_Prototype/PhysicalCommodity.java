package com.fdm.group.Etrm_Project_Prototype;

import java.time.LocalDate;

public abstract class PhysicalCommodity extends EnergyTrade {

	
	protected String deliveryLocation;
	
	protected LocalDate deliveryStartDate;
	
	protected LocalDate deliveryEndDate;
	
	protected String transportMethod;
	
	
	
	public PhysicalCommodity(String dealId, LocalDate tradeDate, String counterParty, double quantity,double contractPrice, String unit,
			String deliveryLocation, LocalDate deliveryStartDate, LocalDate deliveryEndDate) {
		super(dealId, tradeDate, counterParty, quantity, unit);
		this.deliveryLocation = deliveryLocation;
		this.deliveryStartDate = deliveryStartDate;
		this.deliveryEndDate = deliveryEndDate;
		
	}
	
	
	public abstract double getDeliveryCost();
	
	public abstract boolean requiresStorage();
	
	public String getDeliveryLocation() {
		
		return deliveryLocation;
	}
	
	 public LocalDate getDeliveryStartDate() {
	        return deliveryStartDate;
	    }
	    
	 public LocalDate getDeliveryEndDate() {
	        return deliveryEndDate;
	   }
	 
	 
	 public void setDeliveryLocation(String deliveryLocation) {
		 
		 this.deliveryLocation = deliveryLocation;
	 }
	    
	 public void setDeliveryStartDate(LocalDate deliveryStartDate) {
		 
		 this.deliveryStartDate = deliveryStartDate;
	 }
	 
	 public void setDeliveryEndDate(LocalDate deliveryLocation) {
		 
		 this.deliveryEndDate = deliveryEndDate;
	 }
	    
	    
	
	
	
	
	
	

}

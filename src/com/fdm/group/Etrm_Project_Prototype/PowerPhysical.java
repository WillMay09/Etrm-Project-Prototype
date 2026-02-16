package com.fdm.group.Etrm_Project_Prototype;

import java.time.LocalDate;

public class PowerPhysical extends PhysicalDeal {

	private int deliveryHour;

	private double strikePrice;

	private String voltageLevel;

	private String deliveryPoint;

	public PowerPhysical(String dealId, LocalDate tradeDate, String counterParty, double quantity, String unit,
			double price, int deliveryHour, double strikePrice, String voltageLevel, String deliveryPoint) {
		super(dealId, tradeDate, counterParty, quantity, unit, price);

		this.deliveryHour = deliveryHour;
		this.strikePrice = strikePrice;
		this.voltageLevel = voltageLevel;
		this.deliveryPoint = deliveryPoint;
		// TODO Auto-generated constructor stub
	}

	public int getDeliveryHour() {
		return deliveryHour;
	}

	public void setDeliveryHour(int deliveryHour) {
		this.deliveryHour = deliveryHour;
	}

	public double getStrikePrice() {
		return strikePrice;
	}

	public void setStrikePrice(double strikePrice) {
		this.strikePrice = strikePrice;
	}

	public String getVoltageLevel() {
		return voltageLevel;
	}

	public void setVoltageLevel(String voltageLevel) {
		this.voltageLevel = voltageLevel;
	}

	public String getDeliveryPoint() {
		return deliveryPoint;
	}

	public void setDeliveryPoint(String deliveryPoint) {
		this.deliveryPoint = deliveryPoint;
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

	@Override
	public String getInstrumentType() {
		// TODO Auto-generated method stub
		return "NATURAL_GAS";
	}

}

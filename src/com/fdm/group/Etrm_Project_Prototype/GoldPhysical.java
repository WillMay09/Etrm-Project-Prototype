package com.fdm.group.Etrm_Project_Prototype;

import java.time.LocalDate;

public class GoldPhysical extends PhysicalDeal {

	private double purity;
	private String vaultLocation;

	public GoldPhysical(String dealId, LocalDate tradeDate, String counterParty, double quantity, String unit,
			double price) {
		super(dealId, tradeDate, counterParty, quantity, unit, price);
		// TODO Auto-generated constructor stub
		this.purity = purity;
		this.vaultLocation = vaultLocation;

	}

	public void setPurity(double purity) {

		this.purity = purity;
	}

	public double getPurity() {

		return purity;
	}

	public void setVaultLocation(String vaultLocation) {
		
		this.vaultLocation = vaultLocation;

	}

	public String getVaultLocation() {
		
		return vaultLocation;

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
		return "GOLD";
	}

}

package com.fdm.group.Etrm_Project_Prototype;

import java.time.LocalDate;

public class PowerFuture extends DerivativeDeal {
	
	private String contractMonth;
	private String exchange;
	private double initialMargin;
	private double maintenanceMargin;

	public PowerFuture(String dealId, LocalDate tradeDate, String counterParty, double quantity, String unit,
			double price, String underlyingCommodity, LocalDate expiryDate, String contractMonth, String exchange, Double initialMargin,
			double maintenanceMargin) {
		super(dealId, tradeDate, counterParty, quantity, unit, price, underlyingCommodity, expiryDate);
		// TODO Auto-generated constructor stub
		
		this.contractMonth = contractMonth;
		this.exchange = exchange;
		this.initialMargin = initialMargin;
		this.maintenanceMargin = maintenanceMargin;
		
	}

	public String getContractMonth() {
		return contractMonth;
	}

	public void setContractMonth(String contractMonth) {
		this.contractMonth = contractMonth;
	}

	public String getExchange() {
		return exchange;
	}

	public void setExchange(String exchange) {
		this.exchange = exchange;
	}

	public double getInitialMargin() {
		return initialMargin;
	}

	public void setInitialMargin(double initialMargin) {
		this.initialMargin = initialMargin;
	}

	public double getMaintenanceMargin() {
		return maintenanceMargin;
	}

	public void setMaintenanceMargin(double maintenanceMargin) {
		this.maintenanceMargin = maintenanceMargin;
	}
	
	

}

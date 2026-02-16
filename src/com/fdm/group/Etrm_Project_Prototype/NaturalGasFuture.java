package com.fdm.group.Etrm_Project_Prototype;

import java.time.LocalDate;

public class NaturalGasFuture extends DerivativeDeal {
	
	private String contractMonth;
	
	private String exchange;

	public NaturalGasFuture(String dealId, LocalDate tradeDate, String counterParty, double quantity, String unit,
			double price, String underlyingCommodity, LocalDate expiryDate, String contractMonth, String exchange) {
		super(dealId, tradeDate, counterParty, quantity, unit, price, underlyingCommodity, expiryDate);
		// TODO Auto-generated constructor stub
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
	
	

	
	
}

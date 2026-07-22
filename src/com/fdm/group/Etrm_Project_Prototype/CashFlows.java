package com.fdm.group.Etrm_Project_Prototype;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class CashFlows {
	
	
	private final List<CashFlow> cashFlows;
	
	
	private CashFlows(List<CashFlow> cashFlows) {
		
		this.cashFlows = List.copyOf(cashFlows);
	}
	
	public static CashFlows of(CashFlow cashFlow) {
		
		return new CashFlows(Arrays.asList(cashFlow));
	}
	
	public static CashFlows of(List<CashFlow> flows) {
		
		return new CashFlows(flows);
	}
	
	public List<CashFlow> getCashFlows(){ return cashFlows;}
	
	/** Combine two CashFlows instances — used by trade pricer */
	
	public CashFlows combinedWith(CashFlows other) {
		
		List<CashFlow> combined = new ArrayList<>(cashFlows);
		combined.addAll(other.getCashFlows());
		return new CashFlows(combined);
			
	}
	
}

package com.fdm.group.Etrm_Project_Prototype;

import java.time.LocalDate;

public interface Settleable {

	
	public LocalDate getSettlementDate();
	
	public double calculateSettlementAmount();
}

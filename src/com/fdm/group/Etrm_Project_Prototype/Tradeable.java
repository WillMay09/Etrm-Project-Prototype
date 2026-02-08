package com.fdm.group.Etrm_Project_Prototype;

import java.time.LocalDate;

public interface Tradeable {

	
	public LocalDate getTradeDate();
	
	
	public String getCounterParty();
	
	
	public double getNotionalValue();
	
	public String getDealId();
}



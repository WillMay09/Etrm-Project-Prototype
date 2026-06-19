package com.fdm.group.Etrm_Project_Prototype;


//extends portfolio item
public interface Trade {


    /**
     * The business context: who, when, where, settlement.
     * Always present. Never null.
     */
	
	TradeInfo getInfo();
	
	
	
	
}

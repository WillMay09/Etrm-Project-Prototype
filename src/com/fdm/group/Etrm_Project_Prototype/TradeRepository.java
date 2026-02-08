package com.fdm.group.Etrm_Project_Prototype;

import java.util.HashMap;
import java.util.TreeSet;


public class TradeRepository {

	private HashMap<Integer, String> tradeLookUp;
	
	private TreeSet<EnergyTrade> sortedDeals;
	
	
	
	
	
	public TradeRepository() {
		tradeLookUp = new HashMap<>();
		sortedDeals = new TreeSet<>();
		
		
	}
	
	
	
	public TreeSet<EnergyTrade> getSortedDeals(){
		
		
		return sortedDeals;
	}
	
	
	public HashMap<Integer, String> getTradeLookup(){
		
		
		return tradeLookUp;
	}
	
	
	public void addDeals(EnergyTrade deal) {
		
		int tradeNumber = tradeLookUp.size()+ 1;
		
		tradeLookUp.put(tradeNumber, deal.getDealId());
		
		sortedDeals.add(deal);
		
		
		
	}
	
}

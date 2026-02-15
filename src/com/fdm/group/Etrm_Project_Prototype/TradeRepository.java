package com.fdm.group.Etrm_Project_Prototype;

import java.util.HashMap;
import java.util.SortedSet;
import java.util.Map;
import java.util.SortedSet;
import java.util.SortedSet;
import java.util.TreeSet;

public class TradeRepository {

	private HashMap<Integer, String> tradeLookUp;

	private TreeSet<EnergyTrade> sortedDeals;

	public TradeRepository() {
		tradeLookUp = new HashMap<>();
		sortedDeals = new TreeSet<>();

	}

	public SortedSet<EnergyTrade> getSortedDeals() {

		return sortedDeals;
	}

	public Map<Integer, String> getTradeLookup() {

		return tradeLookUp;
	}

	public void addDeals(EnergyTrade deal) {

		int tradeNumber = tradeLookUp.size() + 1;

		tradeLookUp.put(tradeNumber, deal.getDealId());

		sortedDeals.add(deal);

	}

}

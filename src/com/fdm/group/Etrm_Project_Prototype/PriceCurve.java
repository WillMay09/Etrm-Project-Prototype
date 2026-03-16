package com.fdm.group.Etrm_Project_Prototype;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.TreeMap;



/**
 * Represents forward prices for different delivery dates
 * Used for pricing futures contracts
 */

public class PriceCurve {

	private final String commodity;

	private final TreeMap<LocalDate, Double> prices;

	public PriceCurve(String commodity) {

		this.commodity = commodity;

		prices = new TreeMap<>();

	}

	/**
	 * Add a forward price point
	 */

	public void addPrice(LocalDate date, double price) {

		prices.put(date, price);
	}

	/**
	 * Get price for specific date (with interpolation)
	 */

	public double getPrice(LocalDate targetDate) {

		// Exact Match

		if (prices.containsKey(targetDate)) {

			return prices.get(targetDate);

		}

		// Find surrounding dates

		LocalDate before = prices.floorKey(targetDate);
		LocalDate after = prices.ceilingKey(targetDate);

		// Extrapolate if beyond range
		double priceBefore = prices.get(before);
		double priceAfter = prices.get(after);

		long daysBefore = ChronoUnit.DAYS.between(before, targetDate);

		long daysTotal = ChronoUnit.DAYS.between(before, after);

		double weight = (double) daysBefore / daysTotal;

		return priceBefore + weight * (priceAfter - priceBefore);

	}

	public String getCommodity() {

		return commodity;
	}

}

package com.fdm.group.Etrm_Project_Prototype;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;

public class TimeSeries {

	/**
	 * Time series of historical prices. Used for calculating historical volatility,
	 * correlations, etc.
	 */

//	Use TreeMap<LocalDate, Double> because time series need:
//
//		✅ Automatic sorting - Dates always in chronological order
//		✅ Range queries - subMap(start, end) for date ranges
//		✅ Nearest neighbor - floorEntry() for handling holidays/weekends
//		✅ First/last access - firstEntry(), lastEntry() for endpoints
//		✅ Sequential access - Calculate returns, volatility in order

	// good for sub-maps/finding values within limits
	private final NavigableMap<LocalDate, Double> data;

	public TimeSeries() {

		this.data = new TreeMap<>();
	}

	public static TimeSeries empty() {

		return new TimeSeries();
	}
	/**
	 * Add data point
	 */
	public void addPoint(LocalDate date, double value) {

		data.put(date, value);
	}
	/**
	 * Retrieves price at a specific date
	 */
	public double getValue(LocalDate date) {

		if (!data.containsKey(date)) {

			// needs to be implemented

			throw new IllegalArgumentException("No data for this date" + date);
		}

		return data.get(date);

	}

	/**
	 * Get all values in date range
	 */

	public List<Double> getValues(LocalDate startDate, LocalDate endDate) {

		return new ArrayList<>(data.subMap(startDate, true, endDate, true).values());

	}
	
	/**
	 * Return size of historical prices map
	 */
	
	public int size() {
		
		return data.size();
	}

	/**
	 * Calculate historical volatility Returns annualized standard deviation of
	 * returns
	 */
	public double calculateVolatility(int periods) {

		if (data.size() < periods + 1) {

			throw new IllegalArgumentException("Not enough data points for volatility calculation");
		}

		List<Double> prices = new ArrayList<>(data.values());

		List<Double> returns = new ArrayList<>();

		// calculation for daily returns
		for (int i = 1; i < prices.size() && i <= periods; i++) {

			double ret = Math.log(prices.get(i) / prices.get(i - 1));

			returns.add(ret);

		}
		// calculate mean
		double mean = returns.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
		// calculate variance
		double variance = returns.stream().mapToDouble(r -> Math.pow(r - mean, 2)).average().orElse(0.0);

		// Annualize(assuming 252 trading days)
		return Math.sqrt(variance * 252);
	}

}

package com.fdm.group.Etrm_Project_Prototype;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.DoubleUnaryOperator;
import java.util.stream.Collectors;
import java.util.OptionalDouble;

public class TimeSeries {

	/**
	 * Time series of historical prices. Used for calculating historical volatility,
	 * correlations, etc.
	 */

	private final String commodity;

	private final NavigableMap<LocalDate, Double> historicalPrices;

	private TimeSeries(Builder builder) {

		this.commodity = builder.commodity;
		this.historicalPrices = new TreeMap<>(builder.historicalPrices);

	}

	private TimeSeries(String commodity, Map<LocalDate, Double> filtered) {

		this.commodity = commodity;
		this.historicalPrices = new TreeMap<>(filtered);

	}

	/**
	 * Creates a new Builder for MarketDataProvider
	 * 
	 * @return New Builder instance
	 */

	public static Builder builder() {

		return new Builder();

	}

	public static class Builder {

		private NavigableMap<LocalDate, Double> historicalPrices = new TreeMap<>();

		private String commodity;

		private Builder() {

		}

		public Builder commodity(String commodity) {

			this.commodity = commodity;

			return this;

		}

		public Builder addPoint(LocalDate date, double price) {

			historicalPrices.put(date, price);

			return this;
		}

		public TimeSeries createTimeSeries() {

			if (commodity == null) {

				throw new IllegalArgumentException("commodity must not be null");

			}

			return new TimeSeries(this);

		}

	}

	public boolean isEmpty() {

		if (historicalPrices.isEmpty()) {

			return true;
		} else {

			return false;
		}
	}

	/**
	 * Get the earliest date in the series
	 * 
	 * @throws IllegalStateException if series is empty
	 */

	public LocalDate getEarliestDate() {

		if (isEmpty()) {

			throw new IllegalStateException();

		}
		LocalDate firstDate = historicalPrices.firstKey();

		return firstDate;
	}

	/**
	 * Get the latest date in the series
	 * 
	 * @throws IllegalStateException if series is empty
	 */

	public LocalDate getLatestDate() {

		if (isEmpty()) {

			throw new IllegalStateException();
		}

		LocalDate lastDate = historicalPrices.lastKey();

		return lastDate;
	}

	/**
	 * Check if time series contains data for a specific date
	 */

	public boolean containsDate(LocalDate date) {

		return historicalPrices.containsKey(date);

	}

	/**
	 * Add data point
	 */
	public void addPoint(LocalDate date, double value) {

		historicalPrices.put(date, value);
	}

	/**
	 * Retrieves price at a specific date
	 * 
	 * @return OptionalDouble.empty() if date not found, otherwise the value
	 */
	public OptionalDouble getValue(LocalDate date) {

		Double value = historicalPrices.get(date);

		if (value == null) {

			return OptionalDouble.empty();
		}

		return OptionalDouble.of(value);

	}

	// =========================================================================
	// Statistics Methods
	// =========================================================================

	/**
	 * Calculate minimum value in the series
	 * 
	 * @throws IllegalStateException if series is empty
	 */

	public double minValue() {

		if (isEmpty()) {

			throw new IllegalStateException("TimeSeries data must have values to calculate minimum");
		}

		return historicalPrices.values().stream().mapToDouble(Double::doubleValue).min().getAsDouble();

	}

	/**
	 * Calculate maximum value in the series
	 * 
	 * @throws IllegalStateException if series is empty
	 */

	public double maxValue() {

		if (isEmpty()) {

			throw new IllegalStateException("TimeSeries data must have values to calculate maximum");

		}

		return historicalPrices.values().stream().mapToDouble(Double::doubleValue).max().getAsDouble();
	}

	/**
	 * Calculate average (mean) value in the series
	 * 
	 * @throws IllegalStateException if series is empty
	 */

	public double meanValue() {

		if (isEmpty()) {

			throw new IllegalStateException("TimeSeries data must have values to calculate mean");
		}

		return historicalPrices.values().stream().mapToDouble(Double::doubleValue).average().getAsDouble();
	}

	/**
	 * Calculate standard deviation (sample) Used for volatility calculations
	 * 
	 * @throws IllegalStateException if series has fewer than 2 points
	 */

	public double standardDeviation() {

		if (size() < 2) {

			throw new IllegalStateException("Need at least two historical points for standard deviation calculation");
		}

		double mean = meanValue();

		double sumSquaredDiff = historicalPrices.values().stream().mapToDouble(value -> {
			double diff = value - mean;
			return diff * diff;
		}).sum();

		return Math.sqrt(sumSquaredDiff / (size() - 1));

	}

	// =========================================================================
	// subSeries Methods
	// =========================================================================

	/**
	 * Gets a subseries of values between 2 specific dates, inclusive
	 */

	public List<Double> subSeries(LocalDate startDate, LocalDate endDate) {

		return new ArrayList<>(historicalPrices.subMap(startDate, true, endDate, true).values());

	}

	/**
	 * Get the last N data points
	 * 
	 * @param numPoints Number of most recent points to include
	 * @return New TimeSeries with last N points
	 */

	public TimeSeries tailSeries(int numPoints) {

		if (numPoints < 0) {

			throw new IllegalArgumentException("numPoints must be postive");
		}

		if (numPoints >= size()) {

			return new TimeSeries(commodity, historicalPrices);

		}

		List<LocalDate> sortedDates = historicalPrices.keySet().stream().sorted().collect(Collectors.toList());

		List<LocalDate> lastNDates = sortedDates.subList(sortedDates.size() - numPoints, sortedDates.size());

		Map<LocalDate, Double> filtered = historicalPrices.entrySet().stream()
				.filter(entry -> lastNDates.contains(entry.getKey()))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

		return new TimeSeries(commodity, filtered);

	}
	
	

	/**
	 * Get the first N data points
	 * 
	 * @param numPoints Number of earliest points to include
	 * @return New TimeSeries with first N points
	 */

	public TimeSeries headSeries(int numPoints) {
		
		if (numPoints < 0) {

			throw new IllegalArgumentException("numPoints must be postive");
		}

		if (numPoints >= size()) {

			return new TimeSeries(commodity, historicalPrices);

		}
		
		List<LocalDate> sortedDates = historicalPrices.keySet().stream().sorted().collect(Collectors.toList());
		
		List<LocalDate> firstNDates = sortedDates.subList(0, numPoints);
		
		Map<LocalDate, Double> filtered = historicalPrices.entrySet().stream()
				.filter(entry -> firstNDates.contains(entry.getKey()))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
		
		
		return new TimeSeries(commodity, filtered);
		
		

	}
	
	
	
//	/**
//	 * Get a sub-series for a specific date range
//	 * 
//	 * @param startInclusive Start date (inclusive)
//	 * @param endExclusive End date (exclusive)
//	 * @return New TimeSeries containing only dates in range
//	 */
//	
//	
//	public TimeSeries subSeries(LocalDate startInclusive, LocalDate endExclusive) {
//		
//		Objects.requireNonNull(startInclusive, "Start date cannot be null");
//		Objects.requireNonNull(endExclusive, "End date cannot be null");
//		
//		
//		if(!startInclusive.isBefore(endExclusive)) {
//			
//			throw new IllegalArgumentException("start Date must be before end date");
//			
//		}
//		
//		Map<LocalDate, Double> filtered = historicalPrices.entrySet().stream()
//				.filter(entry -> {
//					LocalDate date = entry.getKey();
//					return !date.isBefore(startInclusive) && date.isBefore(endExclusive);
//				}).collect(Collectors.toMap(Map.Entry::getKey,Map.Entry:: getValue));
//		
//		return new TimeSeries(commodity, filtered);
//		
//		
//	}
	
	// =========================================================================
	// Mapping Methods
	// =========================================================================

	public TimeSeries mapValues(DoubleUnaryOperator mapper) {
		
		Objects.requireNonNull(mapper, "Mapper function cannot be null");
		
		Map<LocalDate, Double> transformed =  historicalPrices.entrySet().stream()
					.collect(Collectors.toMap(Map.Entry::getKey, entry -> mapper.applyAsDouble(entry.getValue())));
		
		
		return new TimeSeries(commodity, transformed);
		
	}
	
	
	/**
	 * Apply a function to all values
	 * 
	 * @param mapper Function to apply to each value
	 * @return New TimeSeries with transformed values
	 */

	/**
	 * Return size of historical prices map
	 */

	public int size() {

		return historicalPrices.size();
	}

	/**
	 * Calculate historical volatility Returns annualized standard deviation of
	 * returns
	 */
	public double calculateVolatility(int periods) {

		if (historicalPrices.size() < periods + 1) {

			throw new IllegalArgumentException("Not enough data points for volatility calculation");
		}

		List<Double> prices = new ArrayList<>(historicalPrices.values());

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

package com.fdm.group.Etrm_Project_Prototype;

import java.time.LocalDate;

public final class CashFlow {
	
	private final LocalDate paymentDate;
	
	private final CurrencyAmount presentValue;
	
	private final CurrencyAmount forecastValue;
	
	private final double discountFactor;
	
	
	private CashFlow(LocalDate paymentDate, CurrencyAmount presentValue, CurrencyAmount forecastValue, double discountFactor) {
		
		this.paymentDate = paymentDate;
		this.presentValue = presentValue;
		this.forecastValue = forecastValue;
		this.discountFactor = discountFactor;
		
	}
	
	/**
	   * Creates a {@code CashFlow} representing a single cash flow from
	   * payment date, present value and discount factor.
	   * 
	   * @param paymentDate  the payment date
	   * @param presentValue  the present value as a currency amount
	   * @param discountFactor  the discount factor
	   * @return the cash flow instance
	   */
	
	
	public static CashFlow ofPresentValue(LocalDate paymentDate, CurrencyAmount presentValue, double discountFactor) {
		
		return new CashFlow(paymentDate, presentValue, presentValue.multipliedBy(1d/discountFactor), discountFactor);
	}
	
	
	/**
	   * Creates a {@code CashFlow} representing a single cash flow from
	   * payment date, forecast value and discount factor.
	   * 
	   * @param paymentDate  the payment date
	   * @param forecastValue  the forecast value as a currency amount
	   * @param discountFactor  the discount factor
	   * @return the cash flow instance
	   */
	
	public static CashFlow ofForecastValue(LocalDate paymentDate, CurrencyAmount forecastValue, double discountFactor) {
		
		return new CashFlow(paymentDate, forecastValue.multipliedBy(discountFactor), forecastValue, discountFactor);
	}
	
	 /**
	   * Converts this cash flow to an equivalent amount in the specified currency.
	   * <p>
	   * The result will have both the present and forecast value expressed in terms of the given currency.
	   * If conversion is needed, the provider will be used to supply the FX rate.
	   * 
	   * @param resultCurrency  the currency of the result
	   * @param rateProvider  the provider of FX rates
	   * @return the converted instance, in the specified currency
	   * @throws RuntimeException if no FX rate could be found
	   */
	
	
	
	 /**
	   * Gets the payment date.
	   * <p>
	   * This is the date on which the cash flow occurs.
	   * @return the value of the property, not null
	   */
	  public LocalDate getPaymentDate() {
	    return paymentDate;
	  }

	  //-----------------------------------------------------------------------
	  /**
	   * Gets the present value of the cash flow.
	   * <p>
	   * The present value is signed.
	   * A negative value indicates a payment while a positive value indicates receipt.
	   * @return the value of the property, not null
	   */
	  public CurrencyAmount getPresentValue() {
	    return presentValue;
	  }

	  //-----------------------------------------------------------------------
	  /**
	   * Gets the forecast value of the cash flow.
	   * <p>
	   * The forecast value is signed.
	   * A negative value indicates a payment while a positive value indicates receipt.
	   * @return the value of the property, not null
	   */
	  public CurrencyAmount getForecastValue() {
	    return forecastValue;
	  }

	  //-----------------------------------------------------------------------
	  /**
	   * Gets the discount factor.
	   * <p>
	   * This is the discount factor between valuation date and the payment date.
	   * Thus present value is the forecast value multiplied by the discount factor.
	   * @return the value of the property
	   */
	  public double getDiscountFactor() {
	    return discountFactor;
	  }
	
	
	
	
	
	
	
	

}

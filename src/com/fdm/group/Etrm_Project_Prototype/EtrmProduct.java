package com.fdm.group.Etrm_Project_Prototype;


/**
 * Marker interface for all ETRM financial instruments.
 *
 * Following OpenGamma's pattern exactly — Product has no methods.
 * It exists purely to constrain the generic type parameter on Trade.
 *
 * public interface Product {}  ← OpenGamma's actual definition
 *
 * Each concrete product (CrudeOilOption, CrudeOilFuture) carries
 * ALL the mathematical information needed to price the instrument.
 * No counterparty, no booking date, no settlement — those are TradeInfo.
 */


public interface EtrmProduct {

	 /**
     * The underlying commodity this product references.
     * "CRUDE_OIL", "NATURAL_GAS", "POWER_ERCOT"
     */
	String getUnderlying();
	
	  /**
     * The notional size of one contract in physical units.
     * For crude: 1000 barrels. For gas: 10,000 MMBtu.
     * This is intrinsic to the product definition, not the trade.
     */
	
	
	double getContractSize();
	
}

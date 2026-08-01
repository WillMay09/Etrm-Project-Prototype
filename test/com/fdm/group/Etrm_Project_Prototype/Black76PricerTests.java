
package com.fdm.group.Etrm_Project_Prototype;
 
import static org.junit.jupiter.api.Assertions.*;
 
import java.time.LocalDate;
 
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
 
import com.fdm.group.Etrm_Project_Prototype.CrudeOilOption.PutCall;
 
// ─────────────────────────────────────────────────────────────────────────────
// BLACK-76 PRICER TESTS
// Tests the Black-76 formula methods on BlackScholesPricer and the
// updated forecastValue / presentValue methods on CrudeOilOptionProductPricer.
// ─────────────────────────────────────────────────────────────────────────────
@DisplayName("Black-76 Forward Pricing Tests")
public class Black76PricerTests {
 
    // ── Shared market inputs ──────────────────────────────────────────────────
    private BlackScholesPricer pricer;
 
    // Standard test parameters — WTI-style crude option
    private static final double FORWARD    = 92.50;   // from PriceCurve
    private static final double SPOT       = 91.00;   // current spot (different from forward)
    private static final double STRIKE     = 95.00;
    private static final double T          = 0.30;    // ~110 days
    private static final double RATE       = 0.053;
    private static final double VOL        = 0.28;
    private static final double DELTA      = 1e-4;    // tolerance for price comparisons
 
    @BeforeEach
    void setup() {
        pricer = new BlackScholesPricer();
    }
 
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("black76Call — Formula Correctness")
    class Black76CallFormula {
 
        @Test
        @DisplayName("Call price is positive for ITM forward")
        void call_positive_itmForward() {
            double price = pricer.black76Call(100.0, 95.0, T, RATE, VOL);
            assertTrue(price > 0, "ITM call must have positive value");
        }
 
        @Test
        @DisplayName("Call price is positive but small for deep OTM forward")
        void call_small_otmForward() {
            double price = pricer.black76Call(80.0, 95.0, T, RATE, VOL);
            assertTrue(price > 0 && price < 1.0,
                "Deep OTM call should be small but positive: " + price);
        }
 
        @Test
        @DisplayName("Call price equals discounted intrinsic for zero volatility ITM")
        
        void call_zeroVol_itm_equalsDiscountedIntrinsic() {
            double price    = pricer.black76Call(100.0, 95.0, T, RATE, 0.0);
            double expected = (100.0 - 95.0) * DiscountFactor.of(RATE, T);
            assertEquals(expected, price, DELTA,
                "Zero vol ITM call should be discounted intrinsic");
        }
 
        @Test
        @DisplayName("Call price is zero for zero volatility OTM forward")
        void call_zeroVol_otm_isZero() {
            double price = pricer.black76Call(90.0, 95.0, T, RATE, 0.0);
            assertEquals(0.0, price, DELTA, "Zero vol OTM call should be worthless");
        }
 
        @Test
        @DisplayName("Expired call returns intrinsic value, no discounting")
        void call_expired_returnsIntrinsic() {
            double price = pricer.black76Call(100.0, 95.0, 0.0, RATE, VOL);
            assertEquals(5.0, price, DELTA,
                "Expired ITM call should return raw intrinsic (100-95=5), not discounted");
        }
 
        @Test
        @DisplayName("Expired OTM call returns zero")
        void call_expired_otm_returnsZero() {
            double price = pricer.black76Call(90.0, 95.0, 0.0, RATE, VOL);
            assertEquals(0.0, price, DELTA);
        }
 
        @Test
        @DisplayName("Call price increases as forward increases (positive delta)")
        void call_increasesWithForward() {
            double priceLow  = pricer.black76Call(90.0, STRIKE, T, RATE, VOL);
            double priceHigh = pricer.black76Call(95.0, STRIKE, T, RATE, VOL);
            assertTrue(priceHigh > priceLow,
                "Call value must increase as forward price rises");
        }
 
        @Test
        @DisplayName("Call price decreases as strike increases")
        void call_decreasesWithStrike() {
            double priceLow  = pricer.black76Call(FORWARD, 90.0, T, RATE, VOL);
            double priceHigh = pricer.black76Call(FORWARD, 100.0, T, RATE, VOL);
            assertTrue(priceLow > priceHigh,
                "Call value must decrease as strike rises");
        }
 
        @Test
        @DisplayName("Call price increases with volatility")
        void call_increasesWithVolatility() {
            double priceLow  = pricer.black76Call(FORWARD, STRIKE, T, RATE, 0.20);
            double priceHigh = pricer.black76Call(FORWARD, STRIKE, T, RATE, 0.40);
            assertTrue(priceHigh > priceLow,
                "Higher volatility must produce higher call value");
        }
 
        @Test
        @DisplayName("Call price decreases with higher risk-free rate (discounts more)")
        void call_decreasesWithRate() {
            double priceLow  = pricer.black76Call(FORWARD, STRIKE, T, 0.01, VOL);
            double priceHigh = pricer.black76Call(FORWARD, STRIKE, T, 0.10, VOL);
            assertTrue(priceLow > priceHigh,
                "Higher rate increases discounting, reducing Black-76 call price");
        }
 
        @Test
        @DisplayName("presentValue = forecastValue × discountFactor exactly")
        void call_pvEqualsFvTimesDF() {
            double pv = pricer.black76Call(FORWARD, STRIKE, T, RATE, VOL);
            double fv = pricer.black76ForecastValue(FORWARD, STRIKE, T, VOL);
            double df = DiscountFactor.of(RATE, T);
 
            assertEquals(pv, fv * df, DELTA,
                "black76Call must equal black76ForecastValue × discountFactor");
        }
    }
 
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("black76Put — Formula Correctness")
    class Black76PutFormula {
 
        @Test
        @DisplayName("Put price is positive for OTM forward (forward < strike)")
        void put_positive_otmForward() {
            double price = pricer.black76Put(90.0, 95.0, T, RATE, VOL);
            assertTrue(price > 0, "OTM put (F < K) must have positive value");
        }
 
//        @Test
//        @DisplayName("Put price is positive but small for deep OTM forward")
//        void put_small_deepItm() {
//            double price = pricer.black76Put(110.0, 95.0, T, RATE, VOL);
//            assertTrue(price > 0 && price < 1.0,
//                "Deep ITM put (F >> K) should be small but positive: " + price);
//        }
        
        
        @Test
        @DisplayName("Put price is small for deep OTM forward (F >> K)")
        
        void put_small_deepOtm() {
        	
        	//F=110, K=95 - put is OTM, forward is well above strike
        	//Price should be positive but small relative to the forward
        	double price = pricer.black76Put(110.0, 95.0, T, RATE, VOL);
        	assertTrue(price > 0, "OTM put should have psoitive value due to remaining vol: "+ price);
        	
        	assertTrue(price < 5.0, "OTM put should be significantely cheaper than ATM: " + price);
        	//verify it is less than the equivalent OTM call(which benefits from F> K
        	double callPrice = pricer.black76Call(110.0, 95.0, T, RATE, VOL);
        	
        	assertTrue(price < callPrice, "When F > K, call should be worth more than put: put=" + price+" call=" + callPrice);
        	
        }
        
        @Test
        @DisplayName("Put price is large for deep ITM forward(F << K)")
        
        void put_large_deepItm() {
        	
        	//F=80, K=95 - put is ITM, forward is well below strike
        	
        	double price = pricer.black76Put(80.0, 95.0, T, RATE, VOL);
        	
        	double discountedIntrinsic = (95.0-80.0) * DiscountFactor.of(RATE, T);
        	
        	
        	assertTrue(price > discountedIntrinsic, "Deep ITM put must exceed discounted intrinsic due to remaining time value: " +
        	        "price=" + price + " intrinsic=" + discountedIntrinsic);
        	
        	assertTrue(price > 10.0,
        	        "Deep ITM put (F=80, K=95) should have substantial value: " + price);
        }
 
        @Test
        @DisplayName("Put price equals discounted intrinsic for zero vol OTM forward")
        void put_zeroVol_otm_equalsDiscountedIntrinsic() {
            // OTM put: forward=90, strike=95 → intrinsic = 95-90 = 5
            double price    = pricer.black76Put(90.0, 95.0, T, RATE, 0.0);
            double expected = (95.0 - 90.0) * DiscountFactor.of(RATE, T);
            assertEquals(expected, price, DELTA,
                "Zero vol OTM put should be discounted intrinsic — regression for missing df bug");
        }
 
        @Test
        @DisplayName("Put price is zero for zero vol ITM forward")
        void put_zeroVol_itm_isZero() {
            double price = pricer.black76Put(100.0, 95.0, T, RATE, 0.0);
            assertEquals(0.0, price, DELTA, "Zero vol ITM put should be worthless");
        }
 
        @Test
        @DisplayName("Expired ITM put returns intrinsic value")
        void put_expired_itm_returnsIntrinsic() {
            // F=90, K=95 → intrinsic = 5
            double price = pricer.black76Put(90.0, 95.0, 0.0, RATE, VOL);
            assertEquals(5.0, price, DELTA);
        }
 
        @Test
        @DisplayName("Expired OTM put returns zero")
        void put_expired_otm_returnsZero() {
            double price = pricer.black76Put(100.0, 95.0, 0.0, RATE, VOL);
            assertEquals(0.0, price, DELTA);
        }
 
        @Test
        @DisplayName("Put price decreases as forward increases")
        void put_decreasesWithForward() {
            double priceLow  = pricer.black76Put(90.0, STRIKE, T, RATE, VOL);
            double priceHigh = pricer.black76Put(100.0, STRIKE, T, RATE, VOL);
            assertTrue(priceLow > priceHigh,
                "Put value must decrease as forward price rises");
        }
 
        @Test
        @DisplayName("presentValue = forecastValue × discountFactor for put")
        void put_pvEqualsFvTimesDF() {
            double pv = pricer.black76Put(FORWARD, STRIKE, T, RATE, VOL);
            double fv = pricer.black76ForecastValuePut(FORWARD, STRIKE, T, VOL);
            double df = DiscountFactor.of(RATE, T);
 
            assertEquals(pv, fv * df, DELTA,
                "black76Put must equal black76ForecastValuePut × discountFactor");
        }
    }
 
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("Black-76 Put-Call Parity")
    class Black76PutCallParity {
 
        @Test
        @DisplayName("C - P = (F - K) × e^(-rT) for same inputs")
        void putCallParity_holds() {
            // C - P = (F - K) × discountFactor  for European options on futures
            double call = pricer.black76Call(FORWARD, STRIKE, T, RATE, VOL);
            double put  = pricer.black76Put(FORWARD, STRIKE, T, RATE, VOL);
            double df   = DiscountFactor.of(RATE, T);
 
            double lhs = call - put;
            double rhs = (FORWARD - STRIKE) * df;
 
            assertEquals(rhs, lhs, DELTA,
                "Black-76 put-call parity: C - P = (F - K) × e^(-rT)");
            
        }
 
        @Test
        @DisplayName("Put-call parity holds for ATM forward")
        void putCallParity_atm() {
            // When F = K, C - P = 0 (both sides of parity are zero)
            double call = pricer.black76Call(95.0, 95.0, T, RATE, VOL);
            double put  = pricer.black76Put(95.0, 95.0, T, RATE, VOL);
            double df   = DiscountFactor.of(RATE, T);
 
            assertEquals((95.0 - 95.0) * df, call - put, DELTA,
                "ATM put-call parity: C - P ≈ 0");
        }
 
        @Test
        @DisplayName("Put-call parity holds for deep ITM forward")
        void putCallParity_deepItm() {
            double F    = 110.0;
            double call = pricer.black76Call(F, STRIKE, T, RATE, VOL);
            double put  = pricer.black76Put(F, STRIKE, T, RATE, VOL);
            double df   = DiscountFactor.of(RATE, T);
 
            assertEquals((F - STRIKE) * df, call - put, DELTA);
        }
    }
 
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("black76ForecastValue — Undiscounted Payoff")
    class Black76ForecastValue {
 
        @Test
        @DisplayName("forecastValue has no discounting — fv > pv for positive rate and time")
        void forecastValue_greaterThanPresentValue() {
            double fv = pricer.black76ForecastValue(FORWARD, STRIKE, T, VOL);
            double pv = pricer.black76Call(FORWARD, STRIKE, T, RATE, VOL);
 
            assertTrue(fv > pv,
                "forecastValue must be larger than presentValue when r>0 and T>0");
        }
 
        @Test
        @DisplayName("forecastValue equals presentValue at expiry (df=1)")
        void forecastValue_equalsPresent_atExpiry() {
            // At T=0, no discounting, fv = pv = intrinsic
            double fv = pricer.black76ForecastValue(100.0, 95.0, 0.0, VOL);
            double pv = pricer.black76Call(100.0, 95.0, 0.0, RATE, VOL);
 
            assertEquals(fv, pv, DELTA,
                "At expiry, forecastValue must equal presentValue");
        }
 
        @Test
        @DisplayName("forecastValue has no rate parameter — rate-independent")
        void forecastValue_rateIndependent() {
            // forecastValue takes no riskFreeRate — verify it doesn't change with rate
            double fv1 = pricer.black76ForecastValue(FORWARD, STRIKE, T, VOL);
            double fv2 = pricer.black76ForecastValue(FORWARD, STRIKE, T, VOL);
            // Same inputs → same result (trivial but documents the design intent)
            assertEquals(fv1, fv2, 0.0);
        }
 
        @Test
        @DisplayName("forecastValue × discountFactor = black76Call exactly")
        void forecastValue_timesDF_equalsPresentValue() {
            double fv = pricer.black76ForecastValue(FORWARD, STRIKE, T, VOL);
            double df = DiscountFactor.of(RATE, T);
            double pv = pricer.black76Call(FORWARD, STRIKE, T, RATE, VOL);
 
            assertEquals(fv * df, pv, DELTA,
                "The invariant: presentValue = forecastValue × discountFactor");
        }
    }
 
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("Black-76 vs Black-Scholes — When Forward = Spot × e^(rT)")
    class Black76VsBlackScholes {
 
        @Test
        @DisplayName("Black-76 call matches Black-Scholes when F = S × e^(rT)")
        void black76_matchesBlackScholes_atFairForward() {
            // Cost-of-carry: fair forward = spot × e^(rT)
            double fairForward = SPOT * Math.exp(RATE * T);
 
            double bsPrice  = pricer.priceCall(SPOT, STRIKE, T, RATE, VOL);
            double b76Price = pricer.black76Call(fairForward, STRIKE, T, RATE, VOL);
 
            assertEquals(bsPrice, b76Price, DELTA,
                "Black-76 with fair forward must match Black-Scholes with spot: " +
                "BS=" + bsPrice + " B76=" + b76Price);
        }
 
        @Test
        @DisplayName("Black-76 put matches Black-Scholes when F = S × e^(rT)")
        void black76Put_matchesBlackScholes_atFairForward() {
            double fairForward = SPOT * Math.exp(RATE * T);
 
            double bsPrice  = pricer.pricePut(SPOT, STRIKE, T, RATE, VOL);
            double b76Price = pricer.black76Put(fairForward, STRIKE, T, RATE, VOL);
 
            assertEquals(bsPrice, b76Price, DELTA,
                "Black-76 put with fair forward must match Black-Scholes put with spot");
        }
 
        @Test
        @DisplayName("Black-76 differs from Black-Scholes when market is in backwardation (F < S)")
        void black76_differsByDesign_whenBackwardation() {
            // Crude oil is frequently in backwardation: F < S
            double backwardForward = SPOT * 0.98;  // F is 2% below spot — backwardation
 
            double bsPrice  = pricer.priceCall(SPOT, STRIKE, T, RATE, VOL);
            double b76Price = pricer.black76Call(backwardForward, STRIKE, T, RATE, VOL);
 
            assertNotEquals(bsPrice, b76Price, DELTA,
                "In backwardation, Black-76 and Black-Scholes must give different answers — " +
                "BS uses spot, Black-76 uses the actual market forward");
        }
 
        @Test
        @DisplayName("Black-76 call is lower than Black-Scholes in backwardation")
        void black76_lowerThanBS_inBackwardation() {
            // In backwardation F < S, so Black-76 uses a lower underlying price
            // → lower call value
            double backwardForward = SPOT * 0.96;
 
            double bsCall  = pricer.priceCall(SPOT, STRIKE, T, RATE, VOL);
            double b76Call = pricer.black76Call(backwardForward, STRIKE, T, RATE, VOL);
 
            assertTrue(b76Call < bsCall,
                "In backwardation: Black-76 call < Black-Scholes call because F < S");
        }
    }
 
    // ─────────────────────────────────────────────────────────────────────────
//    @Nested
//    @DisplayName("CrudeOilOptionProductPricer — Forward Pricing Integration")
//    class PricerIntegration {
// 
//        private CrudeOilOptionPricer productPricer;
//        private ResolvedCrudeOilOption      callOption;
//        private ResolvedCrudeOilOption      putOption;
// 
//        private static final double SCALED_QTY   = 10_000.0;  // 10 lots × 1000 barrels
//        private static final double FORWARD_PRICE = 92.50;
// 
//        @BeforeEach
//        void setupPricer() {
//            productPricer = new CrudeOilOptionPricer(
//                new BlackScholesPricer(),
//                new GreeksCalculator()
//            );
// 
//            callOption = buildResolved(PutCall.CALL, FORWARD_PRICE, STRIKE, T, RATE, VOL, SCALED_QTY);
//            putOption  = buildResolved(PutCall.PUT,  FORWARD_PRICE, STRIKE, T, RATE, VOL, SCALED_QTY);
//        }
// 
//        @Test
//        @DisplayName("presentValue is positive for long call position")
//        void pricer_presentValue_positive() {
//            CurrencyAmount pv = productPricer.presentValue(callOption);
//            assertTrue(pv.getAmount() > 0, "Long call present value must be positive");
//            assertEquals("USD", pv.getCurrency());
//        }
// 
//        @Test
//        @DisplayName("forecastValue is larger than presentValue for r>0 and T>0")
//        void pricer_forecastValue_greaterThanPresentValue() {
//            double fv = productPricer.forecastValue(callOption).getAmount();
//            double pv = productPricer.presentValue(callOption).getAmount();
// 
//            assertTrue(fv > pv,
//                "forecastValue must exceed presentValue when r>0 and T>0: fv=" + fv + " pv=" + pv);
//        }
// 
//        @Test
//        @DisplayName("presentValue = forecastValue × discountFactor invariant holds")
//        void pricer_pvEquality_invariant() {
//            double fv = productPricer.forecastValue(callOption).getAmount();
//            double pv = productPricer.presentValue(callOption).getAmount();
//            double df = callOption.getDiscountFactor();
// 
//            assertEquals(fv * df, pv, 0.01,
//                "presentValue must equal forecastValue × discountFactor");
//        }
// 
//        @Test
//        @DisplayName("forecastValue is computed directly from formula, not from pv/df")
//        void pricer_forecastValue_isDirectlyComputed() {
//            // If forecastValue were computed as pv/df, these would agree trivially.
//            // We verify forecastValue comes from black76ForecastValue directly.
//            double fvDirect = new BlackScholesPricer().black76ForecastValue(
//                FORWARD_PRICE, STRIKE, T, VOL
//            ) * SCALED_QTY;
// 
//            double fvPricer = productPricer.forecastValue(callOption).getAmount();
// 
//            assertEquals(fvDirect, fvPricer, 0.01,
//                "forecastValue must equal black76ForecastValue × scaledQuantity directly");
//        }
// 
//        @Test
//        @DisplayName("presentValue × scaledQuantity matches manual Black-76 price")
//        void pricer_presentValue_matchesManualCalculation() {
//            double manualPv = new BlackScholesPricer()
//                .black76Call(FORWARD_PRICE, STRIKE, T, RATE, VOL) * SCALED_QTY;
// 
//            double pricerPv = productPricer.presentValue(callOption).getAmount();
// 
//            assertEquals(manualPv, pricerPv, 0.01);
//        }
// 
//        @Test
//        @DisplayName("Put-call parity holds at pricer level: C - P = (F-K) × df × qty")
//        void pricer_putCallParity() {
//            double callPv = productPricer.presentValue(callOption).getAmount();
//            double putPv  = productPricer.presentValue(putOption).getAmount();
//            double df     = callOption.getDiscountFactor();
// 
//            double lhs = callPv - putPv;
//            double rhs = (FORWARD_PRICE - STRIKE) * df * SCALED_QTY;
// 
//            assertEquals(rhs, lhs, 0.10);
//        }
// 
//        @Test
//        @DisplayName("cashFlows returns CashFlow with correct forecastValue and discountFactor")
//        void pricer_cashFlows_structuredCorrectly() {
//            CashFlow flow = productPricer.cashFlows(callOption).getCashFlows().get(0);
// 
//            assertEquals(callOption.getExpiryDate(), flow.getPaymentDate());
//            assertEquals(callOption.getDiscountFactor(), flow.getDiscountFactor(), 1e-10);
// 
//            // fv × df = pv — the CashFlow invariant
//            double fv = flow.getForecastValue().getAmount();
//            double pv = flow.getPresentValue().getAmount();
//            double df = flow.getDiscountFactor();
// 
//            assertEquals(fv * df, pv, 0.01,
//                "CashFlow invariant: presentValue = forecastValue × discountFactor");
//        }
// 
//        @Test
//        @DisplayName("cashFlows forecastValue matches pricer forecastValue")
//        void pricer_cashFlows_forecastMatchesPricerForecast() {
//            double pricerFv   = productPricer.forecastValue(callOption).getAmount();
//            double cashFlowFv = productPricer.cashFlows(callOption)
//                                             .getCashFlows().get(0)
//                                             .getForecastValue()
//                                             .getAmount();
// 
//            assertEquals(pricerFv, cashFlowFv, 0.01,
//                "CashFlow forecastValue must match pricer.forecastValue()");
//        }
// 
//        // ── Helper to build a ResolvedCrudeOilOption directly ──────────────
//        private ResolvedCrudeOilOption buildResolved(
//            PutCall putCall, double forward, double strike, double T,
//            double rate, double vol, double scaledQty
//        ) {
//            LocalDate expiry     = LocalDate.now().plusDays((long)(T * 365));
//            LocalDate settlement = LocalDate.now().plusDays(2);
//            double df            = DiscountFactor.of(rate, T);
// 
//            return new ResolvedCrudeOilOption(
//                strike, putCall, "USD",
//                scaledQty, T, vol, rate,
//                forward,       // ← forwardPrice, not spot
//                settlement, expiry, df
//            );
//        }
    }

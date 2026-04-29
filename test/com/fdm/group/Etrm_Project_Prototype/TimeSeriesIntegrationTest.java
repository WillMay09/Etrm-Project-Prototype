package com.fdm.group.Etrm_Project_Prototype;
 
import static org.junit.jupiter.api.Assertions.*;
 
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
 
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
 
/**
 * Integration tests for TimeSeries class
 * These tests simulate real-world scenarios and workflows
 */
@DisplayName("TimeSeries Integration Tests")
class TimeSeriesIntegrationTest {
    
    // =========================================================================
    // Real-World Scenario Tests
    // =========================================================================
    
    @Test
    @DisplayName("Should support complete volatility calculation workflow")
    void testVolatilityCalculationWorkflow() {
        // Build 252 trading days of crude oil prices
        TimeSeries.Builder builder = TimeSeries.builder().commodity("CRUDE_OIL");
        
        LocalDate startDate = LocalDate.of(2025, 1, 2);
        double basePrice = 85.0;
        
        // Simulate realistic price movements
        for (int i = 0; i < 252; i++) {
            LocalDate date = startDate.plusDays(i);
            
            // Skip weekends
            if (date.getDayOfWeek().getValue() <= 5) {
                // Add some randomness: +/- 2%
                double change = (Math.random() - 0.5) * 0.04;
                double price = basePrice * (1 + change);
                builder.addPoint(date, price);
                basePrice = price;  // Walk forward
            }
        }
        
        TimeSeries series = builder.createTimeSeries();
        
        // Calculate 30-day historical volatility
        TimeSeries last30Days = series.tailSeries(30);
        double volatility = last30Days.calculateVolatility(29);
        
        // Verify reasonable volatility (typically 15-40% for commodities)
        assertTrue(volatility > 0.10, "Volatility should be above 10%");
        assertTrue(volatility < 0.80, "Volatility should be below 80%");
        
        // Verify can also calculate different time windows
        TimeSeries last90Days = series.tailSeries(90);
        double vol90 = last90Days.calculateVolatility(89);
         assertNotEquals(volatility, vol90, "Different time windows should give different volatility");
    }
    
    @Test
    @DisplayName("Should support P&L attribution workflow")
    void testPnLAttributionWorkflow() {
        // Scenario: Calculate daily P&L for oil position
        TimeSeries prices = TimeSeries.builder()
            .commodity("CRUDE_OIL")
            .addPoint(LocalDate.of(2026, 1, 2), 85.00)  // Friday
            .addPoint(LocalDate.of(2026, 1, 5), 86.50)  // Monday
            .addPoint(LocalDate.of(2026, 1, 6), 84.20)  // Tuesday
            .addPoint(LocalDate.of(2026, 1, 7), 87.30)  // Wednesday
            .createTimeSeries();
        
        // Position: Long 100,000 barrels
        double position = 100_000;
        
        // Calculate P&L for each day
        LocalDate[] dates = {
            LocalDate.of(2026, 1, 5),
            LocalDate.of(2026, 1, 6),
            LocalDate.of(2026, 1, 7)
        };
        
        double totalPnL = 0;
        
        for (LocalDate date : dates) {
            LocalDate prevDate = date.minusDays(1);
            // Skip weekends
            while (prevDate.getDayOfWeek().getValue() > 5) {
                prevDate = prevDate.minusDays(1);
            }
            
            double todayPrice = prices.getValue(date).orElse(0);
            double yesterdayPrice = prices.getValue(prevDate).orElse(0);
            
            double dailyPnL = (todayPrice - yesterdayPrice) * position;
            totalPnL += dailyPnL;
        }
        
        // Monday: (86.50 - 85.00) * 100k = +$150,000
        // Tuesday: (84.20 - 86.50) * 100k = -$230,000
        // Wednesday: (87.30 - 84.20) * 100k = +$310,000
        // Total: $230,000
        
        assertEquals(230_000, totalPnL, 1.0);
    }
    
    @Test
    @DisplayName("Should support scenario analysis with price shocks")
    void testScenarioAnalysisWorkflow() {
        // Base case: Current prices
        TimeSeries baseCase = TimeSeries.builder()
            .commodity("CRUDE_OIL")
            .addPoint(LocalDate.of(2026, 1, 2), 85.00)
            .addPoint(LocalDate.of(2026, 1, 5), 86.00)
            .addPoint(LocalDate.of(2026, 1, 6), 84.50)
            .createTimeSeries();
        
        // Scenario 1: +10% price shock
        TimeSeries upScenario = baseCase.mapValues(price -> price * 1.10);
        
        // Scenario 2: -10% price shock
        TimeSeries downScenario = baseCase.mapValues(price -> price * 0.90);
        
        // Scenario 3: +$5/barrel risk premium
        TimeSeries premiumScenario = baseCase.mapValues(price -> price + 5.0);
        
        // Verify transformations
        assertEquals(93.50, upScenario.getValue(LocalDate.of(2026, 1, 2)).getAsDouble(), 0.01);
        assertEquals(76.50, downScenario.getValue(LocalDate.of(2026, 1, 2)).getAsDouble(), 0.01);
        assertEquals(90.00, premiumScenario.getValue(LocalDate.of(2026, 1, 2)).getAsDouble(), 0.01);
        
        // Calculate impact on portfolio value
        double position = 100_000;  // barrels
        
        double baseValue = baseCase.meanValue() * position;
        double upValue = upScenario.meanValue() * position;
        double downValue = downScenario.meanValue() * position;
        
        double upImpact = upValue - baseValue;
        double downImpact = downValue - baseValue;
        
        // Verify symmetric impact
        assertEquals(-downImpact, upImpact, 1.0);
    }
    
    @Test
    @DisplayName("Should support rolling window analysis")
    void testRollingWindowAnalysis() {
        // Create time series with trend
        TimeSeries.Builder builder = TimeSeries.builder().commodity("TRENDING");
        
        LocalDate startDate = LocalDate.of(2025, 1, 2);
        
        // Simulate upward trend
        for (int i = 0; i < 90; i++) {
            LocalDate date = startDate.plusDays(i);
            if (date.getDayOfWeek().getValue() <= 5) {
                double price = 80.0 + (i * 0.10);  // Gradual increase
                builder.addPoint(date, price);
            }
        }
        
        TimeSeries series = builder.createTimeSeries();
        
        // Calculate rolling 30-day average
        List<Double> rollingAverages = new ArrayList<>();
        
        int windowSize = 30;
        for (int i = windowSize; i < series.size(); i++) {
            TimeSeries window = series.headSeries(i).tailSeries(windowSize);
            rollingAverages.add(window.meanValue());
        }
        
        // Verify increasing trend in rolling averages
        for (int i = 1; i < rollingAverages.size(); i++) {
            assertTrue(rollingAverages.get(i) >= rollingAverages.get(i - 1),
                "Rolling average should show upward trend");
        }
    }
    
    @Test
    @DisplayName("Should support correlation calculation between two commodities")
    void testCommodityCorrelation() {
        // Crude oil prices
        TimeSeries crude = TimeSeries.builder()
            .commodity("CRUDE_OIL")
            .addPoint(LocalDate.of(2026, 1, 2), 85.0)
            .addPoint(LocalDate.of(2026, 1, 5), 87.0)
            .addPoint(LocalDate.of(2026, 1, 6), 84.0)
            .addPoint(LocalDate.of(2026, 1, 7), 86.0)
            .addPoint(LocalDate.of(2026, 1, 8), 88.0)
            .createTimeSeries();
        
        // Natural gas prices (correlated with oil)
        TimeSeries natGas = TimeSeries.builder()
            .commodity("NATURAL_GAS")
            .addPoint(LocalDate.of(2026, 1, 2), 3.20)
            .addPoint(LocalDate.of(2026, 1, 5), 3.30)
            .addPoint(LocalDate.of(2026, 1, 6), 3.15)
            .addPoint(LocalDate.of(2026, 1, 7), 3.25)
            .addPoint(LocalDate.of(2026, 1, 8), 3.35)
            .createTimeSeries();
        
        // Calculate means
        double crudeMean = crude.meanValue();
        double gasMean = natGas.meanValue();
        
        // Calculate covariance (manually for demo)
        List<LocalDate> dates = List.of(
            LocalDate.of(2026, 1, 2),
            LocalDate.of(2026, 1, 5),
            LocalDate.of(2026, 1, 6),
            LocalDate.of(2026, 1, 7),
            LocalDate.of(2026, 1, 8)
        );
        
        double covariance = 0;
        for (LocalDate date : dates) {
            double crudeDeviation = crude.getValue(date).getAsDouble() - crudeMean;
            double gasDeviation = natGas.getValue(date).getAsDouble() - gasMean;
            covariance += crudeDeviation * gasDeviation;
        }
        covariance /= (dates.size() - 1);
        
        // Calculate correlation
        double crudeStdDev = crude.standardDeviation();
        double gasStdDev = natGas.standardDeviation();
        double correlation = covariance / (crudeStdDev * gasStdDev);
        
        // Verify positive correlation
        assertTrue(correlation > 0, "Oil and gas should have positive correlation");
        assertTrue(correlation <= 1.0, "Correlation must be <= 1");
    }
    
    @Test
    @DisplayName("Should support gap analysis and data quality checks")
    void testDataQualityAnalysis() {
        // Create series with gaps
        TimeSeries.Builder builder = TimeSeries.builder().commodity("GAPPY");
        
        builder.addPoint(LocalDate.of(2026, 1, 2), 85.0)   // Jan 2 (Friday)
               .addPoint(LocalDate.of(2026, 1, 3), 86.0)   // Jan 3 (Saturday)
               // Gap: Jan 4-10 missing (7 days)
               .addPoint(LocalDate.of(2026, 1, 11), 87.0)  // Jan 11 (Sunday)
               .addPoint(LocalDate.of(2026, 1, 12), 88.0); // Jan 12 (Monday)
        
        TimeSeries series = builder.createTimeSeries();
        
        // Identify gaps
        LocalDate earliest = series.getEarliestDate();  // Jan 2
        LocalDate latest = series.getLatestDate();      // Jan 12
        
        // Calculate calendar days in range
        // Jan 2 to Jan 12 = 10 days between + 1 = 11 total days
        int totalCalendarDays = (int) java.time.temporal.ChronoUnit.DAYS.between(earliest, latest) + 1;
        int actualDataPoints = series.size();
        int missingDays = totalCalendarDays - actualDataPoints;
        
        // We have: Jan 2, Jan 3, Jan 11, Jan 12 = 4 points
        // Total days: 11
        // Missing: 11 - 4 = 7 days (Jan 4, 5, 6, 7, 8, 9, 10)
        assertEquals(11, totalCalendarDays, "Should have 11 total calendar days");
        assertEquals(4, actualDataPoints, "Should have 4 actual data points");
        assertEquals(7, missingDays, "Should detect 7 days of missing data");
        
        // Verify specific dates are missing
        for (int i = 4; i <= 10; i++) {
            LocalDate missingDate = LocalDate.of(2026, 1, i);
            assertFalse(series.containsDate(missingDate), 
                "Date " + missingDate + " should be missing");
        }
        
        // Verify dates we have are present
        assertTrue(series.containsDate(LocalDate.of(2026, 1, 2)));
        assertTrue(series.containsDate(LocalDate.of(2026, 1, 3)));
        assertTrue(series.containsDate(LocalDate.of(2026, 1, 11)));
        assertTrue(series.containsDate(LocalDate.of(2026, 1, 12)));
    }
    
    @Test
    @DisplayName("Should support Value at Risk (VaR) calculation")
    void testValueAtRiskCalculation() {
        // Create series for VaR calculation
        TimeSeries.Builder builder = TimeSeries.builder().commodity("CRUDE_OIL");
        
        LocalDate startDate = LocalDate.of(2025, 1, 2);
        double[] prices = {
            85.0, 86.0, 84.5, 87.0, 85.5, 88.0, 86.5, 87.5, 86.0, 89.0,
            87.0, 88.5, 86.5, 90.0, 88.0, 87.0, 89.5, 88.5, 87.5, 91.0
        };
        
        for (int i = 0; i < prices.length; i++) {
            builder.addPoint(startDate.plusDays(i), prices[i]);
        }
        
        TimeSeries series = builder.createTimeSeries();
        
        // Calculate returns
        List<Double> returns = new ArrayList<>();
        for (int i = 1; i < prices.length; i++) {
            double ret = (prices[i] - prices[i - 1]) / prices[i - 1];
            returns.add(ret);
        }
        
        // Calculate volatility (standard deviation of returns)
        double mean = returns.stream().mapToDouble(d -> d).average().orElse(0);
        double variance = returns.stream()
            .mapToDouble(r -> Math.pow(r - mean, 2))
            .average()
            .orElse(0);
        double volatility = Math.sqrt(variance);
        
        // 99% VaR (2.33 standard deviations)
        double currentPrice = prices[prices.length - 1];
        double portfolioValue = currentPrice * 100_000;  // 100k barrels
        double var99 = portfolioValue * volatility * 2.33;
        
        // Verify VaR is reasonable (5-15% of portfolio value)
        assertTrue(var99 > portfolioValue * 0.01, "VaR should be at least 1%");
        assertTrue(var99 < portfolioValue * 0.20, "VaR should be under 20%");
    }
    
    @Test
    @DisplayName("Should support basis calculation (spot vs forward)")
    void testBasisCalculation() {
        // Spot prices
        TimeSeries spotPrices = TimeSeries.builder()
            .commodity("CRUDE_OIL_SPOT")
            .addPoint(LocalDate.of(2026, 1, 2), 85.00)
            .addPoint(LocalDate.of(2026, 1, 5), 85.50)
            .addPoint(LocalDate.of(2026, 1, 6), 84.80)
            .createTimeSeries();
        
        // Forward prices (typically higher due to storage costs)
        TimeSeries forwardPrices = TimeSeries.builder()
            .commodity("CRUDE_OIL_3M_FORWARD")
            .addPoint(LocalDate.of(2026, 1, 2), 87.00)
            .addPoint(LocalDate.of(2026, 1, 5), 87.50)
            .addPoint(LocalDate.of(2026, 1, 6), 86.80)
            .createTimeSeries();
        
        // Calculate basis (forward - spot)
        LocalDate[] dates = {
            LocalDate.of(2026, 1, 2),
            LocalDate.of(2026, 1, 5),
            LocalDate.of(2026, 1, 6)
        };
        
        for (LocalDate date : dates) {
            double spot = spotPrices.getValue(date).getAsDouble();
            double forward = forwardPrices.getValue(date).getAsDouble();
            double basis = forward - spot;
            
            // Verify market is in contango (forward > spot)
            assertTrue(basis > 0, "Market should be in contango");
            assertTrue(basis < 5.0, "Basis should be reasonable");
        }
    }
    
    @Test
    @DisplayName("Should support historical backtest of trading strategy")
    void testTradingStrategyBacktest() {
        // Create realistic price series
        TimeSeries.Builder builder = TimeSeries.builder().commodity("CRUDE_OIL");
        
        LocalDate startDate = LocalDate.of(2025, 1, 2);
        double[] prices = {
            85.0, 84.5, 86.0, 87.5, 86.0, 88.0, 87.0, 89.0, 88.5, 90.0,
            89.0, 91.0, 90.5, 92.0, 91.0, 93.0, 92.5, 91.5, 93.5, 92.0
        };
        
        for (int i = 0; i < prices.length; i++) {
            builder.addPoint(startDate.plusDays(i), prices[i]);
        }
        
        TimeSeries series = builder.createTimeSeries();
        
        // Simple Moving Average (SMA) crossover strategy
        // Buy when price > 5-day SMA, sell when price < 5-day SMA
        
        int position = 0;  // 0 = no position, 1 = long
        double cash = 100_000;  // Starting capital
        double shares = 0;
        
        for (int i = 5; i < prices.length; i++) {
            // Calculate 5-day SMA
            TimeSeries window = series.headSeries(i).tailSeries(5);
            double sma = window.meanValue();
            double currentPrice = prices[i];
            
            // Trading logic
            if (currentPrice > sma && position == 0) {
                // Buy signal
                shares = cash / currentPrice;
                cash = 0;
                position = 1;
            } else if (currentPrice < sma && position == 1) {
                // Sell signal
                cash = shares * currentPrice;
                shares = 0;
                position = 0;
            }
        }
        
        // Close position at end
        if (position == 1) {
            cash = shares * prices[prices.length - 1];
        }
        
        // Calculate return
        double returnPct = (cash - 100_000) / 100_000;
        
        // Strategy should make some return (though not guaranteed!)
        assertTrue(returnPct > -0.50, "Strategy shouldn't lose more than 50%");
        assertTrue(returnPct < 2.0, "Strategy return should be reasonable");
    }
}
 
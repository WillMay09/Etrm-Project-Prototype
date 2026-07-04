package com.fdm.group.Etrm_Project_Prototype;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class TradeInfo {

	private final String standardId;
	private final String counterparty;
	private final LocalDate tradeDate;
	private final LocalTime tradeTime;
	private final LocalDate settlementDate;
	private final String book;
	private final String trader;
	private final Map<String, String> attributes;

	// private constructor
	private TradeInfo(Builder builder) {

		this.standardId = builder.standardId;
		this.counterparty = builder.counterparty;
		this.tradeDate = builder.tradeDate;
		this.tradeTime = builder.tradeTime;
		this.settlementDate = builder.settlementDate;
		this.book = builder.book;
		this.trader = builder.trader;
		// defensive copy so caller can't mutate our internals
		this.attributes = Map.copyOf(builder.attributes);

	}
	
	// =========================================================================
		// Getters - Optional Fields
		// =========================================================================
	public Optional<String> getStandardId(){
		
		return Optional.ofNullable(standardId);
	}
	public Optional<String>    getCounterparty()   { return Optional.ofNullable(counterparty); }
    public Optional<LocalDate> getTradeDate()      { return Optional.ofNullable(tradeDate); }
    public Optional<LocalTime> getTradeTime()      { return Optional.ofNullable(tradeTime); }
    public Optional<LocalDate> getSettlementDate() { return Optional.ofNullable(settlementDate); }
    public Optional<String>    getBook()           { return Optional.ofNullable(book); }
    public Optional<String>    getTrader()         { return Optional.ofNullable(trader); }
    public Map<String, String> getAttributes()     { return attributes; }  // already immutable

	
    // ── With methods return new instances, original unchanged ────────
    
    public TradeInfo withStandardId(String standardId) {
    	
    	return toBuilder().standardId(standardId).build();
    }
    
    
    public TradeInfo withSettlementDate(LocalDate newSettlementDate) {
    	
    	return toBuilder().settlementDate(newSettlementDate).build();
    }
    
    public TradeInfo withBook(String newBook) {
    	
    	
    	return toBuilder().book(newBook).build();
    }
    
    public TradeInfo withTrader(String newTrader) {
    	
    	return toBuilder().trader(newTrader).build();
    }
    
    
    public TradeInfo withAttributes(String key, String value) {
    	
    	Map<String, String> updated = new HashMap<>(attributes);
    	
    	updated.put(key, value);
    	
    	return toBuilder().attributes(updated).build();
    	
    }
    
 // Fills in only null fields from the other TradeInfo
    public TradeInfo combinedWith(TradeInfo other) {
    	
    	Builder b = toBuilder();
    	
    	if(standardId == null) {other.getStandardId().ifPresent(b::standardId);}
    	if (counterparty == null)   other.getCounterparty().ifPresent(b::counterparty);
        if (tradeDate == null)      other.getTradeDate().ifPresent(b::tradeDate);
        if (settlementDate == null) other.getSettlementDate().ifPresent(b::settlementDate);
        if (book == null)           other.getBook().ifPresent(b::book);
        return b.build();
    }
    
    
    
    
    public Builder toBuilder() {
    	
    	return new Builder()
    			.standardId(standardId)
    			.counterparty(counterparty)
    			.tradeDate(tradeDate)
    			.tradeTime(tradeTime)
    			.settlementDate(settlementDate)
    			.book(book)
    			.trader(trader)
    			.attributes(new HashMap<>(attributes));
    	
    }
    
    
	
	// =========================================================================
	// Static Factory
	// =========================================================================

	/**
	 * Creates a new Builder for TradeInfo
	 * 
	 * @return New Builder instance
	 */
	public static Builder builder() {

		return new Builder();
	}

	public static class Builder {

		private String standardId;
		private String counterparty;
		private LocalDate tradeDate;
		private LocalTime tradeTime;
		private LocalDate settlementDate;
		private String book;
		private String trader;
		private Map<String, String> attributes = new HashMap<>();
		
		private Builder() {
			
			
		}

		public Builder standardId(String standardId2) {

			this.standardId = standardId2;

			return this;
		}
		
		

		public Builder counterparty(String counterparty) {

			this.counterparty = counterparty;
			return this;
		}

		public Builder tradeDate(LocalDate tradeDate) {

			this.tradeDate = tradeDate;
			return this;
		}

		public Builder tradeTime(LocalTime tradeTime) {

			this.tradeTime = tradeTime;
			return this;
		}

		public Builder settlementDate(LocalDate settlementDate) {

			this.settlementDate = settlementDate;
			return this;
		}

		public Builder book(String book) {
			this.book = book;
			return this;
		}

		public Builder trader(String trader) {
			this.trader = trader;
			return this;
		}
		
		public Builder defaultsettlement() {
			
			if(tradeDate != null) {
				
				this.settlementDate = LocalDate.now().plusDays(2);
			}
			return this;
			
		}

		public Builder addAttribute(String key, String value) {

			attributes.put(key, value);
			return this;
		}

		private Builder attributes(Map<String, String> attributes) {

			this.attributes = attributes;
			return this;
		}

		public TradeInfo build() {

			return new TradeInfo(this);
		}

		@Override
		public String toString() {
			return "TradeInfo{" + "id=" + standardId + ", counterparty=" + counterparty + ", tradeDate=" + tradeDate
					+ ", settlementDate=" + settlementDate + ", book=" + book + '}';
		}


	}
}

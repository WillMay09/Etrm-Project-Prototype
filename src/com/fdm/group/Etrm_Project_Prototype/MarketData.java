package com.fdm.group.Etrm_Project_Prototype;

import java.time.LocalDate;
import java.util.Optional;

public interface MarketData {
	
	LocalDate getValuationDate();
	
	
	//Use Id to get value
	
	<T> T getValue(MarketDataId<T> id);
	
	//optional lookup
	<T> Optional<T> findValue(MarketDataId<T> id);
	
	//check existance
	boolean containsValue(MarketDataId<?> id);
	
	
	//Historical data
	//LocalDateDoubleTimeSeries getTimeSeries(ObservableId id);
	

}

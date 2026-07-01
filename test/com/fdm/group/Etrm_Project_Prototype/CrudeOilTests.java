package com.fdm.group.Etrm_Project_Prototype;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("CrudeOilOption Tests")
public class CrudeOilTests {

	@BeforeEach
	void setup() {

	}

	@Test
	void builder_requires_strike() {

		assertThrows(IllegalArgumentException.class, () -> {

			CrudeOilOption.builder().call().expiryDate(LocalDate.now().plusMonths(3)).strike(0.0).build();

		});

	}

	@SuppressWarnings("deprecation")
	@Test
	void builder_defaults_contractSize1000() {

		CrudeOilOption option = CrudeOilOption.builder().put().strike(4.57).currency("USD")
				.expiryDate(LocalDate.now().plusMonths(4)).build();

		assertEquals(option.getContractSize(), 1_000.0);

	}

	@Test
	void isCall_isPut_areMautallyExclusive() {

		CrudeOilOption option = CrudeOilOption.builder().call().strike(80).expiryDate(LocalDate.now().plusMonths(3))
				.build();

		assertTrue(option.isCall());

		assertFalse(option.isPut());
	}

}

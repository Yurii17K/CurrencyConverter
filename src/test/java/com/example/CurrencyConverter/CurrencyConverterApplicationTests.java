package com.example.CurrencyConverter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

@SpringBootTest
class CurrencyConverterApplicationTests {

	@Autowired
	ExchangeService exchangeService;

	@Test
	void contextLoads() {
	}

	@Test
	void updateExchangeRatesNotNullTest() {
		Assertions.assertTrue(NBPWebApiRequests.updateExchangeRates());
	}

	@Test
	void isExchangeRateUpdatedTodayTestTrue_WhenDateEarlierThan24H() {
		Assertions.assertTrue(exchangeService
				.isExchangeRateUpdatedToday(new Date(System.currentTimeMillis() - 6 * 60 * 60 * 1000)));
		Assertions.assertTrue(exchangeService
				.isExchangeRateUpdatedToday(new Date(System.currentTimeMillis() - 12 * 60 * 60 * 1000)));
		Assertions.assertTrue(exchangeService
				.isExchangeRateUpdatedToday(new Date(System.currentTimeMillis() - 18 * 60 * 60 * 1000)));
		Assertions.assertTrue(exchangeService
				.isExchangeRateUpdatedToday(new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000)));
	}

	@Test
	void isExchangeRateUpdatedTodayTestFalse_WhenDateLaterThan24H() {
		Assertions.assertFalse(exchangeService
				.isExchangeRateUpdatedToday(new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000 - 1)));

	}

	@Test
	void convertCurrencyTestTrue() {
		Assertions.assertTrue(exchangeService
				.convertCurrency(BigDecimal.valueOf(1000), "pln", "uah")
				.matches("^\\d*\\.?\\d*$"));
		Assertions.assertTrue(exchangeService
				.convertCurrency(BigDecimal.valueOf(1000), "uah", "pln")
				.matches("^\\d*\\.?\\d*$"));
		Assertions.assertTrue(exchangeService
				.convertCurrency(BigDecimal.valueOf(1000), "usd", "pln")
				.matches("^\\d*\\.?\\d*$"));
	}

	@Test
	void convertCurrencyTestTrue_WhenCurrenciesSame() {
		Assertions.assertEquals("1000",
				exchangeService.convertCurrency(BigDecimal.valueOf(1000), "usd", "usd"));
		Assertions.assertEquals("1000",
				exchangeService.convertCurrency(BigDecimal.valueOf(1000), "pln", "pln"));

	}

	@Test
	void convertCurrencyTestFalse_WhenNoSuchTargetCurrency() {
		Assertions.assertEquals("No such target currency",
				exchangeService.convertCurrency(BigDecimal.valueOf(1000), "pln", "111111"));
	}

	@Test
	void convertCurrencyTestFalse_WhenNoSuchBaseCurrency() {
		Assertions.assertEquals("No such base currency",
				exchangeService.convertCurrency(BigDecimal.valueOf(1000), "11111", "pln"));
	}

	@Test
	void convertCurrencyTestFalse_WhenCurrenciesNotFound() {
		Assertions.assertEquals("Currency/ies not found",
				exchangeService.convertCurrency(BigDecimal.valueOf(1000), "11111", "usd"));
		Assertions.assertEquals("Currency/ies not found",
				exchangeService.convertCurrency(BigDecimal.valueOf(1000), "usd", "1111"));
		Assertions.assertEquals("Currency/ies not found",
				exchangeService.convertCurrency(BigDecimal.valueOf(1000), "", ""));
	}
}

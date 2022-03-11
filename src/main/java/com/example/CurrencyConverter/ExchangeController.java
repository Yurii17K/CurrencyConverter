package com.example.CurrencyConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/currency")
public class ExchangeController {

    private final Logger logger = LoggerFactory.getLogger(ExchangeController.class);
    private final ExchangeService exchangeService;

    // autowire the main service
    public ExchangeController(ExchangeService exchangeService) {
        this.exchangeService = exchangeService;
    }

    @GetMapping(value = "/convert", params = {"currencyAmount", "baseCurrency", "targetCurrency"})
    public ResponseEntity<String> convertCurrency(
            @RequestParam("currencyAmount") BigDecimal currencyAmount,
            @RequestParam("baseCurrency") String baseCurrency,
            @RequestParam("targetCurrency") String targetCurrency
    ) {
        return new ResponseEntity<>(exchangeService.convertCurrency(currencyAmount, baseCurrency, targetCurrency),
                HttpStatus.OK);
    }
}

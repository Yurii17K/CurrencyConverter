package com.example.CurrencyConverter;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
public class ExchangeService {

    private final Logger logger = LoggerFactory.getLogger(ExchangeService.class);
    public static Date timeOfCurrencyUpdate;
    public static JSONArray exchangeRatesJSON;

    public ExchangeService() {
    }

    public String convertCurrency(BigDecimal currencyAmount, String baseCurrency, String targetCurrency) {
        // check if currency rate has been updated today and update if needed
        if (timeOfCurrencyUpdate == null || !isExchangeRateUpdatedToday(timeOfCurrencyUpdate)) {
            NBPWebApiRequests.updateExchangeRates();
        }

        // getting the array of rates
        JSONArray ratesArray = (JSONArray) ((JSONObject) exchangeRatesJSON.get(0)).get("rates");

        // if we want to convert PLN to other currencies -> just use the exchange rate from the TABLE A
        if (baseCurrency.equalsIgnoreCase("PLN")) {

            return convertPLNToTargetCurrency(currencyAmount, baseCurrency, targetCurrency, ratesArray);

        } else // if the targetCurrency is pln flip division to multiplication and search for baseCurrency instead
            if (targetCurrency.equalsIgnoreCase("pln")) {

                return convertBaseCurrencyToPLN(currencyAmount, baseCurrency, targetCurrency, ratesArray);

            } else  { // if neither of the currencies is PLN -> calculate an exchange rate

                return convertBothNonPLN(currencyAmount, baseCurrency, targetCurrency, ratesArray);

            }
    }

    private String convertBothNonPLN(BigDecimal currencyAmount, String baseCurrency, String targetCurrency, JSONArray ratesArray) {
        // exchange rates for input currencies
        BigDecimal plnToBaseCurrency = null;
        BigDecimal plnToTargetCurrency = null;

        // iterating through the current exchange rates to find both a baseCurrency and a targetCurrency
        for (Object object : ratesArray) {
            JSONObject rate = (JSONObject) object; // get object as a JSON object

            // if the current currency code matches with targetCurrency -> convert
            if (rate.get("code").toString().equalsIgnoreCase(baseCurrency)) {
                plnToBaseCurrency = BigDecimal.valueOf((Double) rate.get("mid"));
            }

            // if the current currency code matches with targetCurrency -> convert
            if (rate.get("code").toString().equalsIgnoreCase(targetCurrency)) {
                plnToTargetCurrency = BigDecimal.valueOf((Double) rate.get("mid"));
            }
        }

        // if baseCurrency or targetCurrency hasn't been found
        if (plnToBaseCurrency == null || plnToTargetCurrency == null) {
            return "Currency/ies not found";
        } else {

            // return the amount if baseCurrency equals targetCurrency
            if (baseCurrency.equalsIgnoreCase(targetCurrency)) {
                return String.valueOf(currencyAmount);
            }
        }

        // convert the baseCurrency to targetCurrency
        logger.info("Conversion from " + baseCurrency + " to " + targetCurrency + " executed");
        return String.valueOf(currencyAmount
                .divide(plnToTargetCurrency
                                .divide(plnToBaseCurrency, // get an exchange rate from baseCurrency to targetCurrency
                                        8,
                                        RoundingMode.DOWN),
                        6,
                        RoundingMode.DOWN));
    }

    private String convertBaseCurrencyToPLN(BigDecimal currencyAmount, String baseCurrency, String targetCurrency, JSONArray ratesArray) {
        // return the amount if baseCurrency equals targetCurrency
        if (baseCurrency.equalsIgnoreCase(targetCurrency)) {
            return String.valueOf(currencyAmount);
        }

        // iterating through the current exchange rates to find a baseCurrency
        for (Object object : ratesArray) {
            JSONObject rate = (JSONObject) object; // get object as a JSON object

            // if the current currency code matches with targetCurrency -> convert
            if (rate.get("code").toString().equalsIgnoreCase(baseCurrency)) {
                logger.info("Conversion from " + baseCurrency + " to PLN executed");
                return String.valueOf(currencyAmount.multiply(BigDecimal.valueOf((Double) rate.get("mid"))));
            }
        }

        return "No such base currency";
    }

    private String convertPLNToTargetCurrency(BigDecimal currencyAmount, String baseCurrency, String targetCurrency, JSONArray ratesArray) {
        // return the amount if baseCurrency equals targetCurrency
        if (baseCurrency.equalsIgnoreCase(targetCurrency)) {
            return String.valueOf(currencyAmount);
        }

        // iterating through the current exchange rates to find a targetCurrency
        for (Object object : ratesArray) {
            JSONObject rate = (JSONObject) object; // get object as a JSON object

            // if the current currency code matches with targetCurrency -> convert
            if (rate.get("code").toString().equalsIgnoreCase(targetCurrency)) {
                logger.info("Conversion from PLN to " + targetCurrency + " executed");
                return String.valueOf(currencyAmount.divide(BigDecimal.valueOf((Double) rate.get("mid")),
                        6, // precision of rounding
                        RoundingMode.DOWN));
            }
        }

        return "No such target currency";
    }

    // checks if the exchange rate has been updated today
    public boolean isExchangeRateUpdatedToday(Date date) {
        return !date.before(new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000));
    }
}

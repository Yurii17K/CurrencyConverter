package com.example.CurrencyConverter;


import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;

public class NBPWebApiRequests {

    private final static Logger logger = LoggerFactory.getLogger(NBPWebApiRequests.class);

    public static boolean updateExchangeRates() {
        // create a request to get exchange rates from the NBP Web Api
        HttpGet request = new HttpGet("http://api.nbp.pl/api/exchangerates/tables/A");

        // send the above request
        try (CloseableHttpResponse response = HttpClients.createDefault().execute(request)) {

            // get the body
            HttpEntity entity = response.getEntity();

            if (entity != null) {
                String result = EntityUtils.toString(entity);

                // write currency exchange rates to the exchangeRatesJSON obj in ExchangeService for further actions
                ExchangeService.exchangeRatesJSON = (JSONArray) new JSONParser().parse(result);
                logger.info("Currency exchange rates updated");

                // change the time of exchange rates update
                ExchangeService.timeOfCurrencyUpdate = new Date(System.currentTimeMillis());

                return true;

            } else {
                logger.error("No data was obtained from the NBP Web Api");
            }

        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        return false;
    }
}

package bot.exchange.service.rest;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class BankHttpService extends HttpService {

    public Map<String, BankValue> getBankRates() {
        String url = getApiUrlWithToken() + "/rates";
        log.info("Bank rates URL: " + url);
        ResponseEntity<String> bankRatesResponse = getRestTemplate().getForEntity(url, String.class);

        JsonElement ratesData = JsonParser.parseString(bankRatesResponse.getBody());
        Map<String, JsonElement> currencyMap = ratesData.getAsJsonArray().get(0).getAsJsonObject().asMap();
        Map<String, BankValue> bankValues = new HashMap<>();
        currencyMap.forEach((currency, value) -> {
            BigDecimal buy = new BigDecimal(value.getAsJsonObject().get("buy").toString().replaceAll("\"",""));
            BigDecimal sell = new BigDecimal(value.getAsJsonObject().get("sell").toString().replaceAll("\"",""));

            BankValue currencyValue = new BankValue(buy, sell);
            bankValues.put(currency, currencyValue);
        });
        return bankValues;
    }

    @Data
    @AllArgsConstructor
    public static class BankValue {
        private BigDecimal buy;
        private BigDecimal sell;
    }

}

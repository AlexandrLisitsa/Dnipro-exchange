package exchange.service.rest;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
@Slf4j
public class ClientHttpService extends HttpService {

    public boolean createClient(String phoneNumber) {

        JsonObject phoneJson = new JsonObject();
        phoneJson.add("phone", new JsonPrimitive(phoneNumber));

        HttpEntity<Object> body = new HttpEntity<>(phoneJson.toString());

        ResponseEntity<String> createResult = getRestTemplate().exchange(
                getApiUrl(),
                HttpMethod.POST,
                body,
                String.class
        );

        return createResult.getStatusCode() == HttpStatus.OK;
    }

    public ClientInfo getClientInfo(String phoneNumber) throws ClientNotFoundException {
        String url = getApiUrlWithToken() + "/client/" + phoneNumber;
        log.debug("Client info URL: " + url);
        ResponseEntity<Client> clientInfoResponse = getRestTemplate().getForEntity(url, Client.class);

        if (clientInfoResponse.getStatusCode() != HttpStatus.OK) {
            throw new ClientNotFoundException("Client with number " + phoneNumber + "doesn't exist.");
        }

        return Objects.requireNonNull(clientInfoResponse.getBody()).getClient();
    }

    public Map<String, CurrencyValue> getClientRates(String phoneNumber) {
        String url = getApiUrlWithToken() + "/client/" + phoneNumber + "/rates";
        log.debug("Client rates URL: " + url);
        ResponseEntity<String> ratesResponse = getRestTemplate().getForEntity(url, String.class);
        JsonElement ratesData = JsonParser.parseString(ratesResponse.getBody());
        Map<String, JsonElement> currencyMap = ratesData.getAsJsonObject().get("data").getAsJsonObject().asMap();
        Map<String, CurrencyValue> currencyValues = new HashMap<>();
        currencyMap.forEach((currency, value) -> {
            BigDecimal buy = new BigDecimal(value.getAsJsonObject().get("buy").toString());
            BigDecimal sell = new BigDecimal(value.getAsJsonObject().get("sell").toString());

            CurrencyValue currencyValue = new CurrencyValue(buy, sell);
            currencyValues.put(currency, currencyValue);
        });

        return currencyValues;
    }

    @Data
    @AllArgsConstructor
    public static class CurrencyValue {
        private BigDecimal buy;
        private BigDecimal sell;
    }

    @Data
    private static class Client {
        private ClientInfo client;
    }

    @Data
    public static class ClientInfo {
        private String phone;
        private double discount;
        private double fine;
        @Getter(AccessLevel.PRIVATE)
        private double ref_balance;
        @Getter(AccessLevel.PRIVATE)
        private double ref_link;

        public double getRefBalance() {
            return ref_balance;
        }

        public double getRefLink() {
            return ref_link;
        }
    }

    public static class ClientNotFoundException extends Exception {
        public ClientNotFoundException(String message) {
            super(message);
        }
    }

}

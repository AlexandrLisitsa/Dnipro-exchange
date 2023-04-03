package bot.exchange.service.rest;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class ClientHttpService extends HttpService {

    public boolean createClient(String phoneNumber) {

        JsonObject phoneJson = new JsonObject();
        phoneJson.add("phone", new JsonPrimitive(phoneNumber));

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", "application/json");

        HttpEntity<Object> body = new HttpEntity<>(phoneJson.toString(), httpHeaders);

        String url = getApiUrlWithToken() + "/client/create";
        log.info("Create client URL: " + url);

        ResponseEntity<String> createResult = getRestTemplate().exchange(
                url,
                HttpMethod.POST,
                body,
                String.class
        );

        return createResult.getStatusCode() == HttpStatus.OK;
    }

    public Optional<Client> getClientInfo(String phoneNumber) {
        String url = getApiUrlWithToken() + "/client/" + phoneNumber;
        ResponseEntity<Client> clientInfoResponse = null;
        log.info("Client info URL: " + url);
        try {
            clientInfoResponse = getRestTemplate().getForEntity(url, Client.class);
        } catch (Exception e) {
            return Optional.empty();
        }

        return Optional.ofNullable(clientInfoResponse.getBody());
    }

    @Data
    public static class Client {
        private String phone;
        private double discount;
        private double fine;
        @Getter(AccessLevel.PRIVATE)
        private double ref_balance;
        @Getter(AccessLevel.PRIVATE)
        private double ref_link;
        private Map<String, Discounts> rates;

        public double getRefBalance() {
            return ref_balance;
        }

        public double getRefLink() {
            return ref_link;
        }
    }

    @Data
    public static class Discounts {
        private String from;
        private String to;
        private String discount;
        private Map<String, Rates> rates;


    }

    @Data
    public static class Rates {
        private BigDecimal buy;
        private BigDecimal sell;
    }

}

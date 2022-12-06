package exchange.service.rest;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DepositHttpService extends HttpService {

    public String getDepositRules() {
        String url = getApiUrlWithToken() + "/deposit/rules";
        log.info("Deposit rules URL: " + url);
        ResponseEntity<String> response = getRestTemplate().getForEntity(url, String.class);

        JsonElement jsonElement = JsonParser.parseString(response.getBody());
        String rules = jsonElement.getAsJsonObject().get("data")
                .getAsJsonObject().get("settings")
                .getAsJsonObject()
                .get("text").getAsString();

        return rules;
    }

    public boolean createDeposit(String phone, String currency, String amount) {
        String url = getApiUrlWithToken() + "/deposit/create";
        log.info("Deposit create URL: " + url);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", "application/json");

        JsonObject depositBody = new JsonObject();
        depositBody.add("phone", new JsonPrimitive(phone));
        depositBody.add("currency", new JsonPrimitive(currency));
        depositBody.add("amount", new JsonPrimitive(amount));
        log.info("Deposit body: " + depositBody);

        HttpEntity<Object> body = new HttpEntity<>(depositBody.toString(), httpHeaders);

        ResponseEntity<String> response = getRestTemplate().exchange(
                url,
                HttpMethod.POST,
                body,
                String.class
        );

        return response.getStatusCode().is2xxSuccessful();
    }

}

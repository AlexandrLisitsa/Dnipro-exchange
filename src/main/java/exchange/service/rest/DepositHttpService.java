package exchange.service.rest;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Slf4j
@Service
public class DepositHttpService extends HttpService {

    public String getDepositRules() {
        String url = getApiUrlWithToken() + "/deposit/rules";
        log.debug("Deposit rules URL: " + url);
        ResponseEntity<String> response = getRestTemplate().getForEntity(url, String.class);
        return response.getBody();
    }

    public String createDeposit(String phone, String currency, String amount) {
        String url = getApiUrlWithToken() + "/deposit/create";
        log.debug("Deposit create URL: " + url);

        JsonObject depositBody = new JsonObject();
        depositBody.add("phone", new JsonPrimitive(phone));
        depositBody.add("currency", new JsonPrimitive(currency));
        depositBody.add("amount", new JsonPrimitive(amount));
        log.debug("Deposit body: " + depositBody);

        HttpEntity<Object> body = new HttpEntity<>(depositBody.toString());

        ResponseEntity<String> response = getRestTemplate().exchange(
                url,
                HttpMethod.POST,
                body,
                String.class
        );

        return "There should be a ticket";
    }

    @PostConstruct
    public void test() {
        createDeposit("+380634410488","USD", "1234");
    }

}

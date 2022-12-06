package exchange.service.rest;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class ExchangeHttpService extends HttpService {

    public boolean confirmExchange(Operation operation, int exchangerId) {
        String url = getApiUrlWithToken() + "/operation/confirm";
        log.info("Request operation confirm: " + url);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", "application/json");

        JsonObject body = new JsonObject();
        body.add("operation", gson.toJsonTree(operation));
        body.add("exchanger_id", new JsonPrimitive(exchangerId));
        log.info("operation confirm body: " + body);

        HttpEntity<String> request = new HttpEntity<>(body.toString(), httpHeaders);

        ResponseEntity<String> response = getRestTemplate().exchange(
                url,
                HttpMethod.POST,
                request,
                String.class
        );

        return response.getStatusCode().is2xxSuccessful();
    }

    public CommitExchangeResponse commitExchange(String phone, String direction, String amount) {
        String url = getApiUrlWithToken() + "/operation/create";
        log.info("Requesting operation commit: " + url);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", "application/json");

        JsonObject operationCommitBody = new JsonObject();
        operationCommitBody.add("direction", new JsonPrimitive(direction));
        operationCommitBody.add("phone", new JsonPrimitive(phone));
        operationCommitBody.add("amount", new JsonPrimitive(amount));

        HttpEntity<Object> request = new HttpEntity<>(operationCommitBody.toString(), httpHeaders);

        log.info("operation commit body: " + operationCommitBody);

        ResponseEntity<String> response = getRestTemplate().exchange(
                url,
                HttpMethod.POST,
                request,
                String.class
        );

        CommitExchangeResponse commitExchangeResponse = extractCommitResponse(response.getBody());
        log.info(commitExchangeResponse.toString());

        return commitExchangeResponse;
    }

    private CommitExchangeResponse extractCommitResponse(String json) {
        JsonElement response = JsonParser.parseString(json);
        JsonElement operation = response.getAsJsonObject().get("operation");

        CommitExchangeResponse commitExchangeResponse = new CommitExchangeResponse();
        Operation operationObj = gson.fromJson(operation.toString(), Operation.class);
        commitExchangeResponse.setOperation(operationObj);

        JsonElement exchangers = response.getAsJsonObject().get("avaliable_exchangers");
        List<Exchanger> exchangerList = new ArrayList<>();
        if (exchangers.isJsonArray()) {
            Exchanger[] exchangersObj = gson.fromJson(exchangers.toString(), Exchanger[].class);
            exchangerList.addAll(Arrays.asList(exchangersObj));
        } else {
            Exchanger exchanger = gson.fromJson(exchangers.toString(), Exchanger.class);
            exchangerList.add(exchanger);
        }
        commitExchangeResponse.setAvaliable_exchangers(exchangerList);

        return commitExchangeResponse;
    }

    @Data
    public static class CommitExchangeResponse {
        private Operation operation;
        private List<Exchanger> avaliable_exchangers;
    }

    @Data
    public static class Exchanger {
        private int id;
        private String title;
    }

    @Data
    public static class Operation {
        private int client;
        private String direction;
        private double amount;
    }

}

package bot.exchange.service.rest;

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

    public boolean rejectOperation(int code, String phone) {
        String url = getApiUrlWithToken() + "/operation/cancel";
        log.info("Request reject operation: " + url);

        return sendProlongRejectRequest(code, phone, url);
    }

    public boolean prolongOperation(int code, String phone) {
        String url = getApiUrlWithToken() + "/operation/extend";
        log.info("Request prolong operation: " + url);

        return sendProlongRejectRequest(code, phone, url);
    }

    private boolean sendProlongRejectRequest(int code, String phone, String url) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", "application/json");

        JsonObject body = getProlongRejectBody(code, phone);
        log.info("Body: " + body.toString());

        HttpEntity<String> request = new HttpEntity<>(body.toString(), httpHeaders);

        ResponseEntity<String> response = getRestTemplate().exchange(
                url,
                HttpMethod.POST,
                request,
                String.class
        );

        return response.getStatusCode().is2xxSuccessful();
    }

    private JsonObject getProlongRejectBody(int code, String phone) {
        JsonObject body = new JsonObject();
        body.add("operation_id", new JsonPrimitive(code));
        body.add("phone", new JsonPrimitive(phone));
        log.info("operation reject body: " + body);
        return body;
    }

    public ConfirmExchangeResponse confirmExchange(Operation operation, int exchangerId, boolean isEnough, String expires) {
        String url = getApiUrlWithToken() + "/operation/confirm";
        log.info("Request operation confirm: " + url);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", "application/json");

        JsonObject body = new JsonObject();
        body.add("operation", gson.toJsonTree(operation));
        body.add("exchanger_id", new JsonPrimitive(exchangerId));
        body.add("enough", new JsonPrimitive(isEnough));
        body.add("expires_at", new JsonPrimitive(expires));
        log.info("operation confirm body: " + body);

        HttpEntity<String> request = new HttpEntity<>(body.toString(), httpHeaders);

        ResponseEntity<String> response = getRestTemplate().exchange(
                url,
                HttpMethod.POST,
                request,
                String.class
        );

        JsonElement jsonElement = JsonParser.parseString(response.getBody());
        JsonElement result = jsonElement.getAsJsonObject().get("Result");
        ConfirmExchangeResponse confirmExchangeResponse = gson.fromJson(result.toString(), ConfirmExchangeResponse.class);

        return confirmExchangeResponse;
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
    public static class ConfirmExchangeResponse {
        private int id;
        private String expires_at;
    }

    @Data
    public static class CommitExchangeResponse {
        private Operation operation;
        private List<Exchanger> avaliable_exchangers;

        public String getOperationTime() {
            return avaliable_exchangers.stream()
                    .filter(ExchangeHttpService.Exchanger::isEnough)
                    .map(ExchangeHttpService.Exchanger::getExpires_at)
                    .findFirst()
                    .orElse("");
        }
    }

    @Data
    public static class Exchanger {
        private int id;
        private String title;
        private boolean enough;
        private Time time_bounds;
        private String expires_at;
    }

    @Data
    public static class Time {
        private boolean today;
        private String from;
        private String to;
    }

    @Data
    public static class Operation {
        private int client;
        private String direction;
        private double amount;
        private double discount;
        private double receive;
        private double rate;
    }

}

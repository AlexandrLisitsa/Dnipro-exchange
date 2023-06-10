package parser.rest;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import parser.tgclient.parser.RateParser;

import java.time.Duration;

@Slf4j
@Service
public class RateUpdater {

    private RestTemplate restTemplate = new RestTemplateBuilder()
            .setConnectTimeout(Duration.ofSeconds(10))
            .setReadTimeout(Duration.ofSeconds(30))
            .build();

    @Value("${api.token}")
    private String accessToken;
    @Value("${api.url}")
    private String apiUrl;
    private Gson gson = new Gson();


    public void updateRates(RateParser.MenorahRates rates) {
        String url = apiUrl + "/api/" + accessToken + "/menorah/update";
        log.info("Requesting menorah update: " + url);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", "application/json");

        String body = gson.toJson(rates);
        HttpEntity<Object> request = new HttpEntity<>(body, httpHeaders);

        log.info("Menorah update body: " + body);

        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                String.class
        );

        log.info(response.toString());
    }


}

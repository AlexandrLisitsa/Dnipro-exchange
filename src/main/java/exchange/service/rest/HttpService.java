package exchange.service.rest;

import com.google.gson.Gson;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.time.Duration;

@Data
public class HttpService {

    @Value("${api.token}")
    protected String accessToken;
    @Value("${api.url}")
    protected String apiUrl;

    protected RestTemplate restTemplate;
    protected Gson gson = new Gson();

    @PostConstruct
    public void init() {
        restTemplate = new RestTemplateBuilder()
                .setReadTimeout(Duration.ofMinutes(1))
                .setConnectTimeout(Duration.ofMinutes(1))
                .build();
    }

    public String getApiUrlWithToken() {
        return apiUrl + "/api/" + accessToken;
    }
}

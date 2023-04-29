package bot.exchange.service.rest;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;

@Slf4j
@Service
public class MenorahHttpService extends HttpService {

    public LinkedHashMap<String, Rates> getMenorahRates() {
        String url = getApiUrlWithToken() + "/menorah";
        log.info("Menorah rates URL: " + url);
        return getRestTemplate().exchange(
                url, HttpMethod.GET, null,
                new ParameterizedTypeReference<LinkedHashMap<String, Rates>>() {
                }).getBody();
    }

    @Data
    public static class Rates {
        private String buy;
        private String sell;
    }

}

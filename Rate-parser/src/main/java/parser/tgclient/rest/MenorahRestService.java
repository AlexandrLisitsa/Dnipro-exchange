package parser.tgclient.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import parser.tgclient.parser.RateParser;

@RestController
public class MenorahRestService {

    @Autowired
    RateParser rateParser;

    @GetMapping("/menorahRate")
    public RateParser.MenorahRates getMenorahRates() {
        return rateParser.getMenorahRates();
    }

}

package bot.tgclient.rest;

import bot.tgclient.parser.RateParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MenorahRestService {

    @Autowired
    RateParser rateParser;

    @GetMapping("/menorahRate")
    public RateParser.MenorahRates getMenorahRates() {
        return rateParser.getMenorahRates();
    }

}

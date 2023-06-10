package parser.tgclient.parser;

import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class RateParser {

    private static final Pattern ratePattern = Pattern.compile("(.*?)(\\d+\\.\\d+)\\s/\\s(\\d+\\.\\d+).*", Pattern.DOTALL);

    public MenorahRates getRates(String text) {
        String[] lines = text.split("\n");

        MenorahRates menorahRates = new MenorahRates();

        List<Currency> currencies = parseCurrency(lines);
        menorahRates.setCurrencies(currencies);

        return menorahRates;
    }

    private List<Currency> parseCurrency(String[] lines) {
        List<Currency> currencies = new ArrayList<>();
        for (String line : lines) {
            line = line.replace(",", ".");
            Matcher matcher = ratePattern.matcher(line.trim());
            if (matcher.matches()) {
                Currency currency = new Currency();
                currency.setName(Objects.requireNonNull(getName(matcher.group(1))).toString());
                currency.setBuy(Double.parseDouble(matcher.group(2)));
                currency.setSell(Double.parseDouble(matcher.group(3)));

                currencies.add(currency);
            }
        }

        return currencies;
    }

    private String getName(String text) {
        String lowerCase = text.toLowerCase(Locale.ROOT);
        if (lowerCase.contains("usd") || lowerCase.contains("дол")) {
            return CurrencyName.USD.getName();
        } else if (lowerCase.contains("eur") || lowerCase.contains("вро")) {
            return CurrencyName.EUR.getName();
        } else if (lowerCase.contains("pln") || lowerCase.contains("зло")) {
            return CurrencyName.PLN.getName();
        } else if (lowerCase.contains("gbp") || lowerCase.contains("фу")) {
            return CurrencyName.GBP.getName();
        }
        return null;
    }

    @Data
    public static class MenorahRates {
        List<Currency> currencies;
    }

    @Data
    public static class Currency {
        private String name;
        private double buy;
        private double sell;
    }

    private enum CurrencyName {
        USD("Долар"),
        PLN("Злоті"),
        EUR("Евро"),
        GBP("Фунт");

        @Getter
        private String name;

        CurrencyName(String apiName) {
            this.name = apiName;
        }
    }

}

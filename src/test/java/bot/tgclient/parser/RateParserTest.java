package bot.tgclient.parser;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RateParserTest {

    private final String rateMessageRu = "Актуальный курс 10:10\n" +
            "\n" +
            "Доллар 40.60 / 41.00\n" +
            "Евро 42.45 / 42.85\n" +
            "Злотый 8.85 / 9.25\n" +
            "\n" +
            "⚠️ Наш канал Telegram:\n" +
            "https://t.me/Vkurse_dp_ua";
    private final String rateMessageUa = "Актуальний курс на 30.12.2022\n" +
            "\n" +
            "⏰ Время работы \n" +
            "Будние: 8:00-21:00\n" +
            "Выходные: 10:00-17:00\n" +
            "\n" +
            " \uD83C\uDDFA\uD83C\uDDF8\uD83C\uDDFA\uD83C\uDDF8\uD83C\uDDFA\uD83C\uDDF8\uD83C\uDDFA\uD83C\uDDF8\uD83C\uDDFA\uD83C\uDDE6\uD83C\uDDFA\uD83C\uDDE6\uD83C\uDDFA\uD83C\uDDE6\uD83C\uDDFA\uD83C\uDDE6\uD83C\uDDFA\uD83C\uDDE6\n" +
            " Долар 40.50 / 40.90\n" +
            " \uD83C\uDDEA\uD83C\uDDFA\uD83C\uDDEA\uD83C\uDDFA\uD83C\uDDEA\uD83C\uDDFA\uD83C\uDDEA\uD83C\uDDFA\uD83C\uDDFA\uD83C\uDDE6\uD83C\uDDFA\uD83C\uDDE6\uD83C\uDDFA\uD83C\uDDE6\uD83C\uDDFA\uD83C\uDDE6\uD83C\uDDFA\uD83C\uDDE6\n" +
            " Євро 42.40 / 42.85\n" +
            " \uD83C\uDDF5\uD83C\uDDF1\uD83C\uDDF5\uD83C\uDDF1\uD83C\uDDF5\uD83C\uDDF1\uD83C\uDDF5\uD83C\uDDF1\uD83C\uDDFA\uD83C\uDDE6\uD83C\uDDFA\uD83C\uDDE6\uD83C\uDDFA\uD83C\uDDE6\uD83C\uDDFA\uD83C\uDDE6\uD83C\uDDFA\uD83C\uDDE6\n" +
            " Злоті 8.90 / 9.10\n" +
            " \uD83C\uDDEC\uD83C\uDDE7\uD83C\uDDEC\uD83C\uDDE7\uD83C\uDDEC\uD83C\uDDE7\uD83C\uDDEC\uD83C\uDDE7\uD83C\uDDFA\uD83C\uDDE6\uD83C\uDDFA\uD83C\uDDE6\uD83C\uDDFA\uD83C\uDDE6\uD83C\uDDFA\uD83C\uDDE6\uD83C\uDDFA\uD83C\uDDE6\n" +
            " Фунт 47.00 / 50.00\n" +
            " \uD83C\uDDEE\uD83C\uDDF1\uD83C\uDDEE\uD83C\uDDF1\uD83C\uDDEE\uD83C\uDDF1!\uD83C\uDDEE\uD83C\uDDF1\uD83C\uDDFA\uD83C\uDDE6\uD83C\uDDFA\uD83C\uDDE6\uD83C\uDDFA\uD83C\uDDE6\uD83C\uDDFA\uD83C\uDDE6\uD83C\uDDFA\uD83C\uDDE6\n" +
            " Приймаємо BTC/USD\n" +
            " \uD83D\uDCB0\uD83D\uDCB0\uD83D\uDCB0\uD83D\uDCB0\uD83C\uDDFA\uD83C\uDDE6\uD83C\uDDFA\uD83C\uDDE6\uD83C\uDDFA\uD83C\uDDE6\uD83C\uDDFA\uD83C\uDDE6\uD83C\uDDFA\uD83C\uDDE6\n" +
            " Золото штамп \uD83D\uDCC858.50/61.00\uD83D\uDCC8\n" +
            " \uD83E\uDD11\uD83E\uDD11\uD83E\uDD11\uD83E\uDD11\uD83E\uDD11\uD83E\uDD11\uD83E\uDD11\uD83E\uDD11\uD83E\uDD11\n" +
            "\n" +
            "\n" +
            " Курс змінюється - дзвоніть, уточнюйте,будь ласка!\n" +
            " 0679892295\n" +
            " 0971875000\n" +
            " 0952805000\n" +
            "\n" +
            " @VkurseDpUa\n" +
            " ⬅️\uD83D\uDD4D\n" +
            "\n" +
            "Ссылка на канал Telegram:\n" +
            "https://t.me/Vkurse_dp_ua";
    RateParser rateParser = new RateParser();

    @Test
    public void parseUaMessage() {
        RateParser.MenorahRates menorahRates = rateParser.parseUaMessage(rateMessageUa);
        Assertions.assertEquals(4, menorahRates.currencies.size());
    }

    @Test
    public void parseRuMessage() {
        RateParser.MenorahRates menorahRates = rateParser.parseRuMessage(rateMessageRu);
        Assertions.assertEquals(3, menorahRates.currencies.size());
    }

}

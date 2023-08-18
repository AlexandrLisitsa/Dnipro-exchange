package bot.utils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class DateTimeUtils {

    private static DateTimeFormatter dateTemplate = DateTimeFormatter.ofPattern("dd.MM.yyyy на HH.mm");

    /**
     * Returns Kiev date time in format dd.MM.yyyy на HH.mm
     *
     * @return
     */
    public static String getFormattedKievDateTime() {
        return ZonedDateTime.of(LocalDateTime.now(), ZoneId.of("GMT+3")).format(dateTemplate);
    }

}

package ARS.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class TimeUtil {

    public static boolean isExpired(String timeString) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime parsedDate = LocalDateTime.parse(timeString, formatter);
        ZonedDateTime moscowTime = ZonedDateTime.now(ZoneId.of("Europe/Moscow"));
        ZonedDateTime parsedDateTime = parsedDate.atZone(ZoneId.systemDefault());
        return parsedDateTime.isBefore(moscowTime);
    }
}

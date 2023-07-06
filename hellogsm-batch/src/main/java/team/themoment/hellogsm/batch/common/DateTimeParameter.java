package team.themoment.hellogsm.batch.common;

import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import lombok.Getter;

@Getter
public class DateTimeParameter {
    private final static DateTimeFormatter format =
            DateTimeFormatter.ofPattern("yyyy/MM/dd-HH:mm:ss-z");

    private final LocalDateTime dateTime;

    public DateTimeParameter(String strDateTime) {
        Assert.notNull(strDateTime, "strDateTime은 null일 수 없습니다.");
        try {
            ZonedDateTime zonedDateTime = ZonedDateTime.parse(strDateTime, format);
            this.dateTime = zonedDateTime.toLocalDateTime();
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("유효하지 않은 format: " + strDateTime, e);
        }
    }
}
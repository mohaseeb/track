package com.mohaseeb.mgmt.tracking;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.Instant;

class TimeUtils {
    static String localDateFormat(Instant timeStamp) {
        return timeStamp.toDateTime().toLocalDate().toString();
    }

    static Instant today() {
        DateTime todayStart = new DateTime();
        return todayStart.withHourOfDay(0)
                .withMinuteOfHour(0)
                .withSecondOfMinute(0)
                .withMillisOfSecond(0)
                .toInstant();
    }

    static Instant nextDay(Instant today) {
        return dayAfterNDays(today, 1);
    }

    static Instant monday(Instant day) {
        return toDateTime(day).withDayOfWeek(DateTimeConstants.MONDAY).toInstant();
    }

    static Instant dayAfterNDays(Instant day, int nDays) {
        DateTime dateTime = toDateTime(day);
        return dateTime.plusDays(nDays).toInstant();
    }

    static DateTime toDateTime(Instant instant) {
        return instant.toDateTime();
    }
}

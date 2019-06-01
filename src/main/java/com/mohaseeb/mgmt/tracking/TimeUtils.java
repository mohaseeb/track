package com.mohaseeb.mgmt.tracking;

import org.joda.time.*;

import java.util.Arrays;
import java.util.List;

public class TimeUtils {
    public static String localDateFormat(Instant timeStamp) {
        return timeStamp == null ? "" : timeStamp.toDateTime().toLocalDate().toString();
    }

    public static String localDateTimeFormat(Instant timeStamp) {
        return timeStamp == null ? "" : timeStamp.toDateTime().toLocalDateTime().toString();
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

    static Instant firstDayOfMonth() {
        return today().toDateTime().withDayOfMonth(1).toInstant();
    }

    static Instant firstDayOfMonth(int month) {
        return today().toDateTime().withMonthOfYear(month).withDayOfMonth(1).toInstant();
    }

    static int monthDays(int year, int month){
        return java.time.YearMonth.of(year, month).lengthOfMonth();
    }

    static Instant dayAfterNDays(Instant day, int nDays) {
        DateTime dateTime = toDateTime(day);
        return dateTime.plusDays(nDays).toInstant();
    }

    static DateTime toDateTime(Instant instant) {
        return instant.toDateTime();
    }

    static String weekDay(Instant day) {
        return day.toDateTime().toLocalDateTime().dayOfWeek().getAsShortText();
    }

    public static List<Instant> thisAndNextMonthStarts(int month) {
        if (month < 1 || month > 12) throw new IllegalArgumentException("month be in {1..12}");
        LocalDate thisMonthStart = new LocalDate(currentYear(), month, 1);
        LocalDate nextMonthStart = thisMonthStart.plusMonths(1);
        return Arrays.asList(
                thisMonthStart.toDateTime(LocalTime.MIDNIGHT).toInstant(),
                nextMonthStart.toDateTime(LocalTime.MIDNIGHT).toInstant()
        );
    }

    private static int currentYear() {
        return LocalDate.now().getYear();
    }
}
